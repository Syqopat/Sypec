package com.example.sypec.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.example.sypec.Sypec;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ProtocolHook {

    private final Sypec plugin;
    private boolean hooked = false;

    public ProtocolHook(Sypec plugin) {
        this.plugin = plugin;
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                registerListener();
                this.hooked = true;
            }
        } catch (Throwable t) {
        }
    }

    private void registerListener() {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        pm.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    PacketContainer packet = event.getPacket();
                    Player receiver = event.getPlayer();

                    List<PlayerInfoData> list = packet.getPlayerInfoDataLists().readSafely(0);
                    if (list == null || list.isEmpty()) {
                        return;
                    }

                    boolean modified = false;
                    List<PlayerInfoData> newList = new ArrayList<>();

                    for (PlayerInfoData data : list) {
                        UUID uuid = data.getProfileId();
                        if (ProtocolHook.this.plugin.getSypecManager().isSypecUUID(uuid) && !receiver.getUniqueId().equals(uuid)) {
                            PlayerInfoData fake = new PlayerInfoData(
                                    data.getProfileId(),
                                    data.getLatency(),
                                    data.isListed(),
                                    EnumWrappers.NativeGameMode.SURVIVAL,
                                    data.getProfile(),
                                    data.getDisplayName(),
                                    data.isShowHat(),
                                    data.getListOrder(),
                                    data.getRemoteChatSessionData()
                            );
                            newList.add(fake);
                            modified = true;
                        } else {
                            newList.add(data);
                        }
                    }

                    if (modified) {
                        packet.getPlayerInfoDataLists().write(0, newList);
                    }
                } catch (Throwable t) {
                }
            }
        });
    }

    public void broadcastFakeSurvival(Player sypecPlayer) {
        if (!hooked) {
            return;
        }

        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

            PlayerInfoData data = new PlayerInfoData(
                    sypecPlayer.getUniqueId(),
                    sypecPlayer.getPing(),
                    true,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedGameProfile.fromPlayer(sypecPlayer),
                    WrappedChatComponent.fromText(sypecPlayer.getPlayerListName()),
                    true,
                    0,
                    null
            );

            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(data));

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(sypecPlayer)) {
                    try {
                        pm.sendServerPacket(other, packet);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    public void broadcastRealGameMode(Player sypecPlayer) {
        if (!hooked) {
            return;
        }

        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

            PlayerInfoData data = new PlayerInfoData(
                    sypecPlayer.getUniqueId(),
                    sypecPlayer.getPing(),
                    true,
                    EnumWrappers.NativeGameMode.fromBukkit(sypecPlayer.getGameMode()),
                    WrappedGameProfile.fromPlayer(sypecPlayer),
                    WrappedChatComponent.fromText(sypecPlayer.getPlayerListName()),
                    true,
                    0,
                    null
            );

            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(data));

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(sypecPlayer)) {
                    try {
                        pm.sendServerPacket(other, packet);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    public boolean isHooked() {
        return hooked;
    }
}
