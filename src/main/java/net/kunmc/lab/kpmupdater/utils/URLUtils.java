package net.kunmc.lab.kpmupdater.utils;

import net.kunmc.lab.kpmupdater.KPMUpdater;
import net.kunmc.lab.kpmupdater.plugin.Pair;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

public class URLUtils
{
    /**
     * URLから取得した結果を返す。
     *
     * @param urlString URL
     * @return レスポンスコード, 結果
     */
    public static Pair<Integer, String> getAsString(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + KPMUpdater.vault.getToken());
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();

            return new Pair<>(connection.getResponseCode(), IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair<>(-1, "");
        }
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url URL
     * @return ローカルのパス
     */
    public static Pair<Boolean, String> downloadFile(String url)
    {
        return downloadFile(url, url.substring(url.lastIndexOf("/") + 1));
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url      URL
     * @param fileName ファイル名
     * @return ローカルのパス
     */
    public static Pair<Boolean, String> downloadFile(String url, String fileName)
    {
        boolean duplicateFile = false;

        if (fileName.equals(""))
            fileName = "tmp1.jar";

        int tryna = 0;
        String original = fileName;
        while (new File("plugins/" + fileName).exists())
        {
            fileName = original + "." + ++tryna + ".jar";
            duplicateFile = true;
        }

        tryna = 0;

        final int redirectLimit = Bukkit.getPluginManager().getPlugin("TeamKunPluginManager").getConfig().getInt("redirectLimit", 15);

        try
        {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            boolean redir;
            do
            {
                if (tryna++ > redirectLimit)
                    throw new IOException("Too many redirects.");

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                if (urlObj.getHost().equals("api.github.com"))
                    connection.setRequestProperty("Authorization", "token " + KPMUpdater.vault.getToken());
                connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
                connection.connect();

                redir = false;
                if (String.valueOf(connection.getResponseCode()).startsWith("3"))
                {
                    URL base = connection.getURL();
                    String locationStr = connection.getHeaderField("Location");
                    if (locationStr != null)
                        base = new URL(base, locationStr);

                    //connection.disconnect();
                    if (base != null)
                    {
                        redir = true;
                        connection = (HttpURLConnection) base.openConnection();
                    }
                }

            }
            while (redir);

            File file = new File("plugins/" + fileName);

            if (!file.createNewFile())
                throw new NoSuchFileException("ファイルの作成に失敗しました。");

            try (InputStream is = connection.getInputStream();
                 OutputStream os = new FileOutputStream(file))
            {
                IOUtils.copy(is, os);
            }


            //FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
            return new Pair<>(duplicateFile, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair<>(false, "");

        }
    }

    public static int fetch(String urlString, String method)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + KPMUpdater.vault.getToken());
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();
            return connection.getResponseCode();

        }
        catch (Exception e)
        {
            return 500;
        }
    }
}
