package com.playhills.lasertag.commands.user_commands;

import com.playhills.lasertag.Main;
import de.fkfabian.api.API;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TopCommand implements CommandExecutor {

    private final HashMap<String, Long> uuid_stats_time_long = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.playerOnly);
            return false;
        }

        Player p = (Player) sender;
        long current = System.currentTimeMillis();
        if (uuid_stats_time_long.containsKey(p.getUniqueId().toString()) && uuid_stats_time_long.get(p.getUniqueId().toString()) > current) {
            p.sendMessage(Main.prefix + "§cDieser Befehl hat einen cooldown. ("+(((uuid_stats_time_long.get(p.getUniqueId().toString())-current)/1000)+1)+" Sekunden)");
            return false;
        }
        uuid_stats_time_long.put(p.getUniqueId().toString(), current+(1000*60));

        Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
            ResultSet set = Main.plugin.getMariaDB().query("SELECT * FROM `Stats` ORDER BY `Wins` DESC LIMIT 10;");
            if (set == null) {
                p.sendMessage(Main.prefix + "Es wurden noch keine TOP-Spieler aufgezeichnet.");
                return;
            }
            ArrayList<TextComponent> toAdd = new ArrayList<>();
            int i = 0;
            try {
                while (set.next()) {
                    i++;
                    String color;
                    switch (i) {
                        case 1:
                            color = "§9§o";
                            break;
                        case 2:
                            color = "§3§o";
                            break;
                        case 3:
                            color = "§b§o";
                            break;
                        default: color = "§7§o";
                    }
                    String name = API.getInstance().getNameSync(UUID.fromString(set.getString("UUID")));
                    TextComponent component = new TextComponent("\n§8» "+color+"#"+i+" §8| "+color+name);
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(color+"#"+i+" §8| "+color+name+"\n\n§8» §e"+set.getInt("Wins")+" Siege\n\n§7/stats "+name+"\n§7§oFür mehr Informationen.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats "+name));
                    toAdd.add(component);
                }
            } catch (Exception exception) {
                p.sendMessage(Main.prefix + "Es wurden noch keine TOP-Spieler aufgezeichnet.");
                return;
            }
            BaseComponent comp = new TextComponent(Main.prefix + "Showing top "+i+" players§8:");
            toAdd.forEach(comp::addExtra);
            p.sendMessage(comp);
        });

        return false;
    }
}
