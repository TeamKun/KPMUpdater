package net.kunmc.lab.kpmupdater.commands;

import net.kunmc.lab.kpmupdater.KPMUpdater;
import net.kunmc.lab.kpmupdater.plugin.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tokyo.peya.lib.bukkit.Say2Functional;

import java.util.UUID;

public class UpdateCommand implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("TeamKunPluginManager"))
        {
            sender.sendMessage(ChatColor.RED + "エラー：TeamKunPluginManager がインストールされていません。このプラグインを削除してください。");
            return true;
        }
        UUID id = null;

        if (sender instanceof Player)
            id = ((Player) sender).getUniqueId();

        KPMUpdater.sf.add(id, new Say2Functional.FunctionalEntry(String::equalsIgnoreCase, s -> {
            if (s.equalsIgnoreCase("y"))
                Updater.doUpdate(sender);
            else
                sender.sendMessage(ChatColor.RED + "アップデートをキャンセルしました。/kpmupdate から手動でアップデートすることができます。");
        }, "y", "n"));

        return true;
    }
}
