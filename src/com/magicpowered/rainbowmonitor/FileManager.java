package com.magicpowered.rainbowmonitor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class FileManager {
    private final RainbowMonitor plugin;
    private FileConfiguration config;
    private FileConfiguration inventory;

    private File configFile;
    private File inventoryFile;

    public FileManager(RainbowMonitor plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            config.setDefaults(defaultConfig);
        }

        if (inventoryFile == null) {
            inventoryFile = new File(plugin.getDataFolder(), "inventory.yml");
        }
        inventory = YamlConfiguration.loadConfiguration(inventoryFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public FileConfiguration getInventory() {
        if (inventory == null) {
            reloadConfig();
        }
        return inventory;
    }


    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveInventory() {
        if (inventory == null || inventoryFile == null) {
            return;
        }
        try {
            getInventory().save(inventoryFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save log to " + inventoryFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }


        if (inventoryFile == null) {
            inventoryFile = new File(plugin.getDataFolder(), "inventory.yml");
        }
        if (!inventoryFile.exists()) {
            plugin.saveResource("inventory.yml", false);
        }
    }

    // 读取物品监察列表
    public List<CheckItem> loadCheckItems() {
        Bukkit.getLogger().info("[彩虹监察] 读取物品监察列表");

        List<CheckItem> checkItems = new ArrayList<>();
        for (String key : getConfig().getConfigurationSection("ItemsToCheck").getKeys(false)) {
            Material material = Material.matchMaterial(Objects.requireNonNull(config.getString("ItemsToCheck." + key + ".Material")));
            String name = getConfig().getString("ItemsToCheck." + key + ".Name");
            List<String> lore = getConfig().getStringList("ItemsToCheck." + key + ".Lore");
            int amount = getConfig().getInt("ItemsToCheck." + key + ".Amount");
            checkItems.add(new CheckItem(key, material, name, lore, amount));
        }
        return checkItems;
    }

    public boolean getSendInfoState() {
        return getConfig().getBoolean("SendInfo.state", true);
    }

    public List<String> getSendInfoPlayer() {
        return getConfig().getStringList("SendInfo.Player");
    }

    // 记录日志
    public void addLogEntry(String playerName, UUID uuid, String world, String location, String containerType, String itemIdentifier, String count) {
        FileConfiguration logConfig = getInventory();
        String path = playerName + "(" + uuid.toString() + ")";
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-HH:mm:ss"));
        String message;

        if (location.equals("虚拟空间")) {
            message = String.format("@ %s -> 访问的世界(%s) 的 %s 处的 %s 中有超出限制的物品: %s(%s个)", currentTime, world, location, translateContainerType(containerType), itemIdentifier, count);
        } else {
            message = String.format("@ %s -> 访问的世界(%s) 的 %s 处的 %s 中有超出限制的物品: %s(%s个)", currentTime, world, location, translateContainerType(containerType), itemIdentifier, count);
        }

        List<String> existingLogs = logConfig.getStringList(path);
        existingLogs.add(message);
        logConfig.set(path, existingLogs);
        saveInventory();
    }

    private String translateContainerType(String containerType) {
        switch (containerType) {
            case "CraftChest":
                return "箱子";
            case "DoubleChest":
                return "大箱子";
            case "CraftHopper":
                return "漏斗";
            case "CraftShulkerBox":
                return "潜影盒";
            case "CraftBarrel":
                return "木桶";
            case "CraftDispenser":
                return "发射器";
            case "CraftDropper":
                return "投掷器";
            default:
                return containerType; // 如果没有匹配的翻译，返回原始类型
        }
    }

    public Map<String, String> getWorldNames() {
        FileConfiguration config = getConfig();
        Map<String, String> worldNames = new HashMap<>();
        if (config.isConfigurationSection("World")) {
            for (String key : config.getConfigurationSection("World").getKeys(false)) {
                worldNames.put(key, config.getString("World." + key));
            }
        }
        return worldNames;
    }

    // 监察豁免玩家
    public boolean isExemptPlayer(Player player) {
        String name = player.getName();
        List<String> players = getConfig().getStringList("Exempt");
        if (players.contains(name)) return true;
        return false;
    }

    // 读取容器
    public Map<String, List<String>> loadChestGuiMappings() {
        FileConfiguration config = getConfig();
        Map<String, List<String>> chestGuiMappings = new HashMap<>();
        if (config.isConfigurationSection("ChestGui")) {
            for (String key : config.getConfigurationSection("ChestGui").getKeys(false)) {
                List<String> titles = config.getStringList("ChestGui." + key);
                chestGuiMappings.put(key, titles);
            }
        }
        return chestGuiMappings;
    }
}
