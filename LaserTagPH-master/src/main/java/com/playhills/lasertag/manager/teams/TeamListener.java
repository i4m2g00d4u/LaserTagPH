package com.playhills.lasertag.manager.teams;

import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeamListener implements Listener {

    @EventHandler
    protected void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInHand() == null) return;
        if (!e.getPlayer().getInventory().getItemInHand().isSimilar(new TeamSelector().bed)) return;
        e.setCancelled(true);
        new TeamSelector().openInv(e.getPlayer());
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
        if (!inv.getTitle().contains("Team Auswahl")) return;

        if (!i.getEnchantments().isEmpty()) {
            if (i.getDurability() == 10) {
                p.sendMessage(Main.prefix + "Du hast Team §5Lila §7verlassen!");
                p.closeInventory();
            } else if (i.getDurability() == 4) {
                p.sendMessage(Main.prefix + "Du hast Team §eGelb §7verlassen!");
                p.closeInventory();
            }
            Main.plugin.getLaserTag().roles.put(p.getName(), LaserTag.ROLE.WAITING);
            new ScoreboardManager().refreshBoard();
            p.closeInventory();
            openforall();
            return;
        }
        if (i.getDurability() == 10) {
            Main.plugin.getLaserTag().roles.put(p.getName(), LaserTag.ROLE.PURPLE);
            p.sendMessage(Main.prefix + "Du hast Team §5Lila §7betreten!"+(i.getType() != Material.STAINED_CLAY ? "": " §8(§7Rang Bypass§8)"));
            p.closeInventory();
        } else if (i.getDurability() == 4) {
            Main.plugin.getLaserTag().roles.put(p.getName(), LaserTag.ROLE.YELLOW);
            p.sendMessage(Main.prefix + "Du hast Team §eGelb §7betreten!"+(i.getType() != Material.STAINED_CLAY ? "": " §8(§7Rang Bypass§8)"));
            p.closeInventory();
        }
        openforall();
        new ScoreboardManager().refreshBoard();
    }

    private void openforall() {
        for (Player all: Bukkit.getOnlinePlayers()) {
            if (all.getOpenInventory() == null) continue;
            if (!all.getOpenInventory().getTitle().contains("Team Auswahl")) continue;
            new TeamSelector().openInv(all);
        }
    }
}
