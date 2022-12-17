package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CountdownCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.countdown")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        if (Main.plugin.getLaserTag().game_gamestate != LaserTag.GAME.WAITING || Main.plugin.getLaserTag().countINT == 0) {
            p.sendMessage(Main.prefix+"Das Spiel ist bereits gestartet.");
            return false;
        }

        BaseComponent bc = new TextComponent("");
        bc.addExtra(Main.prefix + "§c" +p.getName()+" §7hat den Countdown gestartet!");
        Bukkit.getOnlinePlayers().forEach(all -> all.spigot().sendMessage(bc));
        Main.plugin.getLaserTag().countdown();

        return false;
    }
}
