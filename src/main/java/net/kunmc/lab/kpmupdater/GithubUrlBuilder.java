package net.kunmc.lab.kpmupdater;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.kpmupdater.plugin.Pair;
import net.kunmc.lab.kpmupdater.utils.URLUtils;
import org.apache.commons.lang.StringUtils;

public class GithubUrlBuilder
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";

    public static String fetchLatestDirectLink(String repoName)
    {
        Pair<Integer, String> json = URLUtils.getAsString(String.format(GITHUB_REPO_RELEASES_URL, repoName));
        switch (json.getKey())
        {
            case 404:
                return "ERROR ファイルが見つかりませんでした。";
            case 403:
                return "ERROR ファイルを取得できません。しばらくしてからもう一度実行してください。";
        }

        if (json.getKey() != 200)
            return "ERROR 不明なエラーが発生しました。";

        String error = error(json.getValue());
        if (error != null)
            return "ERROR " + error;
        JsonArray array = new Gson().fromJson(json.getValue(), JsonArray.class);

        for (JsonElement elem : array)
        {
            if (((JsonObject) elem).get("prerelease").getAsBoolean())
                continue;

            for (JsonElement asset : ((JsonObject) elem).get("assets").getAsJsonArray())
            {
                JsonObject obj = asset.getAsJsonObject();

                if (StringUtils.endsWithIgnoreCase(obj.get("name").getAsString(), ".jar"))
                    return obj.get("browser_download_url").getAsString();
            }
        }

        return "ERROR アーティファクトが見つかりませんでした。";
    }

    private static String error(String json)
    {
        try
        {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (!jsonObject.has("message"))
                return null;
            return jsonObject.get("message").getAsString();
        }
        catch (Exception ignored)
        {
            return null;
        }
    }
}
