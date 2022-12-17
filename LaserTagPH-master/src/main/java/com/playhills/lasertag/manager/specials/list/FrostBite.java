package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.listener.RespawnListener;
import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.utils.ItemBuilder;
import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FrostBite extends Special {

	public FrostBite() {
		super(Material.FIREWORK_CHARGE, "§bFrostbite", SpecialManager.USETYPE.INTERACT);
	}

	public int explode_time = 2;
	public int strength = 1;
	public int explosion_radius = 7;
	public int frost_duration = 5;
	
	public LaserTag laserTag = Main.plugin.getLaserTag();
	
	private final HashMap<Item, Player> freeze = new HashMap<>();
	
	@Override
	public void onUse(Player p) {
		shoot(p);
	}
	
	public void shoot(Player p) {
        Item s = p.getWorld().dropItem(p.getEyeLocation(), new ItemBuilder(Material.FIREWORK_CHARGE).withName("THROWN").toItemStack());
        s.setPickupDelay(999999);
        s.setVelocity(p.getEyeLocation().getDirection().multiply(strength));
        p.getInventory().clear(p.getInventory().getHeldItemSlot());
        freeze.put(s, p);
        countdown(s);
    }

    private void countdown(Item i) {Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> explode(i), 20L*explode_time);}
    
    private void explode(Item item) {
        LaserTag.ROLE belongs = laserTag.roles.get(freeze.get(item).getName());
        Location location = item.getLocation();
        Random random = ThreadLocalRandom.current();
        
        for (Player all: Bukkit.getOnlinePlayers()) {
            all.playSound(item.getLocation(), Sound.GLASS, 1, 1);
            int[] i = belongs == LaserTag.ROLE.PURPLE ? new int[]{0,0,1} : new int[]{0,1,0};
            for (int j = 0; j < 3000; j++) {
                Vector vector = (new Vector((random.nextDouble() *2.0D-1.0D), (random.nextDouble() *2.0D-1.0D), (random.nextDouble() *2.0D-1.0D)).normalize()).multiply(explosion_radius);
                location.add(vector);
                
                int rndm = new Random().nextInt(4);
                PacketPlayOutWorldParticles packet;
                if(rndm == 0) {
                	 packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), i[0], i[1], i[2], 1, 0);
                } else {
                	int rndm1 = new Random().nextInt(4);
                	if(rndm1 <= 2) {
                		packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), 1, 1, 1, 1, 0);
                	} else {
                		packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), -1, 0.2f, 0.3f, 1, 0);
                	}
                }
                
                ((CraftPlayer)all).getHandle().playerConnection.sendPacket(packet);
                location.subtract(vector);
            }
            if (Main.plugin.getLaserTag().roles.get(all.getName()) == belongs || Main.plugin.getLaserTag().roles.get(all.getName()) == LaserTag.ROLE.WAITING || Main.plugin.getLaserTag().spectators.contains(all.getName())) continue;
            if (all.getLocation().distance(item.getLocation()) > distance()) continue;
            if (RespawnListener.respawning.contains(all.getName())) continue;
            all.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, frost_duration*20, 1));
            all.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, frost_duration*20, 255));
            all.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, frost_duration*20, 1));
            freeze.remove(item);
        }
        item.remove();
    }
    
    public double distance() {
        return explosion_radius;
    }
	
}
