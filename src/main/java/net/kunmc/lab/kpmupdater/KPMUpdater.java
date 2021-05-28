package net.kunmc.lab.kpmupdater;

import net.kunmc.lab.kpmupdater.commands.UpdateCommand;
import net.kunmc.lab.kpmupdater.plugin.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import tokyo.peya.lib.bukkit.Say2Functional;

import java.util.logging.Logger;

public final class KPMUpdater extends JavaPlugin
{
    public static Say2Functional sf;
    public static Logger logger;
    public static KPMUpdater plugin;
    public static TokenVault vault;

    @Override
    public void onEnable()
    {
        plugin = this;
        logger = getLogger();
        sf = new Say2Functional(this);
        getCommand("kpmupdate").setExecutor(new UpdateCommand());

        if (!Bukkit.getPluginManager().isPluginEnabled("TeamKunPluginManager"))
        {
            logger.severe("TeamKunPluginManager がインストールされていません。このプラグインを削除してください。");
            return;
        }

        logger.info("アップデートを実行しますか？");
        KPMUpdater.sf.add(null, new Say2Functional.FunctionalEntry(String::equalsIgnoreCase, s -> {
            if (s.equalsIgnoreCase("y"))
                Updater.doUpdate(Bukkit.getConsoleSender());
            else
                logger.warning(ChatColor.RED + "アップデートをキャンセルしました。/kpmupdate から手動でアップデートすることができます。");
        }, "y", "n"));
    }
}
