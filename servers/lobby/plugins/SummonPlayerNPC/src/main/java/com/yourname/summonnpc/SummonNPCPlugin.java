package com.yourname.summonnpc;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.yourname.summonnpc.manager.NPCManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SummonNPCPlugin extends JavaPlugin {
    private ProtocolManager protocolManager;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        npcManager = new NPCManager(this, protocolManager);

        getCommand("summonnpc").setExecutor(new commands.SummonNPCCommand(npcManager));
        getLogger().info("SummonPlayerNPC enabled!");
    }

    @Override
    public void onDisable() {
        npcManager.removeAllNPCs();
        getLogger().info("SummonPlayerNPC disabled.");
    }
}
