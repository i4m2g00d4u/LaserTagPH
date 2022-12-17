package com.playhills.lasertag.manager.map;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map {

    private final Material material;
    private final String name;
    private final String author;

    private List<Location> purpleSpawns;
    private List<Location> yellowSpawns;

    private Location item_location_1;
    private Location item_location_2;

    private float max_height = -999;

    private Location middle;
    private Location spectatorSpawn;

    public Map(Material material, String name, String author) {
        this.material = material;
        this.name = name;
        this.author = author;
    }

    public void setPurpleSpawns(ArrayList<Location> purpleSpawn) {
        this.purpleSpawns = purpleSpawn;
    }

    public void setYellowSpawns(List<Location> yellowSpawn) {
        this.yellowSpawns = yellowSpawn;
    }

    public void setMiddle(Location middle) {
        this.middle = middle;
    }

    public Location getMiddle() {
        return middle;
    }

    public float getMax_height() {
        return max_height;
    }

    public void setMax_height(float max_height) {
        this.max_height = max_height;
    }

    public Location getItem_location_1() {
        return item_location_1;
    }

    public Location getItem_location_2() {
        return item_location_2;
    }

    public void setItem_location_1(Location item_location_1) {
        this.item_location_1 = item_location_1;
    }

    public void setItem_location_2(Location item_location_2) {
        this.item_location_2 = item_location_2;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public Location getRandomPurpleSpawn() {
        return purpleSpawns.get(new Random().nextInt(purpleSpawns.size()));
    }

    public Location getRandomYellowSpawn() {
        return yellowSpawns.get(new Random().nextInt(yellowSpawns.size()));
    }

    public List<Location> getPurpleSpawns() {
        return purpleSpawns;
    }

    public List<Location> getYellowSpawns() {
        return yellowSpawns;
    }

    public Material getMaterial() {
        return material;
    }
}
