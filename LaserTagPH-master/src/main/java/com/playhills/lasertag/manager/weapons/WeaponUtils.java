package com.playhills.lasertag.manager.weapons;

import com.playhills.lasertag.listener.RespawnListener;
import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WeaponUtils {

	public static void drawParticle(Player p, LaserTag.ROLE team, Location loc) {
		int[] i = team == LaserTag.ROLE.PURPLE ? new int[]{0,0,1} : new int[]{0,1,0};

		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), i[0], i[1], i[2], 1, 0);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
	}
	
	public static void drawLine(Player p, Weapon weapon) {
		Location origin = p.getEyeLocation();
		Vector direction = origin.getDirection();
			direction.multiply(weapon.getRange());
			Location destination = origin.clone().add(direction);
			direction.normalize();
			drawLine1(p, Main.plugin.getLaserTag().roles.get(p.getName()), weapon, origin, destination, weapon.getSpace());
			return;
	}
	
	public static void drawLine1(Player p, LaserTag.ROLE team, Weapon tool, Location point1, Location point2, double space) {
		World world = point1.getWorld();
		double distance = point1.distance(point2);
		Vector p1 = point1.toVector();
		Vector p2 = point2.toVector();
		Vector vector = p2.clone().subtract(p1).normalize().multiply(0.03);
		double length = 0;
		
		int timer = 1;
		
		for(;length < distance; p1.add(vector)) {
			Location endPos = new Location(world, p1.getX(), p1.getY(), p1.getZ());
			if(timer % (15*space) == 0) {
				Bukkit.getOnlinePlayers().forEach(all -> drawParticle(all, team, endPos));
			}
			if(shouldCancel(p, tool, endPos)) {
				break;
			}
			length += 0.03;
			timer++;
		}
	}
	
		
	private static boolean shouldCancel(Player p, Weapon tool, Location loc) {
		if(loc.getBlock().getType().isSolid() && !loc.getBlock().getType().equals(Material.BARRIER)) return true;

		LaserTag laserTag = Main.plugin.getLaserTag();

		for(Player all : Bukkit.getOnlinePlayers()) {
			if (all.equals(p)) continue;
			PlayerHitbox hitbox = new PlayerHitbox(all);
			if (!hitbox.contact(loc)) continue;
			if (RespawnListener.respawning.contains(all.getName())) continue;
			if (laserTag.spectators.contains(all.getName()) || laserTag.roles.get(all.getName()) == LaserTag.ROLE.WAITING) continue;
			if(Main.plugin.getLaserTag().roles.get(p.getName()) == Main.plugin.getLaserTag().roles.get(all.getName())) continue;
			RespawnListener.kill(all, p, tool.getName());
			return true;
		}

		return false;
	}
}
