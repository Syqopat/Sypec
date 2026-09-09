package com.example.sypec.managers;

import com.example.sypec.Sypec;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class ConfigManager {

    private final Sypec plugin;

    public ConfigManager(Sypec plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public String getMessage(String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg == null || msg.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public boolean isTeleportBackOnDisable() {
        return plugin.getConfig().getBoolean("settings.teleport-back-on-disable", true);
    }

    public boolean isHideFromTab() {
        return plugin.getConfig().getBoolean("settings.hide-from-tab", true);
    }

    public boolean isActionBarReminderEnabled() {
        return plugin.getConfig().getBoolean("settings.actionbar-reminder", true);
    }

    public String getActionBarText() {
        String text = plugin.getConfig().getString("settings.actionbar-text", "&a&l[SYPEC] &7Gizli izleyici modundasiniz! Cikmak icin: &e/sypec");
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getSeePermission() {
        return plugin.getConfig().getString("settings.see-permission", "sypec.see");
    }

    public boolean isEnableSoundEnabled() {
        return plugin.getConfig().getBoolean("sounds.enable.enabled", true);
    }

    public Sound getEnableSound() {
        String name = plugin.getConfig().getString("sounds.enable.sound", "ENTITY_ILLUSIONER_CAST_SPELL");
        try {
            return Sound.valueOf(name);
        } catch (Exception e) {
            return Sound.ENTITY_ILLUSIONER_CAST_SPELL;
        }
    }

    public float getEnableVolume() {
        return (float) plugin.getConfig().getDouble("sounds.enable.volume", 1.0);
    }

    public float getEnablePitch() {
        return (float) plugin.getConfig().getDouble("sounds.enable.pitch", 1.2);
    }

    public boolean isDisableSoundEnabled() {
        return plugin.getConfig().getBoolean("sounds.disable.enabled", true);
    }

    public Sound getDisableSound() {
        String name = plugin.getConfig().getString("sounds.disable.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            return Sound.valueOf(name);
        } catch (Exception e) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }

    public float getDisableVolume() {
        return (float) plugin.getConfig().getDouble("sounds.disable.volume", 1.0);
    }

    public float getDisablePitch() {
        return (float) plugin.getConfig().getDouble("sounds.disable.pitch", 0.8);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }
}
