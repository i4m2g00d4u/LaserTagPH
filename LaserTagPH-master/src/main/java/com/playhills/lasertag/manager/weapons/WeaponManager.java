package com.playhills.lasertag.manager.weapons;

import com.playhills.lasertag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

public class WeaponManager {

    public WeaponManager() {}

    private Weapon pistol;
    public Weapon getPistol() {
        if (pistol == null) pistol = new Weapon(new ItemBuilder(Material.WOOD_HOE).withName("§eWaffe").toItemStack(),12, 600, -1, Sound.ITEM_BREAK, 2f);
        return pistol;
    }

    private Weapon rifle;
    public Weapon getRifle() {
        if (rifle == null) rifle = new Weapon(new ItemBuilder(Material.IRON_HOE).withName("§eSchnellfeuerwaffe").toItemStack(),16, 250, 25, Sound.FIREWORK_LARGE_BLAST, 2f);
        return rifle;
    }

    private Weapon doubleShot;
    public Weapon getDoubleShot() {
        if (doubleShot == null) doubleShot = new Weapon(new ItemBuilder(Material.STONE_HOE).withName("§eDoppelläufige Waffe").toItemStack(),12, 1150, 12, Sound.FIREWORK_LAUNCH, 2f);
        return doubleShot;
    }

    private Weapon minigun;
    public Weapon getMinigun() {
        if(minigun == null) minigun = new Weapon(new ItemBuilder(Material.DIAMOND_HOE).withName("§eMinigun").toItemStack(), 24, 25, -1, Sound.VILLAGER_YES, 2f);
        return minigun;
    }

    private Weapon sniper;
    public Weapon getSniper() {
        if(sniper == null) sniper = new Weapon(new ItemBuilder(Material.GOLD_HOE).withName("§eScharfschützengewehr").toItemStack(), 32, 2000, 16, Sound.FIREWORK_LARGE_BLAST2, 0.2f);
        return sniper;
    }

    //PUSH ERROR
}
