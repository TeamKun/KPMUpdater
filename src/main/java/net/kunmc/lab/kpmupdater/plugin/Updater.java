package net.kunmc.lab.kpmupdater.plugin;

import net.kunmc.lab.kpmupdater.KPMUpdater;
import net.kunmc.lab.kpmupdater.utils.InstallResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Updater
{
    private static boolean processing;
    public static String processor;

    public static void doUpdate(CommandSender sender)
    {
        if (processing)
        {
            sender.sendMessage(ChatColor.RED + "E: " + processor + " によるアップデートが実行中です。");
            return;
        }

        processing = true;
        if (sender instanceof Player)
            processor = sender.getName();
        else
            processor = "CONSOLE";

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "アップデートをしています...");

        if (!KPMUpdater.vault.success)
        {
            sender.sendMessage(ChatColor.RED + "エラー：！トークンが保存されていませんでした。");
            sender.sendMessage(ChatColor.GRAY + "NOTE：/kpm register を利用してください。");
            return;
        }

        if (!Installer.unInstall("TeamKunPluginManager", true))
        {
            sender.sendMessage(ChatColor.RED + "エラー：プラグインの削除に失敗しました。");
            fail(sender);
            return;
        }

        InstallResult result = Installer.install("TeamKun/TeamKunPluginManager", false);

        if (!result.success)
        {
            sender.sendMessage(ChatColor.RED + "エラー：プラグインのインストールに失敗しました。");
            fail(sender);
            return;
        }

        sender.sendMessage(getStatusMessage(result.add, result.remove, result.modify));
        sender.sendMessage(ChatColor.GREEN + "成功：TeamKunPluginManager を正常にアップデートしました。");
        fail(sender);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                processing = false;
            }
        }.runTaskLater(KPMUpdater.plugin, 10);
    }

    public static String getStatusMessage(int installed, int removed, int modified)
    {
        return ChatColor.GREEN.toString() + installed + " 追加 " + ChatColor.RED + removed + " 削除 " + ChatColor.YELLOW + modified + " 変更";
    }

    public static void fail(CommandSender sender)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.getServer().dispatchCommand(sender, "kpm uninstall KPMUpdater");
            }
        }.runTaskLater(KPMUpdater.plugin, 2);
    }
}
