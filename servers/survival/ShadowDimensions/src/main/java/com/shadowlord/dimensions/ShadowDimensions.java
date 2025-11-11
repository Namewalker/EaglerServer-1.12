package shadowlord.dimensions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Core plugin class for ShadowDimensions.
 * - Maintains an in-memory list of portals loaded from config on enable
 * - Provides robust save/remove methods that persist to config.yml
 * - Creates worlds via WorldCreator when requested
 */
public class ShadowDimensions extends JavaPlugin {

    private SelectionManager selectionManager;
    private final List<Portal> portals = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        selectionManager = new SelectionManager(this);

        // Register commands and listeners
        getCommand("dimension").setExecutor(new DimensionCommand(this));
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);

        // Load portals from config into memory
        loadPortals();

        getLogger().info("ShadowDimensions enabled.");
    }

    @Override
    public void onDisable() {
        // ensure any in-memory state is persisted (should already be persisted on each change)
        saveConfig();
        getLogger().info("ShadowDimensions disabled.");
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Save a portal to memory and persist the updated list to config.yml
     */
    public void savePortal(Portal portal) {
        // add to memory
        portals.add(portal);

        // persist
        FileConfiguration cfg = getConfig();
        List<String> serialized = new ArrayList<>();
        for (Portal p : portals) {
            serialized.add(p.serialize());
        }
        cfg.set("portals", serialized);
        saveConfig();

        getLogger().info("Saved portal: " + portal.serialize());
    }

    /**
     * Remove a portal from memory and persist the updated list to config.yml
     */
    public void removePortal(Portal portal) {
        // remove from memory
        boolean removed = portals.removeIf(p -> p.getName().equals(portal.getName()));
        if (!removed) {
            getLogger().warning("Attempted to remove a portal that was not in memory: " + portal.getName());
        }

        // persist
        FileConfiguration cfg = getConfig();
        List<String> serialized = new ArrayList<>();
        for (Portal p : portals) {
            serialized.add(p.serialize());
        }
        cfg.set("portals", serialized);
        saveConfig();

        getLogger().info("Removed portal: " + portal.getName());
    }

    /**
     * Return a copy of all loaded portals (safe for callers to iterate)
     */
    public List<Portal> getAllPortals() {
        return new ArrayList<>(portals);
    }

    /**
     * Load portals from config.yml into the in-memory list.
     * Silently skips invalid entries but logs them.
     */
    private void loadPortals() {
        portals.clear();
        FileConfiguration cfg = getConfig();
        List<String> list = cfg.getStringList("portals");
        if (list == null || list.isEmpty()) {
            getLogger().info("No portals found in config.");
            return;
        }

        for (String s : list) {
            Portal p = Portal.deserialize(s);
            if (p != null) {
                portals.add(p);
                getLogger().info("Loaded portal -> " + p.getName() + " -> " + p.getTargetDimension() + " in " + p.getWorldName());
            } else {
                getLogger().warning("Failed to deserialize portal entry: " + s);
            }
        }
    }

    /**
     * Create (or load if already present) a world with the given name/type/seed.
     * Returns the created or existing World instance, or null on failure.
     */
    public World createDimension(String name, String type, Long seed) {
        if (name == null || name.isEmpty()) return null;

        World.Environment env = World.Environment.NORMAL;
        if ("nether".equalsIgnoreCase(type)) env = World.Environment.NETHER;
        else if ("end".equalsIgnoreCase(type)) env = World.Environment.THE_END;
        // 'void' is treated as NORMAL here; you can handle special generation later

        try {
            World existing = Bukkit.getWorld(name);
            if (existing != null) return existing;

            WorldCreator wc = new WorldCreator(name).environment(env);
            if (seed != null) wc.seed(seed);
            World w = wc.createWorld();
            if (w == null) {
                getLogger().warning("WorldCreator returned null for: " + name);
            } else {
                getLogger().info("Created/loaded world: " + name + " (env=" + env + ")");
            }
            return w;
        } catch (Exception ex) {
            getLogger().severe("Failed to create world " + name + ": " + ex.getMessage());
            return null;
        }
    }
}
