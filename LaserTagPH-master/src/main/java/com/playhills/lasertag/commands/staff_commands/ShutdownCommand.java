package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShutdownCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.shutdown")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        Main.plugin.getLaserTag().shutdown();

        return false;
    }
}