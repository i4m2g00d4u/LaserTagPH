package com.playhills.lasertag.manager.teams;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;

public class ScoreboardManager {

    LaserTag laserTag = Main.plugin.getLaserTag();
    public void refreshBoard(Player p) {
        Scoreboard board = p.getScoreboard() == null ? Bukkit.getScoreboardManager().getNewScoreboard() : p.getScoreboard();

        Bukkit.getOnlinePlayers().forEach(p::showPlayer);
        for (Player all : Bukkit.getOnlinePlayers()) {

            laserTag.roles.putIfAbsent(all.getName(), LaserTag.ROLE.WAITING);
            String prefix = "";
            String prio = "";
            String color = "";

            if (!laserTag.spectators.contains(p.getName()) && p.getGameMode() != GameMode.SPECTATOR && p.getGameMode() != GameMode.CREATIVE) p.setAllowFlight(false);

            p.showPlayer(all);

            if (laserTag.spectators.contains(all.getName())) {
                prefix = "§7Spec §8| ";
                prio = "z";
                color = "7";
                if (!all.getAllowFlight()) all.setAllowFlight(true);
                if (!laserTag.spectators.contains(p.getName())) p.hidePlayer(all);
            } else if (laserTag.roles.get(all.getName()) == LaserTag.ROLE.YELLOW) {
                prefix = "§eGelb §8| ";
                prio = "b";
                color = "e";
            } else if (laserTag.roles.get(all.getName()) == LaserTag.ROLE.PURPLE) {
                prefix = "§5Lila §8| ";
                prio = "a";
                color = "5";
            } else if (laserTag.roles.get(all.getName()) == LaserTag.ROLE.WAITING) {
                prefix = "§7Wartend §8| ";
                prio = "z";
                color = "7";
            }
            Team team = new createTeam(prefix, prio, board).setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS).setColor(color).complete();

            String c = "§" + color.replace("§", "").replace("&", "");
            all.setPlayerListName(prefix + c + all.getName());
            team.addEntry(all.getName());
        }

        updateSidebar(p);

