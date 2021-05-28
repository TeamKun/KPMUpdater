package net.kunmc.lab.kpmupdater.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * URlからぶちこむ！
     *
     * @param url                   URL!!!
     * @param ignoreInstall         インストールを除外するかどうか
     * @param withoutResolveDepends 依存関係解決をしない
     * @return ファイル名, プラグイン名
     */
    public static InstallResult install(String url, boolean ignoreInstall, boolean withoutResolveDepends, boolean withoutRemove)
    {

        AtomicReference<String> atomicURL = new AtomicReference<>(url);
        ArrayList<InstallResult> added = new ArrayList<>();

        int add = 0;
        int remove = 0;
        int modify = 0;

        atomicURL.set(PluginResolver.asUrl(url));

        if (atomicURL.get().startsWith("ERROR "))
            return new InstallResult(add, remove, modify, false);

        long startTime = System.currentTimeMillis();

        Pair<Boolean, String> downloadResult = URLUtils.downloadFile(atomicURL.get());
        if (downloadResult.getValue().equals(""))
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

        Plugin plugin = Bukkit.getPluginManager().getPlugin(description.getName());

        added.add(new InstallResult(downloadResult.getValue(), description.getName(), add, remove, modify, true));

        boolean dependFirst = true;
        ArrayList<String> failedResolve = new ArrayList<>();
        for (String dependency : description.getDepend())
        {
            if (withoutResolveDepends)
                break;
            if (Bukkit.getPluginManager().isPluginEnabled(dependency))
                continue;
            if (dependFirst)
            {

                startTime = System.currentTimeMillis();
                dependFirst = false;
            }

            String dependUrl = PluginResolver.asUrl(dependency);
            if (dependUrl.startsWith("ERROR "))
            {
                failedResolve.add(dependency);
                continue;
            }

            Installer.install(dependUrl, true, false, true);

        }


        if (failedResolve.size() > 0)
            return new InstallResult(downloadResult.getValue(), description.getName(), add, remove, modify, true);

        AtomicBoolean success = new AtomicBoolean(true);

        if (!ignoreInstall)
        {
            ArrayList<InstallResult> loadOrder = PluginUtil.mathLoadOrder(added);
            for (InstallResult f : loadOrder)
            {
                try
                {
                    if (PluginUtil.isPluginLoaded(description.getName()))
                    {


                        if (!withoutRemove)
                            delete(new File("plugins/" + f.fileName));

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

                    PluginUtil.load(f.fileName.substring(0, f.fileName.length() - 4));
                }
                catch (Exception e)
                {
                    if (!withoutRemove)
                        delete(new File("plugins/" + f.fileName));
                    e.printStackTrace();
                    success.set(false);
                }
            }
        }
        if (!success.get())
            return new InstallResult(downloadResult.getValue(), description.getName(), add, remove, modify, false);

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
