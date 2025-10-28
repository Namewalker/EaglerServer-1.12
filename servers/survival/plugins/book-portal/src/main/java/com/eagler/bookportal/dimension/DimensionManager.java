package com.eagler.bookportal.dimension;

import com.eagler.bookportal.BookPortalPlugin;
import com.eagler.bookportal.util.NameNormalizer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DimensionManager {

    private final BookPortalPlugin plugin;
    private final Random rnd = new Random();

    public DimensionManager(BookPortalPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized World createOrOpenFromTitle(String title, Location portalLocation) {
        String normalized = NameNormalizer.normalize(title);
        if (normalized == null || normalized.isEmpty()) {
            normalized = "random_" + Math.abs(rnd.nextInt(99999));
        }
        final String worldName = "bookportal_" + normalized;
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            plugin.getLogger().info("Opening existing book-portal world: " + worldName);
            // teleport nearest player who threw? we don't have player reference here.
            return w;
        }

        plugin.getLogger().info("Creating book-portal world: " + worldName + " (title='" + title + "')");
        WorldCreator wc = new WorldCreator(worldName);
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);
        w = Bukkit.createWorld(wc);

        if (w == null) {
            plugin.getLogger().warning("Failed to create world: " + worldName);
            return null;
        }

    // populate spawn area asynchronously (schedule sync task for block operations)
    final World worldRef = w;
    final String normalizedKey = normalized;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    int spawnY = 64;
                    int radius = plugin.getConfig().getInt("spawn_radius", 8);
                    // simple keyword-based generation
                    String key = normalizedKey.toLowerCase();
                    Material fill = Material.DIRT;
                    if (key.contains("enchant") || key.contains("lapis") || key.contains("bookshelf")) {
                        fill = Material.BOOKSHELF;
                    } else if (key.contains("ice") || key.contains("frost")) {
                        fill = Material.PACKED_ICE;
                    } else if (key.contains("nether")) {
                        fill = Material.NETHERRACK;
                    } else if (key.contains("lava")) {
                        fill = Material.LAVA;
                    } else if (key.contains("magical")) {
                        fill = Material.ENCHANTMENT_TABLE;
                    } else {
                        // random pick for variety
                        Material[] options = new Material[]{Material.GRASS, Material.BOOKSHELF, Material.STONE, Material.IRON_BLOCK};
                        fill = options[rnd.nextInt(options.length)];
                    }

                    int cx = worldRef.getSpawnLocation().getBlockX();
                    int cz = worldRef.getSpawnLocation().getBlockZ();
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block b = worldRef.getBlockAt(cx + x, spawnY - 1, cz + z);
                            b.setType(fill);
                            // put some enchant tables if keyword
                            if (fill == Material.BOOKSHELF && rnd.nextDouble() < 0.1) {
                                worldRef.getBlockAt(cx + x, spawnY, cz + z).setType(Material.ENCHANTMENT_TABLE);
                            }
                        }
                    }

                    // ensure a portal-like frame or marker near spawn: set obsidian ring
                    for (int x = -2; x <= 2; x++) {
                        worldRef.getBlockAt(cx + x, spawnY, cz - 3).setType(Material.OBSIDIAN);
                        worldRef.getBlockAt(cx + x, spawnY, cz + 3).setType(Material.OBSIDIAN);
                    }
                    for (int z = -3; z <= 3; z++) {
                        worldRef.getBlockAt(cx - 3, spawnY, cz + z).setType(Material.OBSIDIAN);
                        worldRef.getBlockAt(cx + 3, spawnY, cz + z).setType(Material.OBSIDIAN);
                    }

                    // set a simple colored indicator using wool if available
                    Material colorMat = Material.WOOL;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            worldRef.getBlockAt(cx + x, spawnY, cz + z).setType(colorMat);
                        }
                    }

                } catch (Throwable t) {
                    plugin.getLogger().warning("Error populating world: " + t.getMessage());
                }
            }
        }.runTaskLater(plugin, 20L);

        return w;
    }
}
