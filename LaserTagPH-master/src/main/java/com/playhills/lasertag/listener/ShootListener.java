package com.playhills.lasertag.listener;

import com.playhills.lasertag.manager.weapons.WeaponManager;
import com.playhills.lasertag.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShootListener implements Listener {

    @EventHandler
    public void onShoot(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta().getDisplayName() == null) return;

        WeaponManager wm = Main.plugin.getWeaponManager();
        if (e.getItem().getItemMeta().getDisplayName().equals(wm.getPistol().getName())) {
            e.setCancelled(true);
            wm.getPistol().onShoot(e.getPlayer(), true);
        } else if (e.getItem().getItemMeta().getDisplayName().equals(wm.getRifle().getName())) {
            e.setCancelled(true);
            wm.getRifle().onShoot(e.getPlayer(), true);
        } else if (e.getItem().getItemMeta().getDisplayName().equals(wm.getDoubleShot().getName())) {
            e.setCancelled(true);
            if (wm.getDoubleShot().onCooldown(e.getPlayer())) return;
            wm.getDoubleShot().onShoot(e.getPlayer(), false);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
                if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getItemMeta() == null || e.getPlayer().getItemInHand().getItemMeta().getDisplayName() == null || !e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(wm.getDoubleShot().getName())) return;
                wm.getDoubleShot().onShoot(e.getPlayer(), false);
            },5);
        } else if (e.getItem().getItemMeta().getDisplayName().equals(wm.getMinigun().getName())) {
            e.setCancelled(true);
            wm.getMinigun().onShoot(e.getPlayer(), true);
        } else if (e.getItem().getItemMeta().getDisplayName().equals(wm.getSniper().getName())) {
            e.setCancelled(true);
            wm.getSniper().onShoot(e.getPlayer(), true);
        }
    }
}
