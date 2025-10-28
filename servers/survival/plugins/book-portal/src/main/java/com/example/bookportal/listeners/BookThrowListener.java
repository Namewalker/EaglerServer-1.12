package com.example.bookportal.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerThrowItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class BookThrowListener implements Listener {

    public BookThrowListener() {
        Bukkit.getPluginManager().registerEvents(this, BookPortalPlugin.getInstance());
    }

    @EventHandler
    public void onBookThrow(PlayerThrowItemEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item.getType() == Material.WRITTEN_BOOK) {
            String bookName = item.getItemMeta().getDisplayName();
            createDimensionFromBook(bookName, player);
            event.setCancelled(true);
        }
    }

    private void createDimensionFromBook(String bookName, Player player) {
        // Logic to create a new dimension based on the book's name
        // This will involve calling DimensionManager and handling variations
        // For example:
        DimensionManager.createDimension(bookName, player);
    }
}