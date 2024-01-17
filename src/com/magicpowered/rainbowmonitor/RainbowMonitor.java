package com.magicpowered.rainbowmonitor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class RainbowMonitor extends JavaPlugin implements Listener {
    private FileManager fileManager;
    private CommandListener commandListener;
    private EventListener eventListener;
    private List<CheckItem> checkItems;
    private Map<String, List<String>> chestGui;

    @Override
    public void onEnable() {
        try {
            fileManager = new FileManager(this);

            fileManager.reloadConfig();

            checkItems = fileManager.loadCheckItems();
            chestGui = fileManager.loadChestGuiMappings();

            eventListener = new EventListener(fileManager, checkItems, chestGui);
            commandListener = new CommandListener(this);

            getServer().getPluginManager().registerEvents(eventListener, this);
            getServer().getPluginManager().registerEvents(this, this);
            getCommand("rm").setExecutor(commandListener);
            getCommand("rm").setTabCompleter(commandListener);

            Bukkit.getServer().getLogger().info(" ");
            Bukkit.getServer().getLogger().info("  '||    ||' '||''|.         '||''|.   '||    ||'    妙控动力 MagicPowered");
            Bukkit.getServer().getLogger().info("   |||  |||   ||   ||   ||    ||   ||   |||  |||     彩虹系列 RainbowSeries");
            Bukkit.getServer().getLogger().info("   |'|..'||   ||...|'         || ''|'   |'|..'||     彩虹监察 RainbowMonitor v24.0.1.0");
            Bukkit.getServer().getLogger().info("   | '|' ||   ||        ||    ||   |.   | '|' ||     由 JLING 制作");
            Bukkit.getServer().getLogger().info("  .|. | .||. .||.            .||.  '|' .|. | .||.    https://magicpowered.cn");
            Bukkit.getServer().getLogger().info(" ");
        } catch (
                Exception e) {
            Bukkit.getServer().getLogger().info("[彩虹监察] 启动失败!");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("RainbowMonitor")) {
            Bukkit.getServer().getLogger().info("[彩虹监察] 彩虹光照，世界依然，再会!");
        }
    }

    @Override
    public void onDisable() {
        fileManager.saveConfig();
        fileManager.saveInventory();
    }

    public void reloadPlugin() {
        fileManager.reloadConfig();
        checkItems = fileManager.loadCheckItems();
        chestGui = fileManager.loadChestGuiMappings();

        // 更新 EventListener 的 checkItems
        if (eventListener != null) {
            eventListener.updateCheckItems(checkItems);
        } else {
            eventListener = new EventListener(fileManager, checkItems, chestGui);
            getServer().getPluginManager().registerEvents(eventListener, this);
        }
    }
}
