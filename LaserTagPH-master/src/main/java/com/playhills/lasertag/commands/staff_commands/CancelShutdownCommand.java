package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.Main;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelShutdownCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.command.cancelshutdown")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        if (Main.plugin.getLaserTag().shutdownINT == 16) {
            p.sendMessage(Main.prefix + "Der Stopp-Prozess läuft nicht.");
            return false;
        }

        BaseComponent bc = new TextComponent("");
        bc.addExtra(Main.prefix + "§7Klicke ");
        BaseComponent click = new TextComponent("§aHIER");
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§2/shutdown")));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shutdown"));
        bc.addExtra(click);
        bc.addExtra(" §7um den Server zu stoppen.");
        Bukkit.getOnlinePlayers().forEach(all -> {
            all.sendMessage(Main.prefix+"Der Stopp-Prozess wurde von §c"+p.getName()+" §7unterbrochen.");
            if (all.hasPermission("LaserTag.command.cancelshutdown") || all.hasPermission("LaserTag.command.showshutdown")) all.spigot().sendMessage(bc);
        });
        Main.plugin.getLaserTag().cancel_shutdown();

        return false;
    }
}