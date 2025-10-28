package com.example.bookportal;

import org.bukkit.plugin.java.JavaPlugin;
import com.example.bookportal.listeners.BookThrowListener;
import com.example.bookportal.listeners.PortalInteractListener;

public class BookPortalPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("BookPortal has been enabled!");
        getServer().getPluginManager().registerEvents(new BookThrowListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalInteractListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("BookPortal has been disabled!");
    }
}