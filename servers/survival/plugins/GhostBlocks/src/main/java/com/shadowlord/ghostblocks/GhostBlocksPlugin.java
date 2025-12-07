package com.shadowlord.ghostblocks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitRunnable;

public class GhostBlocksPlugin extends JavaPlugin {
    private static GhostBlocksPlugin instance;
    private GhostBlockManager manager;
    private Logger log;

    public static GhostBlocksPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        log = getLogger();
        saveDefaultConfig();
        manager = new GhostBlockManager(this);
        manager.loadFromDisk();

        if (getCommand("ghostblocks") != null) {
            getCommand("ghostblocks").setExecutor(new GhostBlocksCommand(manager));
            getCommand("ghostblocks").setTabCompleter(new GhostBlocksCommand(manager));
        }

        Bukkit.getPluginManager().registerEvents(new GhostBlockListener(manager), this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    manager.renderChunkIllusions(chunk);
                }
            }
        }, 1L);

        // Enforce server-side AIR for ghost locations periodically to avoid other plugins or physics making them solid
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    manager.enforceAirNearPlayers();
                } catch (Throwable t) {
                    getLogger().warning("Error enforcing ghost block air: " + t.getMessage());
                }
            }
        }.runTaskTimer(this, 2L, 5L);

        log.info("GhostBlocks enabled. Loaded " + manager.count() + " ghost blocks.");
    }

    @Override
    public void onDisable() {
        manager.saveToDisk();
        log.info("GhostBlocks disabled. Saved " + manager.count() + " ghost blocks.");
    }

    public FileConfiguration config() {
        return getConfig();
    }
}
