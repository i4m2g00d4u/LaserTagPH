package com.playhills.lasertag.manager.weapons;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerHitbox {
	
	private final Location aa, ab;

	float range = 0.3F;
	public PlayerHitbox(Player player) {
		this.aa = player.getLocation().subtract(range, 0, range);
		this.ab = player.getLocation().add(range, 1.8, range);
	}
	
	public boolean contact(Location loc) {
		return ((loc.getX() >= aa.getX() && loc.getX() <= ab.getX() &&
				loc.getY() >= aa.getY() && loc.getY() <=  ab.getY() &&
				loc.getZ() >= aa.getZ() && loc.getZ() <= ab.getZ()
				));
	}
	
}
