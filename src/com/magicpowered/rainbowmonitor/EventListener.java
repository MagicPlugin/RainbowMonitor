package com.magicpowered.rainbowmonitor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class EventListener implements Listener {
    private FileManager fileManager;
    private List<CheckItem> checkItems;
    private Map<String, List<String>> chestGuiMappings;

    public EventListener(FileManager fileManager, List<CheckItem> checkItems, Map<String, List<String>> chestGuiMappings) {
        this.fileManager = fileManager;
        this.checkItems = checkItems;
        this.chestGuiMappings = chestGuiMappings;
    }

    public void updateCheckItems(List<CheckItem> newCheckItems) {
        this.checkItems = newCheckItems;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (fileManager.isExemptPlayer((Player) event.getPlayer())) {
            return;
        }

        Map<String, String> worldNames = fileManager.getWorldNames();
        String worldKey = event.getPlayer().getWorld().getName();
        String worldDisplayName = worldNames.getOrDefault(worldKey, worldKey);

        if (!worldNames.containsKey(worldKey)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        String containerType = getContainerType(event, holder);

        for (CheckItem checkItem : checkItems) {
            int count = 0;
            for (ItemStack itemStack : event.getInventory().getContents()) {
                if (itemStack != null && checkItem.matches(itemStack)) {
                    count += itemStack.getAmount();
                }
            }
            if (count > checkItem.getAmount()) {
                switch (checkItem.getType()) {
                    case INFORM:
                        informPlayer(event, checkItem, worldDisplayName, containerType, count, checkItem.getType());
                        break;
                    case BOTH:
                        informPlayer(event, checkItem, worldDisplayName, containerType, count, checkItem.getType());
                    case DELETE:
                        for (int i = 0; i < event.getInventory().getSize(); i++) {
                            ItemStack itemStack = event.getInventory().getItem(i);
                            if (itemStack != null && checkItem.matches(itemStack)) {
                                event.getInventory().setItem(i, null);
                            }
                        }

                        createLog(event, checkItem, worldDisplayName, containerType, count, checkItem.getType());
                        break;
                }
            }
        }
    }

    private void informPlayer(InventoryOpenEvent event, CheckItem checkItem, String worldDisplayName, String containerType, int count, RainbowMonitor.Type type) {
        if (fileManager.getSendInfoState()) {
            String playerName = event.getPlayer().getName();
            Location loc = event.getInventory().getLocation();
            String location = loc != null ? String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) : "虚拟空间";
            String message = String.format("§7[§6彩虹监察§7] §7世界(§6%s§7) 的 玩家(§6%s§7) 打开了位于 %s 的容器，其中有超出限制的物品: §6%s§7(§6%s个§7)。", worldDisplayName, playerName, location, checkItem.getIdentifier(), count);
            for (String infoPlayer : fileManager.getSendInfoPlayer()) {
                Player player = Bukkit.getPlayer(infoPlayer);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
        }

        createLog(event, checkItem, worldDisplayName, containerType, count, type);
    }

    private void createLog(InventoryOpenEvent event, CheckItem checkItem, String worldDisplayName, String containerType, int count, RainbowMonitor.Type type) {
        Player player = (Player) event.getPlayer();
        Location loc = event.getInventory().getLocation();
        String logLocation = loc != null ? String.format("坐标(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) : "虚拟空间";
        fileManager.addLogEntry(player.getName(), player.getUniqueId(), worldDisplayName, logLocation, containerType, checkItem.getIdentifier(), String.valueOf(count), type);
    }

    private String getContainerType(InventoryOpenEvent event, InventoryHolder holder) {
        String containerType;
        if (holder != null) {
            containerType = holder.getClass().getSimpleName();
        } else {
            String guiTitle = event.getViewers().isEmpty() ? "" : event.getViewers().get(0).getOpenInventory().getTitle();
            containerType = "未知";

            if (guiTitle.equals("Crafting")) {
                containerType = "末影箱";
            }

            for (Map.Entry<String, List<String>> entry : chestGuiMappings.entrySet()) {
                for (String titlePart : entry.getValue()) {
                    if (guiTitle.contains(titlePart)) {
                        containerType = entry.getKey();
                        break;
                    }
                }
                if (!containerType.equals("未知")) {
                    break;
                }
            }
        }

        return containerType;
    }
}
