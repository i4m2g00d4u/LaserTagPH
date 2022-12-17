package com.playhills.lasertag.manager.teams;

import com.playhills.lasertag.utils.ItemBuilder;
import com.playhills.lasertag.LaserTag;
import com.playhills.lasertag.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeamSelector {

    public TeamSelector() {}

    public void openInv(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, "§eTeam Auswahl");
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(21, team(LaserTag.ROLE.PURPLE, p));
        inv.setItem(23, team(LaserTag.ROLE.YELLOW, p));

        p.openInventory(inv);
    }

    public ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).withDurability(7).withName("§r").toItemStack();
    public ItemStack team(LaserTag.ROLE role, Player p) {

        ItemStack i = new ItemBuilder(Material.BARRIER).withName(role == LaserTag.ROLE.PURPLE ? "§8Team Lila" : "§8Team Gelb").withLores("","§8§mKlicke, um Team "+(role == LaserTag.ROLE.PURPLE ? "Lila" : "Gelb")+" beizutreten.","§7Team "+(role == LaserTag.ROLE.PURPLE ? "§5Lila" : "§eGelb")+ "§7 ist voll!").withDurability(role == LaserTag.ROLE.PURPLE ? 10 : 4).toItemStack();
        if (p.hasPermission("LaserTag.game.bypassTeamLimit")) i = new ItemBuilder(Material.STAINED_CLAY).withName(role == LaserTag.ROLE.PURPLE ? "§5Team Lila" : "§eTeam Gelb").withLores("","§8§mDieses Team ist voll.","§7Bypass durch deinen Rang§7:","","§7Klicke, um Team "+(role == LaserTag.ROLE.PURPLE ? "§5Lila" : "§eGelb")+"§7 beizutreten.").withDurability(role == LaserTag.ROLE.PURPLE ? 10 : 4).toItemStack();
        if (Main.plugin.getLaserTag().is_allowed_to_join(role)) i = new ItemBuilder(Material.WOOL).withName(role == LaserTag.ROLE.PURPLE ? "§5Team Lila" : "§eTeam Gelb").withLores("","§7Klicke, um Team "+(role == LaserTag.ROLE.PURPLE ? "§5Lila" : "§eGelb")+"§7 beizutreten.").withDurability(role == LaserTag.ROLE.PURPLE ? 10 : 4).toItemStack();
        if (Main.plugin.getLaserTag().roles.get(p.getName()) == role) i = new ItemBuilder(Material.WOOL).setEnch(true).withName(role == LaserTag.ROLE.PURPLE ? "§8Team Lila" : "§8Team Gelb").withLores("","§8§mKlicke, um Team "+(role == LaserTag.ROLE.PURPLE ? "Lila" : "Gelb")+" beizutreten.","§7Du bist bereits in Team "+(role == LaserTag.ROLE.PURPLE ? "§5Lila" : "§eGelb")+"§7!").withDurability(role == LaserTag.ROLE.PURPLE ? 10 : 4).toItemStack();

        return i;
    }
    public ItemStack bed = new ItemBuilder(Material.BED).withName("§eTeam Auswahl").toItemStack();
}
