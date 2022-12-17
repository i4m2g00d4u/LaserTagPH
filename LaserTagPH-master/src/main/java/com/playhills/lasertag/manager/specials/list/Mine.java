package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.listener.RespawnListener;
import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.HashSet;


public class Mine extends Special implements Listener{

	private HashMap<Location, Player> mines = new HashMap<>();
	
	public int explosion_radius = 6;
	public LaserTag laserTag = Main.plugin.getLaserTag();
	
	public Mine() {
		super(Material.STONE_PLATE, "§9Mine", SpecialManager.USETYPE.INTERACT);
		
		settings.put("ExplosionRadius", explosion_radius);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUse(Player p) {
		Block block =  p.getTargetBlock((HashSet<Byte>) null, 100);
		if(!block.getLocation().add(0, 1, 0).getBlock().getType().equals(Material.AIR) || block.getLocation().add(0, 1,0).getBlock().getType().isSolid()) return;
		block.getLocation().add(0, 1, 0).getBlock().setType(Material.STONE_PLATE);
		p.getInventory().clear(p.getInventory().getHeldItemSlot());
		mines.put(block.getLocation().add(0, 1, 0), p);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(!e.getAction().equals(Action.PHYSICAL)) return;
		if(!e.getClickedBlock().getType().equals(Material.STONE_PLATE)) return;
		if(!mines.containsKey(e.getClickedBlock().getLocation())) return;
		LaserTag.ROLE belongs = laserTag.roles.get(mines.get(e.getClickedBlock().getLocation()).getName());
		if(Main.plugin.getLaserTag().roles.get(p.getName()) == belongs || Main.plugin.getLaserTag().roles.get(p.getName()) == LaserTag.ROLE.WAITING || Main.plugin.getLaserTag().spectators.contains(p.getName())) {e.setCancelled(true);return; }
		Block b = e.getClickedBlock();
	
		for(Player all : Bukkit.getOnlinePlayers()) {
			sendEffect(all, b.getLocation());
			if(Main.plugin.getLaserTag().roles.get(all.getName()) == belongs || Main.plugin.getLaserTag().roles.get(p.getName()) == LaserTag.ROLE.WAITING || Main.plugin.getLaserTag().spectators.contains(p.getName())) continue;
			if(!(all.getLocation().distance(b.getLocation()) <= explosion_radius)) continue;
			RespawnListener.kill(all, this.mines.get(e.getClickedBlock().getLocation()), "Mine");
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
			public void run() {
				mines.remove(b.getLocation());
				b.setType(Material.AIR);
			}
		});
	}

	public static void sendEffect(Player p, Location loc) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_NORMAL, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 0.4F, 0.2F, 0.4F, 0, 30);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		p.playSound(loc, Sound.EXPLODE, 1, 1);
	}
	@Override
	public boolean hasEvent() {
		return true;
	}

}
