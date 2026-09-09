package com.example.sypec.listeners;

import com.example.sypec.Sypec;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SypecListener implements Listener {

    private final Sypec plugin;

    public SypecListener(Sypec plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getSypecManager().handlePlayerQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getSypecManager().handlePlayerJoin(event.getPlayer());
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSypecManager().isSypec(player)) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.COMPASS) {
                openTeleportMenu(player);
                event.setCancelled(true);
            } else if (item.getType() == Material.ENDER_PEARL) {
                plugin.getSypecManager().randomTeleport(player);
                event.setCancelled(true);
            } else if (item.getType() == Material.FEATHER) {
                plugin.getSypecManager().cycleFlightSpeed(player);
                event.setCancelled(true);
            } else if (item.getType() == Material.RED_BED) {
                plugin.getSypecManager().disableSypec(player);
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getView().getTitle().equals("§8Oyuncu Isinlanma Menusu")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String targetName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null && target.isOnline()) {
                    player.teleport(target.getLocation());
                    player.sendMessage("§aIsinlanildi: §e" + target.getName());
                } else {
                    player.sendMessage("§cOyuncu cevrimdisi.");
                }
                player.closeInventory();
            }
            return;
        }
        
        if (plugin.getSypecManager().isSypec(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getSypecManager().isSypec(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    private void openTeleportMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Oyuncu Isinlanma Menusu");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(p);
                    meta.setDisplayName("§a" + p.getName());
                    head.setItemMeta(meta);
                }
                inv.addItem(head);
            }
        }
        player.openInventory(inv);
    }
}
