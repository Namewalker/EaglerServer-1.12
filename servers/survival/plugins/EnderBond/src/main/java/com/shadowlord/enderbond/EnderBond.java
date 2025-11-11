package shadowlord.enderbond;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EnderBond extends JavaPlugin {

    private final Map<UUID, UUID> companionOf = new HashMap<>(); // owner -> enderman UUID
    private final Map<UUID, Integer> bondLevel = new HashMap<>(); // owner -> bond (0-100)
    private final Set<UUID> vanishedCompanions = new HashSet<>(); // endermans currently in refuge
    private RefugeGenerator refugeGenerator;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadState();

        getCommand("enderbond").setExecutor(new EnderBondCommand(this));
        getServer().getPluginManager().registerEvents(new TamingListener(this), this);
        getServer().getPluginManager().registerEvents(new CompanionListeners(this), this);

        // schedule tasks: bond tick and day/rain checks
        int tick = 20 * 30; // every 30 seconds
        Bukkit.getScheduler().runTaskTimer(this, new BondTickTask(this), 20L, tick);

        // create refuge world if missing
        FileConfiguration cfg = getConfig();
        String refugeName = cfg.getString("dimensions.refuge_name", "ender_refuge");
        World w = Bukkit.getWorld(refugeName);
        if (w == null) {
            WorldCreator wc = new WorldCreator(refugeName);
            wc.generator(new RefugeGenerator()); // custom generator
            w = wc.createWorld();
            if (w != null) {
                getLogger().info("Created refuge world: " + refugeName);
            }
        }

        // ensure refuge world has no hostile spawns
        if (w != null) {
            w.setGameRuleValue("doMobSpawning", "false");
        }

        getLogger().info("EnderBond enabled. Loaded companions: " + companionOf.size());
    }

    @Override
    public void onDisable() {
        saveState();
        getLogger().info("EnderBond disabled.");
    }

    public void loadState() {
        FileConfiguration cfg = getConfig();
        if (cfg.isConfigurationSection("companions")) {
            for (String k : cfg.getConfigurationSection("companions").getKeys(false)) {
                UUID owner = UUID.fromString(k);
                String endStr = cfg.getString("companions." + k + ".companion", null);
                if (endStr != null) companionOf.put(owner, UUID.fromString(endStr));
                bondLevel.put(owner, cfg.getInt("companions." + k + ".bond", 0));
            }
        }
    }

    public void saveState() {
        FileConfiguration cfg = getConfig();
        cfg.set("companions", null);
        for (UUID owner : bondLevel.keySet()) {
            String path = "companions." + owner.toString();
            cfg.set(path + ".bond", bondLevel.getOrDefault(owner, 0));
            UUID c = companionOf.get(owner);
            if (c != null) cfg.set(path + ".companion", c.toString());
        }
        saveConfig();
    }

    // getters and setters
    public Map<UUID, UUID> getCompanionMap() { return companionOf; }
    public Map<UUID, Integer> getBondMap() { return bondLevel; }
    public Set<UUID> getVanished() { return vanishedCompanions; }

    public boolean isRefugeUnlocked(UUID owner) {
        return getBondMap().getOrDefault(owner, 0) >= getConfig().getInt("bonding.refuge_threshold", 80);
    }

    // helper to get refuge world
    public World getRefugeWorld() {
        return Bukkit.getWorld(getConfig().getString("dimensions.refuge_name", "ender_refuge"));
    }
}
