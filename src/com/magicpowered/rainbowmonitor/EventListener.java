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

//        Bukkit.getLogger().info("[彩虹监察] 开始检查玩家打开的容器");

        InventoryHolder holder = event.getInventory().getHolder();
        String containerType;

        if (holder != null) {
            containerType = holder.getClass().getSimpleName();
//            Bukkit.getLogger().info("[彩虹监察] 容器持有者不为 null，容器类型为: " + containerType);
        } else {
//            Bukkit.getLogger().info("[彩虹监察] 容器持有者为 null，检查 GUI 标题是否符合 ChestGui 配置");
            String guiTitle = event.getViewers().isEmpty() ? "" : event.getViewers().get(0).getOpenInventory().getTitle();
            containerType = "未知";

//            Bukkit.getLogger().info("[彩虹监察] 容器的 Title 为 " + guiTitle);

            if (guiTitle.equals("Crafting")) {
                containerType = "末影箱";
            }

            for (Map.Entry<String, List<String>> entry : chestGuiMappings.entrySet()) {
                for (String titlePart : entry.getValue()) {
//                    Bukkit.getLogger().info("[彩虹监察] 遍历 Map 图找到: " + titlePart);
                    if (guiTitle.contains(titlePart)) {
                        containerType = entry.getKey();
//                        Bukkit.getLogger().info("[彩虹监察] GUI 标题匹配 ChestGui 配置，容器类型为: " + containerType);
                        break;
                    }
                }
                if (!containerType.equals("未知")) {
                    break;
                }
            }

            if (containerType.equals("未知")) {
//                Bukkit.getLogger().info("[彩虹监察] GUI 标题未匹配 ChestGui 配置，容器类型保持未知");
            }
        }

        for (CheckItem checkItem : checkItems) {
            int count = 0;
            for (ItemStack itemStack : event.getInventory().getContents()) {
                if (itemStack != null && checkItem.matches(itemStack)) {
                    count += itemStack.getAmount();
                }
            }
            if (count > checkItem.getAmount()) {
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

                Player player = (Player) event.getPlayer();
                Location loc = event.getInventory().getLocation();
                String logLocation = loc != null ? String.format("坐标(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) : "虚拟空间";
                fileManager.addLogEntry(player.getName(), player.getUniqueId(), worldDisplayName, logLocation, containerType, checkItem.getIdentifier(), String.valueOf(count));
            }

        }
    }
}
