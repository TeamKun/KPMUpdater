package net.kunmc.lab.kpmupdater.commands;

import net.kunmc.lab.kpmupdater.plugin.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        Updater.doUpdate(sender);
        return true;
    }
}
