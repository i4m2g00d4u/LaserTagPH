package com.playhills.lasertag.listener;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.map.Map;
import com.playhills.lasertag.manager.sql.MariaDB;
import com.playhills.lasertag.manager.teams.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;

public class GlobalListener implements Listener {

    private final LaserTag laserTag = Main.plugin.getLaserTag();

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (laserTag.game_developer_mode) {
            if (!p.isWhitelisted() && !p.isOp()) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Main.prefix + "Du bist nicht auf der Whitelist.");
                return;
            }
        }
        if (laserTag.game_gamestate.equals(LaserTag.GAME.ENDING)) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Main.prefix + "§cDie Runde ist bereits vorbei! :(");
        } else if (laserTag.game_gamestate.equals(LaserTag.GAME.WAITING) && Bukkit.getOnlinePlayers().size() >= laserTag.allowed_players[1]) {
            if (!p.hasPermission("LaserTag.game.bypassPlayerLimit")) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Main.prefix + "§cDie Runde ist voll! :(");
            } else {
                laserTag.allowed_players[1]++;
                p.sendMessage(Main.prefix + " §7Um dir Platz zu gewährleisten, wurde die maximale Spielerzahl angepasst.");
            }
        } else if (laserTag.game_gamestate == LaserTag.GAME.RUNNING || laserTag.game_gamestate == LaserTag.GAME.DEATHMATCH) {
            laserTag.spectators.add(p.getName());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void ach(PlayerAchievementAwardedEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (laserTag.game_gamestate != LaserTag.GAME.RUNNING && laserTag.game_gamestate != LaserTag.GAME.DEATHMATCH) return;
        if (!p.getWorld().getName().equals("LaserTag")) return;
        if (laserTag.roles.get(p.getName()) == LaserTag.ROLE.WAITING) return;
        if (p.getGameMode() == GameMode.CREATIVE) return;
        if (!laserTag.spectators.contains(p.getName())) {
            if (Main.plugin.getMapManager().getCurrentMap() == null) return;
            Map map = Main.plugin.getMapManager().getCurrentMap();
            if (p.getLocation().getY() >= map.getMax_height()-3) {
                Main.plugin.getLaserTag().sendActionBar(p, "§cDu bist zu hoch auf der Map.");
            }
            if (p.getLocation().getY() >= map.getMax_height()) {
                p.sendMessage(Main.prefix+ "Du bist zu weit von der Map entfernt.");
                RespawnListener.respawn(p);
            }
        }
        if (laserTag.spectators.contains(p.getName())) return;
        for (Entity entity: p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 100, 100, 100)) {
            if (!(entity instanceof Player)) continue;
            if (laserTag.roles.get(entity.getName()) == laserTag.roles.get(p.getName())) continue;
            if (laserTag.roles.get(entity.getName()) == LaserTag.ROLE.WAITING) continue;
            if (laserTag.spectators.contains(entity.getName())) continue;
            p.setCompassTarget(entity.getLocation());
            ItemMeta m = p.getInventory().getItem(8).getItemMeta();
            m.setDisplayName("§7"+entity.getName() + " §8[§6"+ new DecimalFormat("#.##").format(p.getLocation().distance(entity.getLocation()))+"m§8]");
            p.getInventory().getItem(8).setItemMeta(m);
            return;
        }
        p.getInventory().clear(8);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setLevel(0);
        e.setJoinMessage(null);
        p.setGameMode(GameMode.ADVENTURE);
        Main.plugin.getLaserTag().sendServerBanner(p);
        Main.plugin.getLaserTag().sendPlayingGamemode(p);
        if (p.isOp() && laserTag.game_developer_mode) {
            p.sendMessage(Main.prefix + "Das Plugin befindet sich im Entwickler-Modus.\n§7Der Name des Events wird in diesem Stadium versteckt.");
        }
        if(laserTag.game_gamestate.equals(LaserTag.GAME.WAITING)) {
            e.setJoinMessage("§8[§a+§8] §7"+ p.getName());
            e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0.5, 100, 0.5, 0, 15));
            if (Bukkit.getOnlinePlayers().size() == laserTag.allowed_players[0]) laserTag.countdown();
        } else {
            e.getPlayer().teleport(Main.plugin.getMapManager().getCurrentMap().getSpectatorSpawn());
            laserTag.spectators.add(p.getName());
        }
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        RespawnListener.defaultEquip(p);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> new ScoreboardManager().refreshBoard(), 3);
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
            if (Main.plugin.getMariaDB().getDatabase("Stats", "UUID", p.getUniqueId().toString(), "Games") == null) {
                Main.plugin.getMariaDB().update("INSERT INTO `Stats` (`UUID`,`Games`,`Wins`,`Loses`,`Kills`,`FinalKills`,`Deaths`,`FinalDeaths`) " +
                        "VALUES ('"+p.getUniqueId().toString()+"','0','0','0','0','0','0','0')");
            }
        });
    }

    @EventHandler
    public void onExplode(BlockPhysicsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void OnFood(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void OnFood(WeatherChangeEvent e) {
        e.setCancelled(true);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Main.plugin.getMapManager().votes.remove(p);
        e.setQuitMessage(null);
        laserTag.roles.remove(p.getName());
        if (laserTag.game_gamestate.equals(LaserTag.GAME.WAITING)) {
            e.setQuitMessage("§8[§c-§8] §7" + p.getName());
            if (Bukkit.getOnlinePlayers().size() - 1 == laserTag.allowed_players[0] - 1) laserTag.cancel_countdown(false);
            return;
        }

        if (!laserTag.spectators.contains(p.getName())) RespawnListener.final_death(p);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> new ScoreboardManager().refreshBoard());
        if (laserTag.roles.get(p.getName()) == LaserTag.ROLE.WAITING) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            new ScoreboardManager().updateSidebar();
            if (laserTag.game_gamestate != LaserTag.GAME.RUNNING && laserTag.game_gamestate != LaserTag.GAME.DEATHMATCH) return;
            if (laserTag.spectators.contains(p.getName())) return;
            MariaDB db = Main.plugin.getMariaDB();
            db.update("UPDATE `Stats` SET Loses='" + ((int) db.getDatabase("Stats", "UUID", p.getUniqueId().toString(), "Loses") + 1) + "' WHERE UUID='" + p.getUniqueId().toString() + "'");
            laserTag.check_end();
        }, 3);
    }
}
