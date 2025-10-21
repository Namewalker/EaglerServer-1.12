package com.yourname.summonnpc.manager;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class NPCManager {
    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, Integer> spawnedNPCs = new HashMap<>();

    public NPCManager(JavaPlugin plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
    }

    public void spawnNPC(String playerName, Location loc) {
        // Fetch offline player and skin data
        org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offline.getUniqueId();
        WrappedGameProfile profile = WrappedGameProfile.fromOfflinePlayer(offline);

        // Create and send Player Info ADD packet
        PacketContainer addInfo = protocolManager.createPacket(
                com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO);
        addInfo.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        addInfo.getPlayerInfoDataLists().write(0,
                Collections.singletonList(new PlayerInfoData(profile, 1, 
                    EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerName))));
        protocolManager.broadcastServerPacket(addInfo);

        // Create and send Named Entity Spawn packet
        PacketContainer spawn = protocolManager.createPacket(
                com.comphenix.protocol.PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawn.getIntegers().write(0, /* entityId */ loc.hashCode() & 0x7FFFFFFF);
        spawn.getUUIDs().write(0, uuid);
        spawn.getDoubles().write(0, loc.getX());
        spawn.getDoubles().write(1, loc.getY());
        spawn.getDoubles().write(2, loc.getZ());
        protocolManager.broadcastServerPacket(spawn);

        // Store to remove later if needed
        spawnedNPCs.put(uuid, loc.hashCode() & 0x7FFFFFFF);
    }

    public void removeAllNPCs() {
        for (UUID uuid : spawnedNPCs.keySet()) {
            PacketContainer removeInfo = protocolManager.createPacket(
                    com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO);
            removeInfo.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            removeInfo.getPlayerInfoDataLists().write(0,
                    Collections.singletonList(new PlayerInfoData(
                        WrappedGameProfile.fromHandle(null), 0, 
                        null, null)));
            protocolManager.broadcastServerPacket(removeInfo);
        }
        spawnedNPCs.clear();
    }
}
