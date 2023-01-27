package net.kunmc.lab.kpmupdater;

import net.kunmc.lab.kpmupdater.plugin.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class KPMUpdater extends JavaPlugin
{
    public static Logger logger;
    public static KPMUpdater plugin;
    public static TokenVault vault;

    @Override
    public void onEnable()
    {
        plugin = this;
        logger = getLogger();
        vault = new TokenVault();

        if (!Bukkit.getPluginManager().isPluginEnabled("TeamKunPluginManager"))
        {
            logger.severe("TeamKunPluginManager がインストールされていません。このプラグインを削除してください。");
            return;
        }

        Updater.doUpdate(Bukkit.getConsoleSender());
    }
}
