package com.example.sypec.commands;

import com.example.sypec.Sypec;
import com.example.sypec.managers.ConfigManager;
import com.example.sypec.managers.SypecManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SypecCommand implements CommandExecutor, TabCompleter {

    private final Sypec plugin;

    public SypecCommand(Sypec plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("sypec.admin")) {
                sender.sendMessage(cfg.getMessage("messages.no-permission"));
                return true;
            }
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(cfg.getMessage("messages.reload"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(cfg.getMessage("messages.only-players"));
            return true;
        }

        if (!player.hasPermission("sypec.use")) {
            player.sendMessage(cfg.getMessage("messages.no-permission"));
            return true;
        }

        SypecManager manager = plugin.getSypecManager();

        if (args.length == 0) {
            if (manager.isSypec(player)) {
                manager.disableSypec(player);
            } else {
                manager.enableSypec(player, null);
            }
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMessage("messages.cannot-target-self"));
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(cfg.getMessage("messages.player-not-found"));
            return true;
        }

        if (manager.isSypec(player)) {
            player.teleport(target.getLocation());
            String msg = cfg.getMessage("messages.teleported-to").replace("%target%", target.getName());
            player.sendMessage(msg);
        } else {
            manager.enableSypec(player, target);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String query = args[0].toLowerCase();
            if (sender.hasPermission("sypec.admin") && "reload".startsWith(query)) {
                completions.add("reload");
            }
            if (sender.hasPermission("sypec.use")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getName().equalsIgnoreCase(sender.getName()) && p.getName().toLowerCase().startsWith(query)) {
                        completions.add(p.getName());
                    }
                }
            }
        }
        return completions;
    }
}
