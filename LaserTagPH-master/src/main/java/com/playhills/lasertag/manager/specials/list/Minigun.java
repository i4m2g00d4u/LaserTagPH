package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import org.bukkit.entity.Player;

public class Minigun extends Special {

    public Minigun() {
        super(Main.plugin.getWeaponManager().getDoubleShot().getItem().getType(), Main.plugin.getWeaponManager().getMinigun().getName(), SpecialManager.USETYPE.OTHER);
    }

    @Override
    public void onPickup(Player p) {
        p.getInventory().setItem(Main.plugin.getSpecialManager().getPickupSlot(p), Main.plugin.getWeaponManager().getMinigun().getItem());
    }

    @Override
    public boolean canSpawn() {
        return false;
    }
}
