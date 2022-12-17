package com.playhills.lasertag.commands.staff_commands;

import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.sql.MariaDB;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;

public class ResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("LaserTag.game.resetall")) {
            p.sendMessage(Main.noPerms);
            return false;
        }

        try {
            if (args[0].equals("yesiamsuretoresetstats")) {
                Bukkit.getOnlinePlayers().forEach(all -> all.kickPlayer(Main.prefix + "Verbindung vernichtet.\n\nDie Datenbank wurde von\n"+p.getName()+" geleert."));
                Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
                    MariaDB db = Main.plugin.getMariaDB();
                    ResultSet set = db.query("SELECT * FROM `Stats`;");
                    if (set == null) {
                        p.sendMessage(Main.prefix + "§cDie Datenbank ist bereits leer. (NULL)");
                        return;
                    }
                    try {
                        while (set.next()) {
                            db.update("DELETE FROM `Stats` WHERE UUID='" + set.getString("UUID") + "'");
                        }
                    } catch (Exception ignore) {
                        p.sendMessage(Main.prefix + "§cDie Datenbank ist bereits leer. (NEXT)");
                    }
                });
            } else {
                false_usage(p);
            }
        } catch (Exception e) {
            false_usage(p);
        }

        return false;
    }

    private void false_usage(Player p) {
        p.sendMessage(Main.prefix + "§cDu bist dabei, §4FATALE ÄNDERUNGEN §can der LaserTag-Datenbank vorzunehmen.");
        BaseComponent base = new TextComponent(Main.prefix + "Bitte ");
        TextComponent c1 = new TextComponent("§4bestätige");
        c1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reset yesiamsuretoresetstats"));
        c1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Du bist dabei, alle Datenbank\n§7Einträge zu gespeicherten Stats\n§7dauerhaft zu entfernen.")));
        TextComponent c2 = new TextComponent("§7, dass du mit deine Aktion durchführen möchtest.");

        base.addExtra(c1);
        base.addExtra(c2);
        p.sendMessage(base);
    }
}
