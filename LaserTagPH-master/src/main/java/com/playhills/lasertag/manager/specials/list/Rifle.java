package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.Main;
import org.bukkit.entity.Player;


public class Rifle extends Special {
	
	public Rifle() {
		super(Main.plugin.getWeaponManager().getRifle().getItem().getType(), Main.plugin.getWeaponManager().getRifle().getName(), SpecialManager.USETYPE.OTHER);
	}

	@Override
	public void onPickup(Player p) {
		p.getInventory().setItem(Main.plugin.getSpecialManager().getPickupSlot(p), Main.plugin.getWeaponManager().getRifle().getItem());
	}
}
