package net.kunmc.lab.kpmupdater;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.kpmupdater.plugin.Pair;
import net.kunmc.lab.kpmupdater.utils.URLUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubUrlBuilder
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";
    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repo>[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}/[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,100})(?:/?$|/(?:tag|release)s(?:/?$|/(?:tag/(?<tag>[^/]+)/?$|download/(?<dlTag>[^/]+)/(?<fileName>[^/]+))))/?$");

    /**
     * GitHubのリリースへのAPIのURlをビルドします。
     * tagName をnullにできます。
     *
     * @param repository リポジトリ名
     * @param tagName    タグ(リリース)バージョン
     * @return URlのタイプ, ビルドしたURL
     * @throws NullPointerException repositoryがnullだった場合
     */
    private static Pair<String, String> buildAPIUrl(String repository, @Nullable String tagName)
    {
        if (repository == null)
            return new Pair<>("ERROR", "");

        if (tagName == null)
            return new Pair<>("GITHUB_REPO_RELEASES_URL", String.format(GITHUB_REPO_RELEASES_URL, repository));
        else
            return new Pair<>("GITHUB_REPO_RELEASE_NAME_URL", String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tagName));
    }

    /**
     * GitHubのURLを適正化
     * https://github.com/examples/ExamplePlugin => https://github.com/examples/ExamplePlugin/releases/download/1.0/ExamplePlugin-1.0.jar
     * https://example.com/ => https://example.com/
     *
     * @param urlName 適当なURL
     * @return GitHubのURl
     */
    public static String urlValidate(String urlName, String version)
    {
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch (Exception e)
        {
            return urlName;
        }

        if (!url.getHost().equals("github.com") && !url.getHost().equals("www.github.com"))
            return urlName;

        Matcher matcher = GITHUB_REPO_PATTERN.matcher(url.getPath());

        String repoName = null;
        String tagName = null;

        while (matcher.find())
        {
            String repository = matcher.group("repo");
            String tag = matcher.group("dlTag");
            String tagNF = matcher.group("tag");
            String file = matcher.group("fileName");

            if (file != null && !file.equals(""))
                return urlName;


            if (!repository.equals(""))
                repoName = repository;
            if (tag != null && !tag.equals(""))
                tagName = tag;
            else if (tagNF != null && !tagNF.equals(""))
                tagName = tagNF;
        }

        if (tagName == null && version != null)
            tagName = version;

        Pair<String, String> urlPair = buildAPIUrl(repoName, tagName);

        switch (urlPair.getKey())
        {
            case "GITHUB_REPO_RELEASES_URL":
            {
                Pair<Integer, String> json = URLUtils.getAsString(urlPair.getValue());
                switch (json.getKey())
                {
                    case 404:
                        return "ERROR ファイルが見つかりませんでした。";
                    case 403:
                        return "E: ファイルを取得できません。しばらくしてからもう一度実行してください。";
                }

                if (json.getKey() != 200)
                    return "ERROR 不明なエラーが発生しました。";

                String error = error(json.getValue());
                if (!error.equals(""))
                    return "ERROR " + error;
                JsonArray array = new Gson().fromJson(json.getValue(), JsonArray.class);

                for (JsonElement elem : array)
                    for (JsonElement asset : ((JsonObject) elem).get("assets").getAsJsonArray())
                    {
                        if (StringUtils.endsWithIgnoreCase(((JsonObject) asset).get("name").getAsString(), ".jar"))
                            return ((JsonObject) asset).get("browser_download_url").getAsString();
                    }
                return "ERROR アーティファクトが見つかりませんでした。";
            }
            case "GITHUB_REPO_RELEASE_NAME_URL":
            {
                Pair<Integer, String> json = URLUtils.getAsString(urlPair.getValue());

                switch (json.getKey())
                {
                    case 404:
                        return "ERROR ファイルが見つかりませんでした。";
                    case 403:
                        return "E: ファイルを取得できません。しばらくしてからもう一度実行してください。";
                }

                if (json.getKey() != 200)
                    return "ERROR 不明なエラーが発生しました。";
                String error = error(json.getValue());
                if (!error.equals(""))
                    return "ERROR " + error;
                JsonObject array = new Gson().fromJson(json.getValue(), JsonObject.class);
                for (JsonElement asset : array.get("assets").getAsJsonArray())
                {
                    if (StringUtils.endsWithIgnoreCase(((JsonObject) asset).get("name").getAsString(), ".jar"))
                        return ((JsonObject) asset).get("browser_download_url").getAsString();
                }
                return "ERROR アーティファクトが見つかりませんでした。";
            }
            case "ERROR":
            default:
                return url.toString();
        }
    }

    private static String error(String json)
    {
        try
        {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (!jsonObject.has("message"))
                return "";
            return jsonObject.get("message").getAsString();
        }
        catch (Exception ignored)
        {
            return "";
        }
    }

    public static boolean isRepoExists(String name)
    {
        return String.valueOf(URLUtils.fetch("https://api.github.com/repos/" + name + "", "HEAD")).startsWith("2");
    }

    public static boolean isPermissionGranted(String path)
    {
        return URLUtils.fetch("https://api.github.com" + path, "HEAD") != 401;
    }
}
