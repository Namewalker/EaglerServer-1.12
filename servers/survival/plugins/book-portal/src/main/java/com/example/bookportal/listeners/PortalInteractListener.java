package com.example.bookportal.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PortalInteractListener implements Listener {

    private final JavaPlugin plugin;

    public PortalInteractListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Logic to manage portal appearance based on thrown book
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        // Logic to handle player interaction with the portal
    }
}