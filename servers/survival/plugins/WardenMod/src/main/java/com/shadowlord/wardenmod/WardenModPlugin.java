package com.shadowlord.wardenmod;

import com.shadowlord.wardenmod.commands.*;
import com.shadowlord.wardenmod.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class WardenModPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register command executors
        getCommand("warden").setExecutor(new WardenCommand(this));
        getCommand("shrieker").setExecutor(new ShriekerCommand(this));
        getCommand("skulk").setExecutor(new SkulkCommand(this));
        getCommand("skulksensor").setExecutor(new SkulkSensorCommand(this));
        
        // Register event listeners
        WardenListener wardenListener = new WardenListener(this);
        ShriekerListener shriekerListener = new ShriekerListener(this);
        SkulkSensorListener sensorListener = new SkulkSensorListener(this);
        
        // Connect listeners for cross-communication
        shriekerListener.setWardenListener(wardenListener);
        sensorListener.setShriekerListener(shriekerListener);
        
        getServer().getPluginManager().registerEvents(wardenListener, this);
        getServer().getPluginManager().registerEvents(shriekerListener, this);
        getServer().getPluginManager().registerEvents(sensorListener, this);
        SkulkListener skulkListener = new SkulkListener(this);
        // Connect sensor and skulk listeners
        skulkListener.setSensorListener(sensorListener);
        getServer().getPluginManager().registerEvents(skulkListener, this);
        getServer().getPluginManager().registerEvents(new SneakListener(this), this);
        
        getLogger().info("WardenMod enabled with laser attacks, darkness effect, and Warden emergence!");
    }

    @Override
    public void onDisable() {
        getLogger().info("WardenMod disabled.");
    }
}
