package com.playhills.lasertag.manager;

import com.playhills.lasertag.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {

    public static ItemStack[] getStandardContents(int size, boolean filled) {
        Inventory inv = Bukkit.createInventory(null, size, "");
        ItemStack dGlass = new ItemBuilder(Material.STAINED_GLASS_PANE).withDurability(15).withName(" ").toItemStack();
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).withDurability(7).withName(" ").toItemStack();

        for(int i=0;i<inv.getSize();i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(0, dGlass);
        inv.setItem(1, dGlass);
        inv.setItem(9, dGlass);
        inv.setItem(7, dGlass);
        inv.setItem(8, dGlass);
        inv.setItem(17, dGlass);

        inv.setItem(size-1, dGlass);
        inv.setItem(size-2, dGlass);
        inv.setItem(size-8, dGlass);
        inv.setItem(size-9, dGlass);
        inv.setItem(size-10, dGlass);
        inv.setItem(size-18, dGlass);

        if(!filled) {
            ItemStack air = new ItemStack(Material.AIR);
            for(int a=1;a<inv.getSize()/9-1;a++) {
                for (int i = 0; i < 7; i++) {
                    inv.setItem(a*9+i+1, air);
                }
            }
        }

        return inv.getContents();
    }

}
