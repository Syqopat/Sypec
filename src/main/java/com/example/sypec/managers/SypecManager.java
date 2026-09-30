package com.example.sypec.managers;

import com.example.sypec.Sypec;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SypecManager {

    private final Sypec plugin;
    private final Map<UUID, SypecSession> activeSessions = new HashMap<>();
    private final BukkitTask reminderTask;
    private final BossBar sypecBossBar;
    private final Random random = new Random();

    public SypecManager(Sypec plugin) {
        this.plugin = plugin;
        this.sypecBossBar = Bukkit.createBossBar("Â§cÂ§l[SYPEC] Â§7Gizli Izleyici Modundasiniz", BarColor.RED, BarStyle.SOLID);
        this.reminderTask = Bukkit.getScheduler().runTaskTimer(plugin, this::sendActionBarReminders, 20L, 20L);
    }

    public boolean isSypec(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public boolean isSypecUUID(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }
    
    public BossBar getBossBar() {
        return sypecBossBar;
    }

    public void enableSypec(Player player, Player target) {
        ConfigManager cfg = plugin.getConfigManager();
        UUID uuid = player.getUniqueId();

        activeSessions.put(uuid, new SypecSession(
                player.getLocation().clone(),
                player.getGameMode(),
                player.isFlying(),
                player.getAllowFlight(),
                player.getInventory().getContents(),
                player.getInventory().getArmorContents()
        ));

        player.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        sypecBossBar.addPlayer(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("sypec.see")) {
                online.hidePlayer(plugin, player);
            }
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        giveSypecItems(player);

        try {
            if (plugin.getProtocolHook() != null && plugin.getProtocolHook().isHooked()) {
                plugin.getProtocolHook().broadcastFakeSurvival(player);
            }
        } catch (Throwable t) {
        }

        if (target != null && target.isOnline()) {
            player.teleport(target.getLocation());
            String msg = cfg.getMessage("messages.teleported-to").replace("%target%", target.getName());
            player.sendMessage(msg);
        } else {
            player.sendMessage(cfg.getMessage("messages.enabled"));
        }

        if (cfg.isEnableSoundEnabled()) {
            player.playSound(player.getLocation(), cfg.getEnableSound(), cfg.getEnableVolume(), cfg.getEnablePitch());
        }
    }

    private void giveSypecItems(Player player) {
        player.getInventory().setItem(0, createItem(Material.COMPASS, "Â§aOyuncu Isinlanma Menusu"));
        player.getInventory().setItem(4, createItem(Material.ENDER_PEARL, "Â§dRastgele Isinlanma"));
        player.getInventory().setItem(7, createItem(Material.FEATHER, "Â§bUcus Hizi Ayari"));
        player.getInventory().setItem(8, createItem(Material.RED_BED, "Â§cSypec Modundan Cik"));
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void disableSypec(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        UUID uuid = player.getUniqueId();
        SypecSession session = activeSessions.remove(uuid);

        if (session == null) {
            return;
        }

        player.setGameMode(session.previousGameMode);
        player.setAllowFlight(session.wasAllowFlight);
        player.setFlying(session.wasFlying);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        sypecBossBar.removePlayer(player);
        player.setFlySpeed(0.1f);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        player.getInventory().setContents(session.inventoryContents);
        player.getInventory().setArmorContents(session.armorContents);

        try {
            if (plugin.getProtocolHook() != null && plugin.getProtocolHook().isHooked()) {
                plugin.getProtocolHook().broadcastRealGameMode(player);
            }
        } catch (Throwable t) {
        }

        if (cfg.isTeleportBackOnDisable()) {
            player.teleport(session.previousLocation);
        }

        player.sendMessage(cfg.getMessage("messages.disabled"));

        if (cfg.isDisableSoundEnabled()) {
            player.playSound(player.getLocation(), cfg.getDisableSound(), cfg.getDisableVolume(), cfg.getDisablePitch());
        }
    }
    
    public void randomTeleport(Player player) {
        List<Player> targets = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && !isSypec(p)) {
                targets.add(p);
            }
        }
        if (targets.isEmpty()) {
            player.sendMessage("Â§cIsinlanacak uygun oyuncu bulunamadi!");
            return;
        }
        Player target = targets.get(random.nextInt(targets.size()));
        player.teleport(target.getLocation());
        player.sendMessage("Â§aRastgele isinlanildi: Â§e" + target.getName());
    }
    
    public void cycleFlightSpeed(Player player) {
        float speed = player.getFlySpeed();
        if (speed < 0.2f) speed = 0.2f;
        else if (speed < 0.4f) speed = 0.4f;
        else if (speed < 0.6f) speed = 0.6f;
        else if (speed < 0.8f) speed = 0.8f;
        else speed = 0.1f;
        player.setFlySpeed(speed);
        player.sendMessage("Â§aUcus hizi guncellendi: Â§e" + (speed * 10));
    }

    public void handlePlayerQuit(Player quitter) {
        if (isSypec(quitter)) {
            disableSypec(quitter);
        }
    }
    
    public void handlePlayerJoin(Player joiner) {
        for (UUID uuid : activeSessions.keySet()) {
            Player sypecPlayer = Bukkit.getPlayer(uuid);
            if (sypecPlayer != null && !joiner.hasPermission("sypec.see")) {
                joiner.hidePlayer(plugin, sypecPlayer);
            }
        }
    }

    public void disableAll() {
        if (reminderTask != null) {
            reminderTask.cancel();
        }
        sypecBossBar.removeAll();

        for (UUID uuid : new HashMap<>(activeSessions).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                disableSypec(player);
            }
        }
        activeSessions.clear();
    }

    private void sendActionBarReminders() {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isActionBarReminderEnabled()) {
            return;
        }

        String text = cfg.getActionBarText();
        for (UUID uuid : activeSessions.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
            }
        }
    }

    private static class SypecSession {
        private final Location previousLocation;
        private final GameMode previousGameMode;
        private final boolean wasFlying;
        private final boolean wasAllowFlight;
        private final ItemStack[] inventoryContents;
        private final ItemStack[] armorContents;

        public SypecSession(Location previousLocation, GameMode previousGameMode, boolean wasFlying, boolean wasAllowFlight, ItemStack[] inventoryContents, ItemStack[] armorContents) {
            this.previousLocation = previousLocation;
            this.previousGameMode = previousGameMode;
            this.wasFlying = wasFlying;
            this.wasAllowFlight = wasAllowFlight;
            this.inventoryContents = inventoryContents;
            this.armorContents = armorContents;
        }
    }
}
