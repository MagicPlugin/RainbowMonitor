package com.magicpowered.rainbowmonitor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements CommandExecutor, TabCompleter {
    private RainbowMonitor plugin;
    public CommandListener(RainbowMonitor rainbowMonitor) {
        this.plugin = rainbowMonitor;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§7[§c彩虹监察§7] 帮助:");
            sender.sendMessage("§7  |- 必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
            sender.sendMessage("§7  |- §c/rs reload §7- 重新载入配置文件 (管理员)");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("rainbowskull.reload")) {
                    sender.sendMessage("§7[§c彩虹监察§7] 您没有执行此命令的权限");
                    return true;
                }

                plugin.reloadPlugin();

                sender.sendMessage("§7[§c彩虹监察§7] 配置文件已重新加载");
                return true;

            case "help":
                sender.sendMessage("§7[§c彩虹监察§7] 帮助:");
                sender.sendMessage("§7  |- 必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
                sender.sendMessage("§7  |- §c/rs reload §7- 重新载入配置文件 (管理员)");
                return true;

            default:
                sender.sendMessage("§7[§c彩虹监察§7] 这是一个不存在的命令或拼写错误: " + args[0]);
                sender.sendMessage("§7  |- 输入 §c/rs help §7查看帮助");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command cmd, String s, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!cmd.getName().equalsIgnoreCase("rm")) {
            return completions;
        }

        switch (args.length) {
            case 1:
                completions.addAll(Arrays.asList("reload", "help"));
                break;
        }

        return completions;
    }
}
