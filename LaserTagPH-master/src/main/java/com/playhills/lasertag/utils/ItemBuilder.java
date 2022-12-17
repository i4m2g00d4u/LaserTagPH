package com.playhills.lasertag.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {
    private Material material;
    private int amount = 1;
    private String name;
    private List<String> lores = new ArrayList<String>();
    private short durability = -101;
    private MaterialData data;
    private Potion potion;
    private Color color;
    private boolean unbreakable;
    private boolean ench;
    private HashMap<Enchantment, Integer> enchants = new HashMap<>();

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setEnch(Boolean b) {
        this.ench = b;
        return this;
    }

    public ItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder withLores(String... lores) {
        for (String l : lores) {
            this.lores.add("§7"+l);
        }
        return this;
    }

    public ItemBuilder withLores(List<String> lores) {
        for (String lore : lores) {
            this.lores.add(lore);
        }
        return this;
    }

    public ItemBuilder withLore(String lore) {
        lores.add(lore);
        return this;
    }

    public ItemBuilder withDurability(int durability) {
        this.durability = (short) durability;
        return this;
    }

    public ItemBuilder withData(MaterialData data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withPotion(Potion potion) {
        this.potion = potion;
        return this;
    }

    public ItemBuilder setUnbreakable(boolean value) {
        this.unbreakable = value;
        return this;
    }

    public ItemBuilder withColor(Color color) {
        this.color = color;
        return this;
    }

    public ItemBuilder withEnchantment(Enchantment enchantment, int x) {
        enchants.put(enchantment, x);
        return this;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (name != null && name != "") {
            meta.setDisplayName(name);
        }
        if (!lores.isEmpty()) {
            meta.setLore(lores);
        }
        if (durability != -101) {
            item.setDurability(durability);
        }
        if (data != null) {
            item.setData(data);
        }
        if (potion != null && material == Material.POTION) {
            potion.apply(item);
        }
        if (color != null && meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }

        if(unbreakable) {
            meta.spigot().setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        if (ench) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        for (Map.Entry<Enchantment, Integer> set : enchants.entrySet()) {
            meta.addEnchant(set.getKey(), set.getValue(), true);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        if (ench) {
            item.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        }
        return item;
    }

    public ItemStack toPlayerHead(String ownername) {
        ItemStack item = new ItemStack(material, 1, (short) 3);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (name != null && name != "") {
            meta.setDisplayName(name);
        }
        if (!lores.isEmpty()) {
            meta.setLore(lores);
        }
        if (durability != -101) {
            item.setDurability(durability);
        }
        if (data != null) {
            item.setData(data);
        }
        if (potion != null && material == Material.POTION) {
            potion.apply(item);
        }
        if (color != null && meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        ((SkullMeta) meta).setOwner(ownername);
        for (Map.Entry<Enchantment, Integer> set : enchants.entrySet()) {
            meta.addEnchant(set.getKey(), set.getValue(), true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}
