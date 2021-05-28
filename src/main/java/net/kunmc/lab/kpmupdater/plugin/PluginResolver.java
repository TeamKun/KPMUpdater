package net.kunmc.lab.kpmupdater.plugin;

import net.kunmc.lab.kpmupdater.GithubUrlBuilder;
import net.kunmc.lab.kpmupdater.utils.rdmaker.DevBukkit;
import net.kunmc.lab.kpmupdater.utils.rdmaker.Spigotmc;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.Bukkit;

import java.util.List;

public class PluginResolver
{

    /**
     * プラグインのURLを名前解決します。
     *
     * @param query 指定
     * @return URLまたはError
     */
    public static String asUrl(String query)
    {
        String[] q = StringUtils.split(query, "@");

        String s = q[0];
        String ver = q.length > 1 ? q[1]: null;

        if (UrlValidator.getInstance().isValid(query))
        {
            if (DevBukkit.isMatch(query))
                return DevBukkit.toDownloadUrl(query);
            else if (Spigotmc.isMatch(query))
                return Spigotmc.toDownloadUrl(query);
            return GithubUrlBuilder.urlValidate(s, ver);
        }

        if (StringUtils.split(query, "/").length == 2)
            return GithubUrlBuilder.urlValidate("https://github.com/" + s, ver);

        //configのorgを順番にfetch

        Object obj = Bukkit.getPluginManager().getPlugin("TeamKunPluginManager").getConfig().get("gitHubName");

        if (obj instanceof String)
            if (GithubUrlBuilder.isRepoExists(obj + "/" + s))
                return GithubUrlBuilder.urlValidate("https://github.com/" + obj + "/" + s, ver);
            else
                return "ERROR " + query + "が見つかりませんでした。";

        if (!(obj instanceof List) && !(obj instanceof String[]))
            return "ERROR " + query + "が見つかりませんでした。";


        for (String str : Bukkit.getPluginManager().getPlugin("TeamKunPluginManager").getConfig().getStringList("gitHubName"))
        {
            if (GithubUrlBuilder.isRepoExists(str + "/" + query))
                return GithubUrlBuilder.urlValidate("https://github.com/" + str + "/" + s, ver);
        }

        return "ERROR " + query + "が見つかりませんでした。";
    }

}
