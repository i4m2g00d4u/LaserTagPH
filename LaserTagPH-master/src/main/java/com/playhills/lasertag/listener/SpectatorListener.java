package com.playhills.lasertag.listener;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import com.playhills.lasertag.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SpectatorListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
        e.setCancelled(true);
        if (e.getAction() == Action.PHYSICAL) return;
        if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() != Material.COMPASS) return;
        GUI(e.getPlayer(), true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPickUp(PlayerPickupItemEvent e) {
         if (!Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
         e.setCancelled(true);
     }

     private void GUI(Player spec, boolean show_spectators) {
         Inventory inv = Bukkit.createInventory(null, 45, "§eZuschauen");
         LaserTag laserTag = Main.plugin.getLaserTag();
         for (int i = 0; i < 9; i++) {
             inv.setItem(i, glass);
         }
         inv.setItem(4, title);
         inv.setItem(8, hide(!show_spectators));

         for (Player all: Bukkit.getOnlinePlayers()) {
             if (all == spec) continue;
             if (laserTag.spectators.contains(all.getName()) && show_spectators) continue;
             inv.addItem(skull(all.getName()));
         }

         spec.openInventory(inv);
     }

     @EventHandler
     public void onMove(PlayerMoveEvent e) {
         for (Player all : Bukkit.getOnlinePlayers()) {
             if (all.getGameMode() !=GameMode.SPECTATOR) continue;
             if (all.getSpectatorTarget() == null) continue;
             Main.plugin.getLaserTag().sendActionBar(all, "§6"+all.getSpectatorTarget().getName()+" §8| §7Schleichen zum verlassen.");
         }
     }

     @EventHandler
     public void onSneak(PlayerToggleSneakEvent e) {
        if (!Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
        if (e.getPlayer().getGameMode() != GameMode.SPECTATOR || e.getPlayer().getSpectatorTarget() != null) return;
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getPlayer().setAllowFlight(true);
         Main.plugin.getLaserTag().sendActionBar(e.getPlayer(), "§3");
        RespawnListener.defaultEquip(e.getPlayer());
     }

     @EventHandler
     public void onTeleport(PlayerTeleportEvent e) {
         if (!Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
         if (e.getPlayer().getGameMode() != GameMode.SPECTATOR || e.getPlayer().getSpectatorTarget() != null) return;
         e.getPlayer().setGameMode(GameMode.ADVENTURE);
         e.getPlayer().setAllowFlight(true);
         Main.plugin.getLaserTag().sendActionBar(e.getPlayer(), "§3");
         RespawnListener.defaultEquip(e.getPlayer());
     }

    @EventHandler
    protected void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
        if (e.getClickedInventory() == null || e.getClickedInventory()!= e.getView().getTopInventory()) return;
        if (!e.getView().getTitle().contains("§")) return;
        if (e.getCurrentItem() == null) return;

        ItemStack i = e.getCurrentItem();
        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getClickedInventory();
        if (!inv.getTitle().contains("Zuschauen")) return;

        if (i.getType()==Material.HOPPER) {
            boolean b = i.getItemMeta().getDisplayName().contains("§c");
            GUI(p, b);
            return;
        }
        if (i.getType()==Material.SKULL_ITEM) {
            p.closeInventory();
            if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
                spectate(p, i.getItemMeta().getDisplayName(), true);
            } else if (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.SHIFT_LEFT) {
                spectate(p, i.getItemMeta().getDisplayName(), false);
            }
        }
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (!Main.plugin.getLaserTag().spectators.contains(e.getDamager().getName())) return;
        if (((Player) e.getDamager()).getItemInHand() != null && ((Player) e.getDamager()).getItemInHand().getType() == Material.COMPASS) return;
        if (Main.plugin.getLaserTag().spectators.contains(e.getEntity().getName())) return;
        spectate((Player) e.getDamager(), e.getEntity().getName(), true);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player)) return;
        if (!Main.plugin.getLaserTag().spectators.contains(e.getPlayer().getName())) return;
        if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.COMPASS) return;
        if (Main.plugin.getLaserTag().spectators.contains(e.getRightClicked().getName())) return;
        spectate(e.getPlayer(), e.getRightClicked().getName(), true);
    }

    private void spectate(Player spec, String target, boolean setAsSpec) {
        target = target.replace("§e","").replace("§5","").replace("§7","");
        Player t = Bukkit.getPlayer(target);
        if (t == null) {
            spec.sendMessage(Main.prefix + "Der Spieler ist nicht länger auffindbar.");
            return;
        }
        spec.teleport(t);
        spec.playSound(spec.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        if (!setAsSpec) {
            spec.sendMessage(Main.prefix + "Du hast dich zu §e"+t.getName()+"§7 teleportiert.");
        } else {
            if (Main.plugin.getLaserTag().spectators.contains(t.getName()) && spec.getSpectatorTarget() == null) {
                spec.sendMessage(Main.prefix + "Du kannst keinem Zuschauer zuschauen.");
                return;
            }
            spec.sendMessage(Main.prefix + "Du siehst nun §e"+t.getName()+"§7 zu.");
            spec.setGameMode(GameMode.SPECTATOR);
            spec.setSpectatorTarget(t);
            spec.getInventory().clear(0);
        }
    }

    private final ItemStack title = new ItemBuilder(Material.NAME_TAG).withName("§eZuschauen").toItemStack();
    private ItemStack hide(boolean b) {
        return new ItemBuilder(Material.HOPPER).withName("§7Verstecke Zuschauer§8: "+(b ? "§cNein" : "§aJa")).setEnch(b).toItemStack();
    }
    private final ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).withDurability(7).withName("§r").toItemStack();
    private ItemStack skull(String playername) {
        LaserTag laserTag = Main.plugin.getLaserTag();
        return new ItemBuilder(Material.SKULL_ITEM).withName((laserTag.roles.get(playername) == LaserTag.ROLE.PURPLE ? "§5" : laserTag.roles.get(playername) == LaserTag.ROLE.YELLOW ? "§e" : "§7")+ playername).withLores("","§7Linksklick zum Teleportieren","§7Rechtsklick zum Zuschauen").toPlayerHead(playername);
    }
}
