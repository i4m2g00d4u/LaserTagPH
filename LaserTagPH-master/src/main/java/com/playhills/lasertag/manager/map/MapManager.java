package com.playhills.lasertag.manager.map;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.InventoryManager;
import com.playhills.lasertag.utils.ItemBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class MapManager implements Listener {

    private List<Map> maps;

    private String pathMaps;
    private String pathServer;

    private Map currentMap = null;

    public HashMap<Player, Map> votes = new HashMap<Player, Map>();
    public MapManager() {
        maps = new ArrayList<Map>();
        init();
        loadMaps();
    }

    public Map getVotedMap() {
        if(currentMap != null) return currentMap;
        if(votes.isEmpty()) return maps.get(new Random().nextInt(maps.size()));
        if(votes.size() == 1) return (Map)votes.values().toArray()[0];

        HashMap<Map, Integer> mv = new HashMap<>();

        for(Player p : votes.keySet()) {
            mv.put(votes.get(p), (mv.containsKey(votes.get(p)) ? mv.get(votes.get(p))+1 : 1));
        }
        int maxValueInMap = (Collections.max(mv.values()));

        ArrayList<Map> draw = new ArrayList<>();

        for(Entry<Map, Integer> entry : mv.entrySet()) {
            if(entry.getValue() == maxValueInMap) {
                draw.add(entry.getKey());
            }
        }
        return draw.get(new Random().nextInt(draw.size()));
    }

    private void loadMaps() {

        File[] listOfFiles = new File(pathMaps + "/Maps").listFiles();

        for(File f : listOfFiles) {
            if(!f.isDirectory()) continue;
            File configFile = new File(f.getPath() + "/mapconfig.yml");
            if(!configFile.exists()) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            Map m = new Map(Material.valueOf(config.getString("Map.material")), config.getString("Map.name"), config.getString("Map.author"));

            maps.add(m);
        }
    }

    public void forceMap(Map m) {
        if(!Main.plugin.getLaserTag().game_gamestate.equals(LaserTag.GAME.WAITING)) return;
        loadMap(m.getName());
        currentMap = m;
    }

    public void forceMap(String name) {
        if(!Main.plugin.getLaserTag().game_gamestate.equals(LaserTag.GAME.WAITING)) return;

        Map m = getMapByName(name);
        loadMap(name);
        if(m == null) return;

        currentMap = m;
    }

    private void init() {
        String name = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        pathMaps = name.substring(0, name.length() - (name.split("/")[name.split("/").length-1]).length()) + "Lasertag";
        pathServer = name.substring(0, name.length() - ((name.split("/")[name.split("/").length-1]).length() + name.split("/")[name.split("/").length-2].length()+1));

        File f = new File(pathMaps + "/Maps");
        if(!f.exists()) {
            f.mkdirs();
        }

        File f1 = new File(pathServer + "/LaserTag");
        if(!f1.exists()) {
            f1.mkdirs();
        }
    }

    public void loadMap(String name) {
        if(Bukkit.getWorld("Lasertag") != null) return;

        try {
            FileUtils.copyDirectory(new File(pathMaps + "/Maps/" + name), new File(pathServer + "/LaserTag"));
        } catch (IOException e) {
            Bukkit.broadcastMessage(Main.plugin + "§7Es ist fataler Fehler aufgetreten. Die Map §e" + maps.get(0).getName() + " §7wird vorbereitet.");
            loadMap(maps.get(0).getName());
            return;
        }

        Map map = getMapByName(name);

        WorldCreator worldCreator = new WorldCreator("LaserTag");
        World laserTag = worldCreator.createWorld();

        File f = new File(pathMaps + "/Maps/" + name+ "/mapconfig.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        ArrayList<Location> purpleSpawns = new ArrayList<>();
        for(String a : config.getConfigurationSection("Map.location.purple").getKeys(false)) {
            Location purple = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.purple." + a + ".x"), (float)config.getDouble("Map.location.purple." + a + ".y"), (float)config.getDouble("Map.location.purple." + a + ".z"), (float)config.getDouble("Map.location.purple." + a + ".yaw"), (float)config.getDouble("Map.location.purple." + a + ".pitch"));
            purpleSpawns.add(purple);
        }

        ArrayList<Location> yellowSpawns = new ArrayList<>();
        for(String a : config.getConfigurationSection("Map.location.yellow").getKeys(false)) {
            Location yellow = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.yellow." + a + ".x"), (float)config.getDouble("Map.location.yellow." + a + ".y"), (float)config.getDouble("Map.location.yellow." + a + ".z"), (float)config.getDouble("Map.location.yellow." + a + ".yaw"), (float)config.getDouble("Map.location.yellow." + a + ".pitch"));
            yellowSpawns.add(yellow);
        }

        map.setPurpleSpawns(purpleSpawns);
        map.setYellowSpawns(yellowSpawns);

        if(config.contains("Map.location.middle")) {
            Location middle = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.middle.x"), (float)config.getDouble("Map.location.middle.y"), (float)config.getDouble("Map.location.middle.z"));
            map.setMiddle(middle);
        }

        if(config.contains("Map.location.spectator")) {
            Location spectator = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.spectator.x"), (float)config.getDouble("Map.location.spectator.y"), (float)config.getDouble("Map.location.spectator.z"),(float)config.getDouble("Map.location.spectator.yaw"),(float)config.getDouble("Map.location.spectator.pitch"));
            map.setSpectatorSpawn(spectator);
        }

        if(config.contains("Map.location.item.1")) {
            Location item1 = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.item.1.x"), (float)config.getDouble("Map.location.item.1.y"), (float)config.getDouble("Map.location.item.1.z"));
            map.setItem_location_1(item1);
        }

        if(config.contains("Map.location.item.2")) {
            Location item2 = new Location(Bukkit.getWorld("LaserTag"), (float)config.getDouble("Map.location.item.2.x"), (float)config.getDouble("Map.location.item.2.y"), (float)config.getDouble("Map.location.item.2.z"));
            map.setItem_location_2(item2);
        }

        if(config.contains("Map.location.max_height")) {
            map.setMax_height((float)config.getDouble("Map.location.max_height"));
        }

        if (map.getItem_location_1() == null) map.setItem_location_1(new Location(Bukkit.getWorld("LaserTag"), 1, 100, 1));
        if (map.getItem_location_2() == null) map.setItem_location_2(new Location(Bukkit.getWorld("LaserTag"), 3, 100, 3));

        if (map.getMax_height() == -999) map.setMax_height(120);

        if (map.getMiddle() == null) map.setMiddle(new Location(Bukkit.getWorld("LaserTag"), 0, 100, 0));
        if (map.getSpectatorSpawn() == null) map.setSpectatorSpawn(new Location(Bukkit.getWorld("LaserTag"), 0, 100, 0));

        Main.plugin.applyGamerules(laserTag);
    }


    public Map getMapByName(String name) {
        for(Map m : maps) {
            if(ChatColor.stripColor(m.getName()).trim().equalsIgnoreCase(ChatColor.stripColor(name).trim())) {
                return m;
            }
        }
        return null;
    }

    public Map getCurrentMap() {
        return currentMap;
    }

    public List<Map> getMaps() {
        return maps;
    }

    private void openInventory(Player p) {
        int size = maps.size()/7 + 2;
        if(size < 4) size=4;
        Inventory inv = Bukkit.createInventory(null, 9*size, "§cMap Auswahl");
        inv.setContents(InventoryManager.getStandardContents(9*size, false));
        for(Map m : maps) {
            int v = 0;
            for(Player all : votes.keySet()) {
                if(votes.get(all) == m) v++;
            }
            ItemBuilder mapItem = new ItemBuilder(m.getMaterial()).withName("§e" + m.getName()).withAmount(v);
            mapItem.withLores("§8➥ §7ErbauerInnen§8: §6" + m.getAuthor());
            if(votes.containsKey(p) && m == votes.get(p)) {
                mapItem.setEnch(true);
            }
            inv.addItem(mapItem.toItemStack());
        }
        p.openInventory(inv);
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(e.getAction().equals(InventoryAction.NOTHING)) return;
        if(!e.getClickedInventory().getTitle().equals("§cMap Auswahl")) return;
        if(e.getCurrentItem() == null) return;
        if(e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        Map m = null;
        if((m = getMapByName(e.getCurrentItem().getItemMeta().getDisplayName())) == null) return;
        if(votes.containsKey(p) && votes.get(p) == m) {p.sendMessage(Main.prefix + "§cDu hast bereits für diese Map abgestimmt!");return;}
        votes.put(p, m);
        p.sendMessage(Main.prefix + "§aDu hast für die Map §e" + m.getName() + " §aabgestimmt!");
        p.closeInventory();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getPlayer().getInventory().getItemInHand() == null) return;
        if(e.getPlayer().getInventory().getItemInHand().getType().equals(Material.BOOK) && e.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().equals("§cMap")) {
            openInventory(e.getPlayer());
        }
    }

}
