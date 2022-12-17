package com.playhills.lasertag.manager.specials;

import com.playhills.lasertag.manager.specials.list.Double;
import com.playhills.lasertag.manager.specials.list.*;
import com.playhills.lasertag.utils.ItemBuilder;
import com.playhills.lasertag.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SpecialManager implements Listener{
	
	private final List<Special> specials;
	
	private final int[] specialSlots = {1,2,3};


	private final ArrayList<Special> spawnedSpecials = new ArrayList<>();

	
	public SpecialManager() {
		specials = new ArrayList<>();
		
		specials.add(new Grenade());
		specials.add(new Mine());
		specials.add(new Speed());
		specials.add(new FrostBite());
		specials.add(new Rifle());
		specials.add(new Sniper());
		specials.add(new Double());
		specials.add(new Shield());

		for (Special s : specials) {
			if (!s.hasEvent()) continue;
			Bukkit.getPluginManager().registerEvents((Listener)s, Main.plugin);
		}
	}
	
	public Special getSpecialByName(String name) {
		for(Special sp : specials) {
			if(ChatColor.stripColor(sp.getName().trim()).equalsIgnoreCase(name.trim())) {
				return sp;
			}
		}
		return null;
	}

	private int task;
	public void cancelSpecial() {
		Bukkit.getScheduler().cancelTask(task);
	}
	public void spawnSpecialsQueue() {
		 task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, () -> {
			if(spawnedSpecials.size() < 4) {
				spawnRandomSpecial();
			}
		},0, 20*15);
	}
	public Special spawnRandomSpecial() {
		Special sp;
		while (true) {
			 sp = specials.get(new Random().nextInt(specials.size()));
			if (!spawnedSpecials.contains(sp)) break;
		}
		Location spawn = getRandomSpecialLocation();

		spawnSpecial(sp, spawn);
		return sp;
	}

	private Location getRandomSpecialLocation() {
		Location start = Main.plugin.getMapManager().getCurrentMap().getItem_location_1();
		Location end = Main.plugin.getMapManager().getCurrentMap().getItem_location_2();
		Location loc;
		while (true) {
			boolean x = start.getBlockX() > end.getBlockX();
			int Ix = x ? new Random().nextInt(start.getBlockX() - end.getBlockX()) + end.getBlockX() : new Random().nextInt(end.getBlockX() - start.getBlockX()) + start.getBlockX();
			boolean z = start.getBlockZ() > end.getBlockZ();
			int Iz = z ? new Random().nextInt(start.getBlockZ() - end.getBlockZ()) + end.getBlockZ() : new Random().nextInt(end.getBlockZ() - start.getBlockZ()) + start.getBlockZ();
			loc = new Location(start.getWorld(), Ix+.5, start.getBlockY(), Iz+.5);
			if (loc.getBlock().getType() == Material.AIR) break;
		}
		return loc;
	}

	public void spawnSpecial(Special sp, Location loc) {

		Item item = loc.getWorld().dropItem(loc, new ItemBuilder(sp.getMaterial()).toItemStack());
		item.setCustomName(sp.getName());
		item.setCustomNameVisible(true);
		spawnedSpecials.add(sp);
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		e.setCancelled(true);
		if (Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
		ItemStack stack = e.getItem().getItemStack();
		for (Special sp : specials) {
			if (!sp.getMaterial().equals(stack.getType())) continue;
			if(!canPickupSpecial(sp, e.getPlayer())) return;
			if(!sp.canPickup(e.getPlayer())) return;

			sp.onPickup(e.getPlayer());
			e.getItem().remove();

			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
				spawnedSpecials.remove(sp);
			}, 3);
			return;
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(e.getPlayer().getItemInHand() == null) return;
		if(e.getPlayer().getItemInHand().getItemMeta() == null) return;
		if(e.getPlayer().getItemInHand().getItemMeta().getDisplayName() == null) return;
		for(Special sp : specials) {
			if(!sp.getUseType().equals(USETYPE.INTERACT)) continue;
			if(e.getPlayer().getItemInHand().getType().equals(sp.getMaterial()) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains(sp.getName())) {
				e.setCancelled(true);
				sp.onUse(e.getPlayer());
				break;
			}
		}
		
	}

	public int getPickupSlot(Player p) {
		for(int i : specialSlots) {
			if(p.getInventory().getItem(i) == null) {
				return i;
			}
		}
		return -1;
	}
	public boolean canPickupSpecial(Special sp, Player p) {
		if(sp.getUseType().equals(USETYPE.PICKUP)) return true;
		for(int i : specialSlots) {
			if(p.getInventory().getItem(i) == null) {
				return true;
			}
		}
		return false;
	}

	public List<Special> getSpecials() {
		return specials;
	}
	
	public enum USETYPE {
		INTERACT, PICKUP, OTHER;
	}
	
}
