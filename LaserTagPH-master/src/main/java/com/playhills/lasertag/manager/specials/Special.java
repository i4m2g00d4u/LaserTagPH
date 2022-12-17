package com.playhills.lasertag.manager.specials;

import com.playhills.lasertag.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;


public class Special {

	private Material material;
	private String name;
	private SpecialManager.USETYPE useType;
	
	public HashMap<String, Object> settings = new HashMap<>();
		
	
	public Special(Material material, String name, SpecialManager.USETYPE useType) {
		this.material = material;
		this.name = name;
		this.useType = useType;
	}
	
	public void onPickup(Player p) {
		if(useType.equals(SpecialManager.USETYPE.PICKUP)) {
			onUse(p);
			return;
		}
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§7"+name);
		item.setItemMeta(meta);
		p.getInventory().setItem(Main.plugin.getSpecialManager().getPickupSlot(p), item);
	}
		
	public void onUse(Player p) {}
	
	public SpecialManager.USETYPE getUseType() {
		return useType;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public String getName() {
		return name;
	}
	
	public HashMap<String, Object> getSettings() {
		return settings;
	}
	
	public boolean hasEvent() {return false;}

	public boolean canSpawn() {return true;}

	public boolean canPickup(Player p) {return true;}
	
}
