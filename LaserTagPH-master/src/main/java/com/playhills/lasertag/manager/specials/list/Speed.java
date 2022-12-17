package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed extends Special {

	public int time = 8;
	public int amplifier = 2;
	
	public Speed() {
		super(Material.SUGAR, "§aSpeed", SpecialManager.USETYPE.PICKUP);
	}

	@Override
	public void onUse(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, time*20, amplifier));
	}

}