        p.setScoreboard(board);
    }

    public void updateSidebar(Player p) {
        Scoreboard board = p.getScoreboard();
        try {
            board.getObjective(p.getName()).unregister();
        } catch (Exception ignore) {}
        Objective o = board.registerNewObjective(p.getName(), "dummy");
        o.setDisplayName(Main.prefix.split("\\[")[1].split("]")[0]);

        if (laserTag.game_gamestate == LaserTag.GAME.WAITING) {
            o.getScore("§1").setScore(15);
            if (Bukkit.getOnlinePlayers().size() < laserTag.allowed_players[0]) {
                o.getScore("§7Warte auf §6" + (laserTag.allowed_players[0] - Bukkit.getOnlinePlayers().size())+ "§7 Spieler").setScore(14);
            } else {
                o.getScore("§7Platz für weitere §6" + (laserTag.allowed_players[1] - Bukkit.getOnlinePlayers().size())+"§7 Spieler").setScore(14);
            }
            o.getScore("§2").setScore(13);
            int purple = 0; int yellow = 0;
            for (LaserTag.ROLE s: laserTag.roles.values()) {
                purple += s == LaserTag.ROLE.PURPLE ? 1 : 0;
                yellow += s == LaserTag.ROLE.YELLOW ? 1 : 0;
            }
            o.getScore("§5Team Lila§8: §7"+purple+"§8/§7"+laserTag.allowed_players[1]/2).setScore(12);
            o.getScore("§eTeam Gelb§8: §7"+yellow+"§8/§7"+laserTag.allowed_players[1]/2).setScore(11);
            o.getScore("§3").setScore(10);
        } else if (laserTag.game_gamestate == LaserTag.GAME.RUNNING || laserTag.game_gamestate == LaserTag.GAME.DEATHMATCH) {
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
            o.getScore("§1").setScore(15);
            o.getScore("§7Nächstes Event").setScore(14);
            if (laserTag.game_gamestate == LaserTag.GAME.RUNNING) {
                int sek = laserTag.playINT[0]-laserTag.playINT[1];
                int min = 0;
                while (sek >= 60) {
                    min++;
                    sek-=60;
                }
                String time = (min > 0 ? min+"m " : "") + sek+"s";
                o.getScore("§8➥ §6"+time + " §8(§cDeathmatch§8)").setScore(13);
            } else {
                int sek = laserTag.playINT[0];
                int min = 0;
                while (sek >= 60) {
                    min++;
                    sek-=60;
                }
                String time = (min > 0 ? min+"m " : "") + sek+"s";
                o.getScore("§8➥ §6"+time + " §8(§cSpielende§8)").setScore(13);
            }

            o.getScore("§2").setScore(12);
            if (laserTag.team_lives[0]>0) {
                o.getScore("§5Lila Leben§8: §7" + laserTag.team_lives[0]).setScore(11);
            } else {
                o.getScore("§5Lila Spieler§8: §d" + purple + "§8/§d" + laserTag.player_start_count[0]).setScore(11);
            }
            if (laserTag.team_lives[1]>0) {
                o.getScore("§eGelbe Leben§8: §7" + laserTag.team_lives[1]).setScore(10);
            } else {
                o.getScore("§eGelbe Spieler§8: §6" + yellow + "§8/§6" + laserTag.player_start_count[1]).setScore(10);
            }
            o.getScore("§3").setScore(9);
            o.getScore("§7Gespielte Map§8").setScore(8);
            o.getScore("§8➥ §6"+Main.plugin.getMapManager().getCurrentMap().getName()).setScore(7);
            o.getScore("§4").setScore(6);
        } else if (laserTag.game_gamestate == LaserTag.GAME.ENDING && laserTag.roles.get(p.getName()) != LaserTag.ROLE.WAITING) {
            o.getScore("§1").setScore(15);
            o.getScore("§7Deine Kills§8: §9"+(laserTag.kills.get(p.getName()) == null ? 0 : laserTag.kills.get(p.getName()))).setScore(14);
            o.getScore("§7Deine Tode§8: §9"+(laserTag.deaths.get(p.getName()) == null ? 0 : laserTag.deaths.get(p.getName()))).setScore(13);
            o.getScore("§7Deine KDR§8: §3"+new DecimalFormat("#.##").format(laserTag.kdr(p.getName()))).setScore(12);
            o.getScore("§3").setScore(11);
        }

        o.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void updateSidebar() {
        Bukkit.getOnlinePlayers().forEach(this::updateSidebar);
    }

    public void refreshBoard() {
        Bukkit.getOnlinePlayers().forEach(this::refreshBoard);
    }
}

class createTeam {

    private Team team;
    private String color;
    private String prefix;

    public createTeam(String TeamName, String TeamPriority, Scoreboard board) {
        final String s = "§" + TeamPriority.replace("§", "").replace("&", "") + TeamName;
        try {
            this.team = board.registerNewTeam(s);
        } catch (Exception ignore) {}
        this.team = board.getTeam(s);
    }

    public createTeam setColor(String TeamColor) {
        this.color = "§" + TeamColor.replace("§", "").replace("&", "");
        return this;
    }

    public createTeam setPrefix(String TeamPrefix) {
        this.prefix = TeamPrefix;
        return this;
    }

    public createTeam setNameTagVisibility(NameTagVisibility ntv) {
        this.team.setNameTagVisibility(ntv);
        return this;
    }

    public createTeam setSuffix(String TeamSuffix) {
        team.setSuffix(TeamSuffix);
        return this;
    }

    public createTeam friendlyFire(boolean allowed) {
        team.setAllowFriendlyFire(allowed);
        return this;
    }

    public createTeam setFriendlyVisibility(boolean allowed) {
        team.setCanSeeFriendlyInvisibles(allowed);
        return this;
    }

    public Team complete() {
        String p = "";
        p += this.prefix != null ? this.prefix : "";
        p += this.color != null ? this.color : "";
        team.setPrefix(p);

        return team;
    }
    //PUSH ERROR

}