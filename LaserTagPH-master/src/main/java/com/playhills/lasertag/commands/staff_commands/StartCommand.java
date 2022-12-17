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

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.start.default") && !p.hasPermission("LaserTag.command.start.bypass")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        if (Main.plugin.getLaserTag().game_gamestate != LaserTag.GAME.WAITING || Main.plugin.getLaserTag().countINT <= 10) {
            p.sendMessage(Main.prefix+"Das Spiel ist bereits gestartet.");
            return false;
        }

        if (!p.hasPermission("LaserTag.command.start.bypass") && Main.plugin.getLaserTag().countINT == 61) {
            p.sendMessage(Main.prefix + "Das Spiel kann noch nicht gestartet werden!");
            return false;
        }

        BaseComponent bc = new TextComponent("");
        bc.addExtra(Main.prefix+"Die Wartezeit wurde verkürzt.");
        Bukkit.getOnlinePlayers().forEach(all -> all.spigot().sendMessage(bc));
        Main.plugin.getLaserTag().cancel_countdown(true);
        Main.plugin.getLaserTag().countdown();
        Main.plugin.getLaserTag().countINT=11;

        return false;
    }
}