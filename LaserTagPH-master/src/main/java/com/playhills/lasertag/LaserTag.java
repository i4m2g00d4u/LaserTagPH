package com.playhills.lasertag;

import com.google.gson.JsonObject;
import com.playhills.lasertag.listener.RespawnListener;
import com.playhills.lasertag.manager.CloudManager;
import com.playhills.lasertag.manager.map.Map;
import com.playhills.lasertag.manager.sql.MariaDB;
import com.playhills.lasertag.manager.teams.ScoreboardManager;
import com.playhills.lasertag.utils.LabyModProtocol;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LaserTag {

    public LaserTag() {}

    public boolean game_developer_mode = false;
    public GAME game_gamestate = GAME.WAITING;

    public int[] allowed_players = new int[]{4,16};
    public int[] player_start_count = new int[]{0,0};
    public int[] team_lives = new int[]{0,0};

    public HashMap<String, Integer> punkte = new HashMap<>();
    public HashMap<String, Integer> kills = new HashMap<>();
    public HashMap<String, Integer> deaths = new HashMap<>();
    public ArrayList<String> spectators = new ArrayList<>();
    public HashMap<String, ROLE> roles = new HashMap<>();
    private final double lebens_wert = 7.44;

    public enum GAME {
        RUNNING, DEATHMATCH, WAITING, ENDING
    }
    public enum ROLE {
        YELLOW, PURPLE, WAITING
    }

    public boolean is_allowed_to_join(ROLE team) {
        int purple = 0; int yellow = 0;
        for (ROLE s: roles.values()) {
            purple += s == ROLE.PURPLE ? 1 : 0;
            yellow += s == ROLE.YELLOW ? 1 : 0;
        }
        if (purple == 0 && yellow == 0) return true;
        if (purple == yellow) return true;
        if (team == ROLE.PURPLE) return purple < yellow;
        return yellow < purple;
    }

    public void countdown() {
        countTASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
            countINT--;
            Bukkit.getOnlinePlayers().forEach(all -> all.setLevel(countINT));
            if (countINT > 30) return;
            switch (countINT) {
                case 1:
                    Bukkit.getOnlinePlayers().forEach(all -> {
                        all.sendMessage(Main.prefix + "Das Spiel beginnt in §a" + countINT + "§7 Sekunde!");
                        all.playSound(all.getLocation(), Sound.ORB_PICKUP, 1, 1.4f);
                    });
                    break;
                case 10:
                    Bukkit.getOnlinePlayers().forEach(all -> {
                        all.getInventory().clear(0);
                        all.closeInventory();
                    });
                    if (Main.plugin.getMapManager().getCurrentMap() != null) {
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Gespielte Map§8: §e"+Main.plugin.getMapManager().getCurrentMap().getName()));
                        return;
                    }
                    Map map = Main.plugin.getMapManager().getVotedMap();
                    Main.plugin.getMapManager().forceMap(map);
                    Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Gespielte Map§8: §e"+map.getName()));
                case 30:
                case 5:
                case 4:
                case 3:
                case 2:
                    Bukkit.getOnlinePlayers().forEach(all -> {
                        all.sendMessage(Main.prefix + "Das Spiel beginnt in §a" + countINT + "§7 Sekunden!");
                        all.playSound(all.getLocation(), Sound.ORB_PICKUP, 1, 1.4f);
                    });
                    break;
            }
            if (countINT != 0) return;
            cancel_countdown(true);
            force_start();
        },0,20);
    }

    public void sendActionBar(Player p, String msg) {
        IChatBaseComponent bar = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat chat = new PacketPlayOutChat(bar, (byte)2);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(chat);
    }

    public void sendServerBanner(Player player) {
        JsonObject object = new JsonObject();
        object.addProperty("url", "https://cdn.discordapp.com/attachments/1012843841117761638/1038232106423308318/Lasertag.png");
        LabyModProtocol.sendClientMessage(player, "server_banner", object);
    }

    public void sendPlayingGamemode(Player player) {
        if (game_developer_mode) return;
        JsonObject obj = new JsonObject();
        obj.addProperty("show_gamemode", true);
        obj.addProperty("gamemode_name", "§ePlayHills.eu §8» §3Laser§bTag");
        //obj.addProperty("gamemode_name", "§ePlayHills.eu §8» §3Schlubbi");
        LabyModProtocol.sendClientMessage(player, "server_gamemode", obj);
    }

    private int countTASK;
    public int countINT = 61;
    public void cancel_countdown(boolean cancelMessage) {
        Bukkit.getScheduler().cancelTask(countTASK);
        countINT = 61;
        Bukkit.getOnlinePlayers().forEach(all -> all.setLevel(0));
        if (cancelMessage) return;
        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Der Start wurde abgebrochen!"));
    }

    private int playTASK;
    public int[] playINT = new int[]{1200,600};
    public void cancel_playtime() {
        Bukkit.getScheduler().cancelTask(playTASK);
    }
    public void start_playtime() {
        playTASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
            new ScoreboardManager().updateSidebar();
            int time = playINT[0]-playINT[1];
            if (time >= 0) {
                switch (time) {
                    case 60:
                    case 30:
                    case 10:
                    case 3:
                    case 2:
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Das Deathmatch startet in §e"+time+" §7Sekunden."));
                        break;
                    case 1:
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Das Deathmatch startet in §e"+time+" §7Sekunde."));
                        break;
                    case 0:
                        Bukkit.getOnlinePlayers().forEach(all -> {
                        all.playSound(all.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                        all.sendTitle("§cDeathmatch","§7");
                        all.sendMessage(Main.prefix + "§c§lDEATHMATCH\n§eBeide Teams werden nicht länger wiederbelebt. Das letzte Lebende Team gewinnt das Spiel!");
                        game_gamestate = GAME.DEATHMATCH;
                        team_lives[0] = 0;
                        team_lives[1] = 0;
                        Main.plugin.getSpecialManager().cancelSpecial();
                    });
                        break;
                }
            } else {
                switch (playINT[0]) {
                    case 60:
                    case 30:
                    case 10:
                    case 3:
                    case 2:
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Das Spiel endet in §e"+playINT[0]+" §7Sekunden."));
                        break;
                    case 1:
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(Main.prefix + "Das Spiel endet in §e"+playINT[0]+" §7Sekunde."));
                        break;
                    case 0:
                        force_end(ROLE.WAITING);
                        return;
                }
            }
            new ScoreboardManager().updateSidebar();
            playINT[0]--;
        },0, 20);
    }

    private void everyone_teams() {
        for (Player all: Bukkit.getOnlinePlayers()) {
            if (roles.get(all.getName()) != ROLE.WAITING) continue;
            int purple = 0; int yellow = 0;
            for (ROLE s: roles.values()) {
                purple += s == ROLE.PURPLE ? 1 : 0;
                yellow += s == ROLE.YELLOW ? 1 : 0;
            }
            if (purple > yellow) {
                roles.put(all.getName(), ROLE.YELLOW);
            } else if (yellow > purple) {
                roles.put(all.getName(), ROLE.PURPLE);
            } else {
                roles.put(all.getName(), new Random().nextBoolean() ? ROLE.YELLOW : ROLE.PURPLE);
            }
        }
    }

    private void fair_teams() {
        int purple = 0; int yellow = 0;
        for (ROLE s: roles.values()) {
            purple += s == ROLE.PURPLE ? 1 : 0;
            yellow += s == ROLE.YELLOW ? 1 : 0;
        }
        while (purple > yellow+1) {
            ArrayList<String> players = new ArrayList<>();
            for (String s: roles.keySet()) {
                if (roles.get(s) != ROLE.PURPLE) continue;
                players.add(s);
            }
            String randomPlayer = players.get(new Random().nextInt(players.size()));
            roles.put(randomPlayer, ROLE.YELLOW);
            purple = 0; yellow = 0;
            for (ROLE s: roles.values()) {
                purple += s == ROLE.PURPLE ? 1 : 0;
                yellow += s == ROLE.YELLOW ? 1 : 0;
            }
        }
        while (yellow > purple+1) {
            ArrayList<String> players = new ArrayList<>();
            for (String s: roles.keySet()) {
                if (roles.get(s) != ROLE.YELLOW) continue;
                players.add(s);
            }
            String randomPlayer = players.get(new Random().nextInt(players.size()));
            roles.put(randomPlayer, ROLE.PURPLE);
            purple = 0; yellow = 0;
            for (ROLE s: roles.values()) {
                purple += s == ROLE.PURPLE ? 1 : 0;
                yellow += s == ROLE.YELLOW ? 1 : 0;
            }
        }
        new ScoreboardManager().refreshBoard();
    }

    public void force_start() {
        if (game_gamestate == GAME.RUNNING) return;
        this.game_gamestate = GAME.RUNNING;
        int i = 0;
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (spectators.contains(all.getName())) continue;
            i++;
        }
        i *= lebens_wert;
        i+=20;
        team_lives = new int[]{i, i};
        start_playtime();

        Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.plugin, () -> {
            for (Player all : Bukkit.getOnlinePlayers()) {
                MariaDB db = Main.plugin.getMariaDB();
                db.update("UPDATE `Stats` SET Games='" + ((int) db.getDatabase("Stats", "UUID", all.getUniqueId().toString(), "Games") + 1) + "' WHERE UUID='" + all.getUniqueId().toString() + "'");
            }
        });

        CloudManager.requestNewServer();
        everyone_teams();
        fair_teams();

        for (Player all : Bukkit.getOnlinePlayers()) {
            all.getInventory().setItem(0, Main.plugin.getWeaponManager().getPistol().getItem());
            if (roles.get(all.getName()) == ROLE.PURPLE) {
                player_start_count[0]++;
                all.teleport(Main.plugin.getMapManager().getCurrentMap().getRandomPurpleSpawn());
            } else if (roles.get(all.getName()) == ROLE.YELLOW) {
                player_start_count[1]++;
                all.teleport(Main.plugin.getMapManager().getCurrentMap().getRandomYellowSpawn());
            }
            all.closeInventory();
            all.playSound(all.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
            all.sendTitle("§e§l" + Main.plugin.getMapManager().getCurrentMap().getName(), "§7by §6" + Main.plugin.getMapManager().getCurrentMap().getAuthor());
            RespawnListener.defaultEquip(all);
        }
        if (player_start_count[0] > player_start_count[1]) {
            team_lives[1]+=lebens_wert;
        } else if (player_start_count[1] > player_start_count[0]) {
            team_lives[0]+=lebens_wert;
        }
        Main.plugin.getSpecialManager().spawnSpecialsQueue();
    }

    public void shutdown() {
        for (Entity e : Main.plugin.getMapManager().getCurrentMap().getMiddle().getWorld().getEntities()) {
            if (e instanceof Player) continue;
            e.remove();
        }
        if (Bukkit.getOnlinePlayers() != null) Bukkit.getOnlinePlayers().forEach(all -> all.kickPlayer("§cLaserTag forced shutdown"));
        CloudManager.killServerImmediately();
        Bukkit.getServer().shutdown();
    }

    public void cancel_shutdown() {
        Bukkit.getScheduler().cancelTask(shutdownTASK);
        shutdownINT = 16;
    }

    public void check_end() {
        if (game_gamestate != GAME.RUNNING && game_gamestate != GAME.DEATHMATCH) return;
        for (Entity eall: Main.plugin.getMapManager().getCurrentMap().getMiddle().getWorld().getEntities()) {
            if (eall instanceof Player) continue;
            eall.remove();
        }
        int purple = 0; int yellow = 0;
        for (LaserTag.ROLE s: roles.values()) {
            purple += s == LaserTag.ROLE.PURPLE ? 1 : 0;
            yellow += s == LaserTag.ROLE.YELLOW ? 1 : 0;
        }
        for (String s: spectators) {
            if (roles.get(s) == LaserTag.ROLE.PURPLE) {
                purple--;
            } else if (roles.get(s) == LaserTag.ROLE.YELLOW) {
                yellow--;
            }
        }

        if (purple == 0 && yellow == 0) {
            force_end(ROLE.WAITING);
        } else if (purple == 0) {
            force_end(ROLE.YELLOW);
        } else if (yellow == 0) {
            force_end(ROLE.PURPLE);
        }
    }
    public int shutdownINT = 16;
    private int shutdownTASK;
    public void force_end(ROLE winner) {
        this.game_gamestate = GAME.ENDING;
        cancel_playtime();
        Main.plugin.getSpecialManager().cancelSpecial();
        new ScoreboardManager().refreshBoard();
        MariaDB db = Main.plugin.getMariaDB();
        if (winner == ROLE.WAITING) {
            Bukkit.getOnlinePlayers().forEach(all -> {
                all.sendTitle("§7UNENTSCHIEDEN!", "§7NIEMAND GEWINNT!");
                if (roles.get(all.getName()) != ROLE.WAITING) db.update("UPDATE `Stats` SET Loses='" + ((int) db.getDatabase("Stats", "UUID", all.getUniqueId().toString(), "Loses") + 1) + "' WHERE UUID='" + all.getUniqueId().toString() + "'");
            });
        } else if (winner == ROLE.PURPLE) {
            Bukkit.getOnlinePlayers().forEach(all -> {
                all.sendTitle("§5TEAM LILA", "§7hat das Spiel gewonnen!");
                String action = roles.get(all.getName()) == ROLE.PURPLE ? "Wins" : "Loses";
                if (roles.get(all.getName()) != ROLE.WAITING) db.update("UPDATE `Stats` SET "+action+"='" + ((int) db.getDatabase("Stats", "UUID", all.getUniqueId().toString(), action) + 1) + "' WHERE UUID='" + all.getUniqueId().toString() + "'");
            });
        } else {
            Bukkit.getOnlinePlayers().forEach(all -> {
                all.sendTitle("§eTEAM GELB", "§7hat das Spiel gewonnen!");
                String action = roles.get(all.getName()) == ROLE.YELLOW ? "Wins" : "Loses";
                if (roles.get(all.getName()) != ROLE.WAITING) db.update("UPDATE `Stats` SET " + action + "='" + ((int) db.getDatabase("Stats", "UUID", all.getUniqueId().toString(), action) + 1) + "' WHERE UUID='" + all.getUniqueId().toString() + "'");
            });
        }
        for (Player all : Bukkit.getOnlinePlayers()) {
            Bukkit.getOnlinePlayers().forEach(all1 -> all1.showPlayer(all));
            all.playSound(all.getLocation(), Sound.ENDERDRAGON_DEATH, 1, 0.5f);

            send_statistics(all);

            if (roles.get(all.getName()) != winner) continue;
            punkte.putIfAbsent(all.getName(), 0);
            punkte.put(all.getName(),punkte.get(all.getName())+30);
        }
        shutdownTASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
            shutdownINT--;
            if (shutdownINT > 5) return;
            Bukkit.getOnlinePlayers().forEach(this::moveHub);
            if (shutdownINT != 0) return;
            shutdown();
        },0,20);
    }

    private void send_statistics(Player receiver) {
        if (kills.isEmpty()) return;
        String s = "";
        for (String s1: top_killer()) {
            if (s1.equals(top_killer().get(top_killer().size() - 1))) {
                s+="§6"+s1;
                continue;
            }
            s+="§6"+s1+"§8, ";
        }
        String s2 = "";
        for (String s3: top_kill_death_rate()) {
            if (s3.equals(top_kill_death_rate().get(top_kill_death_rate().size() - 1))) {
                s2+="§3"+s3;
                continue;
            }
            s2+="§3"+s3+"§8, ";
        }
        receiver.sendMessage("\n\n§8§m           §8[§a§lSPIEL STATISTIKEN§8]§m           ");
        receiver.sendMessage("\n§8>   §eMeiste Kills§8: "+s+"§8 - §6"+kills.get(top_killer().get(0))+"\n");
        receiver.sendMessage("§8>   §bBeste KDR§8: "+s2+"§8 - §3"+new DecimalFormat("#.##").format(kdr(top_kill_death_rate().get(0))));
        receiver.sendMessage("\n\n§8§m           §8[§a§lSPIEL STATISTIKEN§8]§m           \n ");
    }

    private ArrayList<String> top_killer() {
        ArrayList<String> top = new ArrayList<>();
        for (String s: kills.keySet()) {
            if (top.isEmpty()) {
                top.add(s);
                continue;
            }
            if (kills.get(top.get(0)) > kills.get(s)) {
                continue;
            }
            if (kills.get(top.get(0)) == kills.get(s)) {
                top.add(s);
                continue;
            }
            top.clear();
            top.add(s);
        }
        return top;
    }

    private ArrayList<String> top_kill_death_rate() {
        ArrayList<String> kdr = new ArrayList<>();

        for (String s: kills.keySet()) {
            if (kdr.isEmpty()) {
                kdr.add(s);
                continue;
            }
            if (kdr(kdr.get(0)) > kdr(s)) continue;
            if (kdr(kdr.get(0)) == kdr(s)) {
                kdr.add(s);
                continue;
            }
            kdr.clear();
            kdr.add(s);
        }

        return kdr;
    }

    public double kdr(String s) {
        int kills = this.kills.getOrDefault(s, 0);
        int deaths = this.deaths.getOrDefault(s, 0);

        if (kills == 0 && deaths == 0) return 0;
        if (kills == 0) return 0;
        if (deaths == 0) return kills;
        if (kills == deaths) return 1;
        return (double)kills/deaths;
    }

    public void moveHub(Player p) {
        //TODO SEND LOBBY
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            if (p == null) return;
            p.kickPlayer(Main.prefix +"Bei deiner Verbindung zur Lobby ist ein Fehler aufgetreten.\n"+Main.prefix + "Du wirst zum Fallback Server verbunden.");
        },20*3);
    }
}
