package com.playhills.lasertag.commands.user_commands;

import com.playhills.lasertag.Main;
import de.fkfabian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.HashMap;

public class StatsCommand implements CommandExecutor {

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
            p.sendMessage(Main.prefix + "§cDieser Befehl hat einen cooldown. (" + (((uuid_stats_time_long.get(p.getUniqueId().toString()) - current) / 1000) + 1) + " Sekunden)");
            return false;
        }

        Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
            try {
                uuid_stats_time_long.put(p.getUniqueId().toString(), current+(1000*60));
                message(p, args.length == 0 ? p.getName() : args[0]);
            } catch (Exception exception) {
                uuid_stats_time_long.put(p.getUniqueId().toString(), current+(1000*15));
                sender.sendMessage(Main.prefix + "§cDer Spieler hat keine protokollierten Stats.");
            }
        });
        return false;
    }

    private void message(Player sender, String requested) {
        String UUID = API.getInstance().getUUIDSync(requested).toString();
        ResultSet set = Main.plugin.getMariaDB().query("SELECT * FROM `Stats` WHERE `UUID`='"+UUID+"'");
        if (set == null) {
            sender.sendMessage(Main.prefix + "§cDer Spieler hat keine protokollierten Stats.");
            return;
        }
        try {
            while (set.next()) {
                sender.sendMessage("§8§m           §8[§9§l"+requested.toUpperCase()+"§8]§m           ");
                sender.sendMessage("§8>   §7Totale Spiele§8: §3"+set.getString("Games"));
                sender.sendMessage("§8>   §7Gewonnene Spiele§8: §3"+set.getString("Wins"));
                sender.sendMessage("§8>   §7Verlorene Spiele§8: §3"+set.getString("Loses"));
                sender.sendMessage("§9§l§c§r");
                sender.sendMessage("§8>   §7Totale Kills§8: §3"+(set.getInt("Kills")+set.getInt("FinalKills")));
                sender.sendMessage("§8>   §7Finale Kills§8: §3"+set.getInt("FinalKills"));
                sender.sendMessage("§8>   §7Totale Tode§8: §3"+(set.getInt("Deaths")+set.getInt("FinalDeaths")));
                sender.sendMessage("§8>   §7Finale Tode§8: §3"+set.getInt("FinalDeaths"));
                sender.sendMessage("§8§m           §8[§9§l"+requested.toUpperCase()+"§8]§m           ");
                return;
            }
        } catch (Exception exception) {
            sender.sendMessage(Main.prefix + "§cDer Spieler hat keine protokollierten Stats.");
        }
    }
}
