package com.playhills.lasertag.manager.weapons;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Weapon {

	private int range;
	private int delay;
	private final int amountOfShots;
	private final List<Player> waiting;
	private final Sound sound;
	private final float pitch;
	private final ItemStack item;
	
	private double space = 1;
	
	public Weapon(ItemStack item, int range, int delay, int amountOfShots, Sound sound, float pitch) {
		this.range = range;
		this.amountOfShots = amountOfShots;
		this.delay = delay;
		this.sound = sound;
		this.pitch = pitch;
		this.item = item;
		
		waiting = new ArrayList<>();
	}

	public String getName() {
		return item.getItemMeta().getDisplayName();
	}
	
	public void onShoot(Player p, boolean checkCooldown) {
		if (waiting.contains(p) && checkCooldown) return;
		waiting.add(p);
		onRemove(p);
		Bukkit.getOnlinePlayers().forEach(all -> all.playSound(p.getLocation(), sound, 1, pitch));
		WeaponUtils.drawLine(p, this);

		if (amountOfShots == -1) return;

		int max = item.getType().getMaxDurability();
		int thru = max / amountOfShots; thru++;
		p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() + thru));
		if (p.getItemInHand().getDurability() > item.getType().getMaxDurability()) {
			p.setItemInHand(null);
		}
	}
	
	private void onRemove(Player p) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				waiting.remove(p);
			}
		}, delay);
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public void setRange(int range) {
		this.range = range;
	}

	public ItemStack getItem() {
		return item;
	}

	public boolean onCooldown(Player p) {
		return waiting.contains(p);
	}
	
	public int getDelay() {
		return delay;
	}

	public int getRange() {
		return range;
	}

	public void setSpace(double space) {
		this.space = space;
	}
	
	public double getSpace() {
		return space;
	}
	
}
