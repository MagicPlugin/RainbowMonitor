package com.magicpowered.rainbowmonitor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CheckItem {

    private String identifier;

    private Material material;
    private String name;
    private List<String> lore;
    private int amount;
    private RainbowMonitor.Type type;


    // 构造函数、getters 和 setters
    public CheckItem(String identifier, Material material, String name, List<String> lore, int amount, RainbowMonitor.Type type) {
        this.identifier = identifier;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.type = type;
    }

    public boolean matches(ItemStack itemStack) {

        // 检查物品材质
        if (material != null) {
            if (itemStack.getType() != material) {
                return false;
            }
        }

        // 检查物品名称
        if (name != null && !name.isEmpty()) {
            String itemName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()) : "";
            if (!itemName.contains(name)) {
                return false;
            }
        }

        // 检查物品描述
        if (lore != null && !lore.isEmpty()) {
            if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) {
                return false;
            }
            List<String> itemLore = itemStack.getItemMeta().getLore();
            for (String line : lore) {
                boolean lineMatch = false;
                for (String loreLine : itemLore) {
                    if (ChatColor.stripColor(loreLine).equals(line)) {
                        lineMatch = true;
                        break;
                    }
                }
                if (!lineMatch) {
                    return false;
                }
            }
        }

        return true;
    }

    public int getAmount() {
        return amount;
    }

    public String getIdentifier() {return identifier;}

    public RainbowMonitor.Type getType() {
        return type;
    }
}
