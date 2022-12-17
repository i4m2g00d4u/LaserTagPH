package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.specials.Special;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.item")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        Special sp = Main.plugin.getSpecialManager().getSpecialByName(args[0]);
        if(sp == null) {
            p.sendMessage(Main.prefix + "Unbekannte Syntax.");
            return false;
        }

        sp.onPickup(p);
        p.sendMessage(Main.prefix + "Du hast 1x §a" + sp.getName() + " §7erhalten.");

        return false;
    }

    //PUSH ERROR
}
