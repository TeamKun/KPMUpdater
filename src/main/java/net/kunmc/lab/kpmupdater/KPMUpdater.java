package net.kunmc.lab.kpmupdater;

import net.kunmc.lab.kpmupdater.commands.UpdateCommand;
import net.kunmc.lab.kpmupdater.plugin.Updater;
import net.kunmc.lab.kpmupdater.utils.Say2Functional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

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
        vault = new TokenVault();
        getCommand("kpmupdate").setExecutor(new UpdateCommand());

        if (!Bukkit.getPluginManager().isPluginEnabled("TeamKunPluginManager"))
        {
            logger.severe("TeamKunPluginManager がインストールされていません。このプラグインを削除してください。");
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!player.hasPermission("kpm.use"))
                return;
            player.sendMessage(ChatColor.GREEN + "KPMUpdaterがインストールされました。インストールを続行しますか？ Y/n>");
            KPMUpdater.sf.add(player.getUniqueId(), new Say2Functional.FunctionalEntry(String::equalsIgnoreCase, s -> {
                if (s.equalsIgnoreCase("y"))
                    Updater.doUpdate(Bukkit.getConsoleSender());
                else
                    player.sendMessage(ChatColor.RED + "アップデートをキャンセルしました。/kpmupdate から手動でアップデートすることができます。");
            }, "y", "n"));
        });

        logger.info("アップデートを実行しますか？");
        KPMUpdater.sf.add(null, new Say2Functional.FunctionalEntry(String::equalsIgnoreCase, s -> {
            if (s.equalsIgnoreCase("y"))
                Updater.doUpdate(Bukkit.getConsoleSender());
            else
                logger.warning(ChatColor.RED + "アップデートをキャンセルしました。/kpmupdate から手動でアップデートすることができます。");
        }, "y", "n"));
    }
}
