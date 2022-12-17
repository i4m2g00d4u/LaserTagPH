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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Grenade extends Special {

    public int explode_time = 2;
    public int strength = 1;

    public int explosion_radius = 5;

    public LaserTag laserTag = Main.plugin.getLaserTag();

    private HashMap<Item, Player> grenades = new HashMap<>();

    public Grenade() {
        super(Material.FIREBALL, "§cGranate", SpecialManager.USETYPE.INTERACT);
        
        settings.put("ExplodeTime", explode_time);
        settings.put("Strength", strength);
        settings.put("ExplosionRadius", explosion_radius);
    }

    @Override
    public void onUse(Player p) {
    	shoot(p);
    }
    
    public void shoot(Player p) {
        Item s = p.getWorld().dropItem(p.getEyeLocation(), new ItemBuilder(Material.FIREBALL).withName("").toItemStack());
        s.setPickupDelay(999999);
        s.setVelocity(p.getEyeLocation().getDirection().multiply(strength));
        p.getInventory().clear(p.getInventory().getHeldItemSlot());
        grenades.put(s, p);
        countdown(s);
    }

    private void countdown(Item i) {Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> explode(i), 20L*explode_time);}
    
    private void explode(Item item) {
        LaserTag.ROLE belongs = laserTag.roles.get(grenades.get(item).getName());
        Location location = item.getLocation();
        Random random = ThreadLocalRandom.current();
        
        for (Player all: Bukkit.getOnlinePlayers()) {
            all.playSound(item.getLocation(), Sound.EXPLODE, 1, 1);
            //location.add(0.0D, 0.0D, 0.0D);
            int[] i = belongs == LaserTag.ROLE.PURPLE ? new int[]{0,0,1} : new int[]{0,1,0};
            for (int j = 0; j < 3000; j++) {
                Vector vector = (new Vector((random.nextDouble() *2.0D-1.0D), (random.nextDouble() *2.0D-1.0D), (random.nextDouble() *2.0D-1.0D)).normalize()).multiply(explosion_radius);
                location.add(vector);
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), i[0], i[1], i[2], 1, 0);
                ((CraftPlayer)all).getHandle().playerConnection.sendPacket(packet);
                location.subtract(vector);
            }
            if (Main.plugin.getLaserTag().roles.get(all.getName()) == belongs || Main.plugin.getLaserTag().roles.get(all.getName()) == LaserTag.ROLE.WAITING || Main.plugin.getLaserTag().spectators.contains(all.getName())) continue;
            if (all.getLocation().distance(item.getLocation()) > distance()) continue;
            if (RespawnListener.respawning.contains(all.getName())) continue;
            RespawnListener.kill(all, grenades.get(item), "Grenade");
            grenades.remove(item);
        }
        item.remove();
    }

    public double distance() {
        return explosion_radius;
    }
    
}

