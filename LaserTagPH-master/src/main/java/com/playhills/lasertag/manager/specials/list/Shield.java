package com.playhills.lasertag.manager.specials.list;

import com.playhills.lasertag.Main;
import com.playhills.lasertag.manager.specials.Special;
import com.playhills.lasertag.manager.specials.SpecialManager;
import com.playhills.lasertag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Shield extends Special {

    public ArrayList<String> shield = new ArrayList<>();

    public Shield() {
        super(Material.JUNGLE_DOOR_ITEM, "§6Schild", SpecialManager.USETYPE.PICKUP);
    }

    @Override
    public void onUse(Player p) {
        shield.add(p.getName());
        p.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).withName("§7Schild").withLores("","§7Dein Schild schützt dich 1 mal vor dem Tod.").setUnbreakable(true).toItemStack());
        p.sendMessage(Main.prefix + "Du hast ein Schild ausgerüstet!");
    }

    @Override
    public boolean canPickup(Player p) {
        if(shield.contains(p.getName())) {
            return false;
        }
        return true;
    }
}
