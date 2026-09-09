package com.example.sypec;

import com.example.sypec.commands.SypecCommand;
import com.example.sypec.listeners.SypecListener;
import com.example.sypec.managers.ConfigManager;
import com.example.sypec.managers.SypecManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Sypec extends JavaPlugin {

    private ConfigManager configManager;
    private SypecManager sypecManager;
    private com.example.sypec.hooks.ProtocolHook protocolHook;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.sypecManager = new SypecManager(this);
        this.protocolHook = new com.example.sypec.hooks.ProtocolHook(this);

        getServer().getPluginManager().registerEvents(new SypecListener(this), this);

        var cmd = getCommand("sypec");
        if (cmd != null) {
            SypecCommand executor = new SypecCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        if (sypecManager != null) {
            sypecManager.disableAll();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SypecManager getSypecManager() {
        return sypecManager;
    }

    public com.example.sypec.hooks.ProtocolHook getProtocolHook() {
        return protocolHook;
    }
}
