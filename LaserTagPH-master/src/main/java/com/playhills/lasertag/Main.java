package com.playhills.lasertag;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.playhills.lasertag.commands.staff_commands.*;
import com.playhills.lasertag.commands.user_commands.StatsCommand;
import com.playhills.lasertag.commands.user_commands.TopCommand;
import com.playhills.lasertag.listener.*;
import com.playhills.lasertag.manager.map.MapManager;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.manager.sql.MariaDB;
import com.playhills.lasertag.manager.teams.ScoreboardManager;
import com.playhills.lasertag.manager.teams.TeamListener;
import com.playhills.lasertag.manager.weapons.WeaponManager;
import de.fkfabian.api.API;
import de.fkfabian.api.mc.cloud.SpigotCloud;
import de.fkfabian.api.util.MongoAPI;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static String prefix = "§8[§3Laser§bTag§8] §7";
    public static String playerOnly = prefix + "Dieser Befehl ist nur für Spieler verfügbar.";
    public static String noPerms = prefix + "§cDeine Berechtigungen sind unzureichend.";
    private LaserTag laserTag;
    private WeaponManager weaponManager;
    private SpecialManager specialManager;
    private MariaDB mariaDB;
    private MapManager mapManager;

    @Override
    public void onEnable() {
        API.setTablisthandling(true);
        API.setTablist("");
        API.setChatEnabled(false);
        plugin = this;
        laserTag = new LaserTag();
        weaponManager = new WeaponManager();
        specialManager = new SpecialManager();
        mapManager = new MapManager();
        if (laserTag.game_developer_mode) prefix = "§8[§4§k12345678§8] §7";

        mariaDB = new MariaDB("45.83.244.88", 3306, "dev_user", "yloRWVK5d1l5q%Q*o25i");
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("-----------------------\n\n\nLaserTag ran into a fatal error!\n-----------------------\n\n\n[LaserTag] Disabling plugin\n\n\n\n-----------------------");
            onDisable();
            return;
        }
        mariaDB.createTable();

        Bukkit.getPluginManager().registerEvents(new TeamListener(), this);
        Bukkit.getPluginManager().registerEvents(new GlobalListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShootListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpectatorListener(), this);
        Bukkit.getPluginManager().registerEvents(specialManager, this);
        Bukkit.getPluginManager().registerEvents(mapManager, this);

        getCommand("countdown").setExecutor(new CountdownCommand());
        getCommand("cancelcountdown").setExecutor(new CancelCountdownCommand());
        getCommand("start").setExecutor(new StartCommand());
        getCommand("shutdown").setExecutor(new ShutdownCommand());
        getCommand("cancelshutdown").setExecutor(new CancelShutdownCommand());
        getCommand("item").setExecutor(new ItemCommand());
        getCommand("top").setExecutor(new TopCommand());
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("reset").setExecutor(new ResetCommand());
        getCommand("forcemap").setExecutor(new ForcemapCommand());

        laserTag.cancel_countdown(true);
        new ScoreboardManager().refreshBoard();
        Bukkit.getOnlinePlayers().forEach(RespawnListener::defaultEquip);

        applyGamerules(Bukkit.getWorld("world"));
        Bukkit.getWorld("world").getWorldBorder().setSize(200);
        Bukkit.getWorld("world").getWorldBorder().setWarningTime(0);
        Bukkit.getWorld("world").getWorldBorder().setCenter(0.5, 0.5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (SpigotCloud.getConfiguration() == null) return;
                Document document = new Document("server", SpigotCloud.servername);
                document.append("status", laserTag.game_gamestate.name());
                MongoAPI.getCollection("lasertag_servers").insertOne(document);
                cancel();
                updatePlayerCount();
            }
        }.runTaskTimer(this, 0L, 5L);
    }

    private void updatePlayerCount() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            MongoAPI.getCollection("lasertag_servers").updateOne(Filters.eq("server", SpigotCloud.servername),
                    Updates.combine(
                            Updates.set("online", Bukkit.getOnlinePlayers().size()),
                            Updates.set("status", laserTag.game_gamestate.name())
                    )
            );
        }, 0, 20l);
    }

    public void applyGamerules(World w) {
        w.setGameRuleValue("doDaylightCycle", "false");
        w.setGameRuleValue("doEntityDrops", "false");
        w.setGameRuleValue("doFireTick", "false");
        w.setGameRuleValue("doMobLoot", "false");
        w.setGameRuleValue("doMobSpawning", "false");
        w.setGameRuleValue("doTileDrops", "false");
        w.setGameRuleValue("keepInventory", "true");
        w.setGameRuleValue("logAdminCommands", "false");
        w.setGameRuleValue("mobGriefing", "false");
        w.setGameRuleValue("naturalRegeneration", "false");
        w.setGameRuleValue("randomTickSpeed", "0");
        w.setTime(6000);
        w.setThundering(false);
        w.setStorm(false);
    }

    @Override
    public void onDisable() {
        MongoAPI.getCollection("lasertag_servers").deleteMany(Filters.eq("server", SpigotCloud.servername));
        Bukkit.getOnlinePlayers().forEach( all -> all.kickPlayer("shutting down"));
        Bukkit.getWorlds().forEach(worlds -> worlds.getEntities().forEach(Entity::remove));
        try {
            Bukkit.unloadWorld("LaserTag", false);
            String name = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            FileUtils.deleteDirectory(new File(name.substring(0, name.length() - ((name.split("/")[name.split("/").length-1]).length() + name.split("/")[name.split("/").length-2].length()+1))+"/LaserTag"));
        } catch (Exception ingore) {}
    }

    public SpecialManager getSpecialManager() {
        return specialManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public LaserTag getLaserTag() { return laserTag; }
    public WeaponManager getWeaponManager() { return weaponManager; }

    public MariaDB getMariaDB() {
        return mariaDB;
    }
}