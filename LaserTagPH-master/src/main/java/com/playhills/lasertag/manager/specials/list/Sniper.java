package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.Main;
import org.bukkit.entity.Player;

public class Sniper extends Special {
    public Sniper() {
        super(Main.plugin.getWeaponManager().getSniper().getItem().getType(), Main.plugin.getWeaponManager().getSniper().getName(), SpecialManager.USETYPE.OTHER);
    }

    @Override
    public void onPickup(Player p) {
        p.getInventory().setItem(Main.plugin.getSpecialManager().getPickupSlot(p), Main.plugin.getWeaponManager().getSniper().getItem());
    }
}
