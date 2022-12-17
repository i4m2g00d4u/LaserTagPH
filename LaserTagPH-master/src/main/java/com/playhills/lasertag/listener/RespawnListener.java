package com.playhills.lasertag.listener;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.specials.list.Shield;
import com.playhills.lasertag.manager.sql.MariaDB;
import com.playhills.lasertag.manager.teams.ScoreboardManager;
import com.playhills.lasertag.manager.teams.TeamSelector;
import com.playhills.lasertag.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;

public class RespawnListener implements Listener {

    public static ArrayList<String> respawning = new ArrayList<>();
    public static HashMap<Player, Integer> killstreak = new HashMap<>();
    public static void respawn(Player p) {
        LaserTag laserTag = Main.plugin.getLaserTag();
        respawning.add(p.getName());
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> respawning.remove(p.getName()),20*3);
        if (laserTag.roles.get(p.getName()) == LaserTag.ROLE.PURPLE) {
            if (laserTag.team_lives[0] == 0) {
                final_death(p);
                return;
            }
            laserTag.team_lives[0]--;
            p.teleport(Main.plugin.getMapManager().getCurrentMap().getRandomPurpleSpawn());
            if (laserTag.team_lives[0] == 0) {
                Bukkit.getOnlinePlayers().forEach(all -> {
                    all.sendMessage(Main.prefix + "Das §5Lila §7Team wird nicht länger wiederbelebt!");
                    all.playSound(all.getLocation(), Sound.EXPLODE, 1, 0.1f);
                });
            }
        } else if (laserTag.roles.get(p.getName()) == LaserTag.ROLE.YELLOW) {
            if (laserTag.team_lives[1] == 0) {
                final_death(p);
                return;
            }
            laserTag.team_lives[1]--;
            p.teleport(Main.plugin.getMapManager().getCurrentMap().getRandomYellowSpawn());
            if (laserTag.team_lives[1] == 0) {
                Bukkit.getOnlinePlayers().forEach(all -> {
                    all.sendMessage(Main.prefix + "Das §eGelbe §7Team wird nicht länger wiederbelebt!");
                    all.playSound(all.getLocation(), Sound.EXPLODE, 1, 0.1f);
                });
            }
        } else {
            p.teleport(Main.plugin.getMapManager().getCurrentMap().getSpectatorSpawn());
        }
        new ScoreboardManager().updateSidebar();
        RespawnListener.defaultEquip(p);
    }

    public static void defaultEquip(Player p) {
        LaserTag laserTag = Main.plugin.getLaserTag();
        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        if (laserTag.game_gamestate == LaserTag.GAME.WAITING) {
            p.getInventory().setItem(4, new TeamSelector().bed);
            if (Main.plugin.getMapManager().getCurrentMap() == null) p.getInventory().setItem(0, new ItemBuilder(Material.BOOK).withName("§cMap").toItemStack());
        } else if (laserTag.game_gamestate == LaserTag.GAME.RUNNING || laserTag.game_gamestate == LaserTag.GAME.DEATHMATCH) {
            if (laserTag.spectators.contains(p.getName())) {
                p.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).withName("§eKompass").withLores("","§7Teleportiere dich zu Spielern.").setEnch(true).toItemStack());
            } else {
                p.getInventory().addItem(Main.plugin.getWeaponManager().getPistol().getItem());
                p.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).withName("§7Atzenhelm").withLores("","§7Ein stylishes Accessoire für jeden Kämpfer!").withColor(laserTag.roles.get(p.getName()) == LaserTag.ROLE.PURPLE ? Color.PURPLE : Color.YELLOW).setUnbreakable(true).toItemStack());
                p.getInventory().setItem(8, new ItemBuilder(Material.COMPASS).withName("§eKompass").withLores("","§7Zeigt auf den nächsten Gegner.").setEnch(true).toItemStack());
            }
        }

    }

    public static void kill(Player p, Player Killer, String reason) {
        if (p.getGameMode()!= GameMode.ADVENTURE) return;
        LaserTag laserTag = Main.plugin.getLaserTag();

        Killer.playSound(Killer.getLocation(), Sound.ORB_PICKUP, 1 ,1);
        if (((Shield)Main.plugin.getSpecialManager().getSpecialByName("Schild")).shield.contains(p.getName())) {
            Killer.playSound(Killer.getLocation(), Sound.ANVIL_BREAK, 1 ,1);
            p.playSound(p.getLocation(), Sound.ANVIL_BREAK, 1 ,1);
            p.sendMessage(Main.prefix + "Dein Schild wurde durch "+(laserTag.roles.get(p.getName()) != LaserTag.ROLE.PURPLE ? "§5" : "§e")+Killer.getName() +" §7zerstört! §8(§7"+reason+"§8)");
            Killer.sendMessage(Main.prefix + "Du hast das Schild von "+(laserTag.roles.get(p.getName()) != LaserTag.ROLE.PURPLE ? "§e" : "§5") + p.getName()+" §7zerstört! §8(§7"+reason+"§8)");
            ((Shield)Main.plugin.getSpecialManager().getSpecialByName("Schild")).shield.remove(p.getName());
            p.getInventory().setChestplate(null);
            return;
        }

        killstreak.put(Killer, killstreak.containsKey(Killer) ? killstreak.get(Killer)+1 : 1);
        killstreak.put(p, 0);
        laserTag.kills.putIfAbsent(Killer.getName(), 0);
        laserTag.kills.put(Killer.getName(), laserTag.kills.get(Killer.getName())+1);

        if(killstreak.containsKey(Killer) && killstreak.get(Killer) % 5 == 0) {
            Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "§a" + Killer.getName() + " §7hat eine §a" + killstreak.get(Killer) + "er §7Killstreak!"));
        }

        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
        boolean respawn_able = laserTag.roles.get(p.getName()) == LaserTag.ROLE.PURPLE ? laserTag.team_lives[0] > 0 : laserTag.team_lives[1] > 0;
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
            MariaDB db = Main.plugin.getMariaDB();
            String r = respawn_able ? "" : "Final";
            db.update("UPDATE `Stats` SET "+r+"Kills='"+((int)db.getDatabase("Stats","UUID",Killer.getUniqueId().toString(),r+"Kills")+1)+"' WHERE UUID='"+Killer.getUniqueId().toString()+"'");
            db.update("UPDATE `Stats` SET "+r+"Deaths='"+((int)db.getDatabase("Stats","UUID",p.getUniqueId().toString(),r+"Deaths")+1)+"' WHERE UUID='"+p.getUniqueId().toString()+"'");
        });
        laserTag.deaths.putIfAbsent(p.getName(), 0);
        laserTag.deaths.put(p.getName(), laserTag.deaths.get(p.getName())+1);
        respawn(p);
        Killer.sendMessage(Main.prefix + "Du hast "+(laserTag.roles.get(p.getName()) != LaserTag.ROLE.PURPLE ? "§e" : "§5") + p.getName()+" §7"+(respawn_able ? "getötet!" : "vernichtet!")+" §8(§7"+reason+"§8)");
        p.sendMessage(Main.prefix + (respawn_able ? "Getötet" : "Vernichtet") + " durch "+(laserTag.roles.get(p.getName()) != LaserTag.ROLE.PURPLE ? "§5" : "§e")+Killer.getName()+"§7! §8(§7"+reason+"§8)");
        p.playSound(p.getLocation(), "mob.wither.hurt", 1 ,0.1F);
    }

    public static void final_death(Player p) {
        LaserTag laserTag = Main.plugin.getLaserTag();
        for (Player all: Bukkit.getOnlinePlayers()) {
            if (!laserTag.spectators.contains(all.getName())) continue;
            if (all.getSpectatorTarget() == null || all.getSpectatorTarget() != p) continue;
            all.setSpectatorTarget(null);
        }
        laserTag.spectators.add(p.getName());
        p.teleport(Main.plugin.getMapManager().getCurrentMap().getSpectatorSpawn());
        int purple = 0; int yellow = 0;
        for (LaserTag.ROLE s: laserTag.roles.values()) {
            purple += s == LaserTag.ROLE.PURPLE ? 1 : 0;
            yellow += s == LaserTag.ROLE.YELLOW ? 1 : 0;
        }
        for (String s: laserTag.spectators) {
            if (laserTag.roles.get(s) == LaserTag.ROLE.PURPLE) {
                purple--;
            } else if (laserTag.roles.get(s) == LaserTag.ROLE.YELLOW) {
                yellow--;
            }
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == p) continue;
            if (laserTag.roles.get(p.getName()) == LaserTag.ROLE.PURPLE) {
                onlinePlayer.sendMessage(Main.prefix + "§5" + p.getName() + "§7 wurde vernichtet! ("+purple+" verbleibend)");
            } else {
                onlinePlayer.sendMessage(Main.prefix + "§e" + p.getName() + "§7 wurde vernichtet! ("+yellow+" verbleibend)");
            }
        }
        defaultEquip(p);
        new ScoreboardManager().refreshBoard();
        if (laserTag.game_gamestate != LaserTag.GAME.RUNNING && laserTag.game_gamestate != LaserTag.GAME.DEATHMATCH) return;
        laserTag.check_end();
    }
}
