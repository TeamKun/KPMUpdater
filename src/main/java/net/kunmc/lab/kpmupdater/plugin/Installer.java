package net.kunmc.lab.kpmupdater.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kunmc.lab.kpmupdater.GithubUrlBuilder;
import net.kunmc.lab.kpmupdater.KPMUpdater;
import net.kunmc.lab.kpmupdater.utils.InstallResult;
import net.kunmc.lab.kpmupdater.utils.PluginUtil;
import net.kunmc.lab.kpmupdater.utils.URLUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class Installer
{

    /**
     * アンインストールをする
     *
     * @param name  対象プラグ
     * @param force 強制削除かどうか
     */
    public static boolean unInstall(String name, boolean force)
    {

        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);

        PluginUtil.unload(plugin);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                File file = PluginUtil.getFile(plugin);
                if (file != null)
                    file.delete();

            }
        }.runTaskLaterAsynchronously(KPMUpdater.plugin, 20L);
        return true;
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

    public static InstallResult install(String repoName, boolean withoutRemove)
    {
        ArrayList<InstallResult> added = new ArrayList<>();

        int add = 0;
        int remove = 0;
        int modify = 0;

        String directLink = GithubUrlBuilder.fetchLatestDirectLink(repoName);

        if (directLink.startsWith("ERROR "))
            return new InstallResult(add, remove, modify, false);

        long startTime = System.currentTimeMillis();

        Pair<Boolean, String> downloadResult = URLUtils.downloadFile(directLink);
        if (downloadResult.getValue().isEmpty())
            return new InstallResult(add, remove, modify, false);

        add++;

        PluginDescriptionFile description;

        try
        {
            description = PluginUtil.loadDescription(new File("plugins/" + downloadResult.getValue()));
        }
        catch (FileNotFoundException e)
        {

            if (!withoutRemove)
                delete(new File("plugins/" + downloadResult.getValue()));
            return new InstallResult(add, remove, modify, false);
        }
        catch (IOException | InvalidDescriptionException e)
        {
            if (!withoutRemove)
                return new InstallResult(add, remove, modify, false);

            return new InstallResult(add, remove, modify, false);
        }

        String fileName = downloadResult.getValue();
        try
        {
            if (PluginUtil.isPluginLoaded(description.getName()))
            {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(description.getName());

                assert plugin != null;

                if (!withoutRemove)
                    delete(new File("plugins/" + fileName));

                PluginUtil.unload(plugin);

                new BukkitRunnable()
                {

                    @Override
                    public void run()
                    {
                        File file = PluginUtil.getFile(plugin);
                        if (!withoutRemove && file != null)
                            file.delete();
                    }
                }.runTaskLaterAsynchronously(KPMUpdater.plugin, 20L);
            }

            PluginUtil.load(fileName.substring(0, fileName.length() - 4));
        }
        catch (Exception e)
        {
            if (!withoutRemove)
                delete(new File("plugins/" + fileName));
            e.printStackTrace();
            return new InstallResult(add, remove, modify, false);
        }

        return new InstallResult(downloadResult.getValue(), description.getName(), add, remove, modify, true);
    }

    public static boolean delete(File f)
    {
        try
        {
            f.delete();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
