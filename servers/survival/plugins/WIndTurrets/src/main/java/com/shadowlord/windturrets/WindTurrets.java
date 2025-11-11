package shadowlord.windturrets;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WindTurrets extends JavaPlugin {

    private final Map<String, Turret> turrets = new HashMap<>();
    private TurretFireTask fireTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadTurrets();

        getCommand("turret").setExecutor(new TurretCommand(this));
        getServer().getPluginManager().registerEvents(new TurretPlacementListener(this), this);

        // register wind charge listener (handles snowball hits)
        getServer().getPluginManager().registerEvents(new WindChargeListener(this), this);

        // start repeating fire task
        int interval = getConfig().getInt("fire_interval_ticks", 10);
        fireTask = new TurretFireTask(this);
        Bukkit.getScheduler().runTaskTimer(this, fireTask, interval, interval);

        getLogger().info("WindTurrets enabled. Loaded " + turrets.size() + " turrets.");
    }

    @Override
    public void onDisable() {
        // persist
        saveTurrets();
        if (fireTask != null) fireTask.cancelAll();
        getLogger().info("WindTurrets disabled.");
    }

    public Map<String, Turret> getTurrets() {
        return Collections.unmodifiableMap(turrets);
    }

    public Turret getTurret(String id) {
        return turrets.get(id);
    }

    public void addTurret(Turret t) {
        turrets.put(t.getId(), t);
        saveTurrets();
    }

    public void removeTurret(String id) {
        turrets.remove(id);
        saveTurrets();
    }

    private void loadTurrets() {
        turrets.clear();
        FileConfiguration cfg = getConfig();
        if (!cfg.isConfigurationSection("turrets")) return;
        for (String key : cfg.getConfigurationSection("turrets").getKeys(false)) {
            String path = "turrets." + key;
            try {
                Turret t = Turret.fromConfig(key, cfg.getConfigurationSection(path));
                if (t != null) turrets.put(key, t);
            } catch (Exception e) {
                getLogger().warning("Failed to load turret: " + key + " -> " + e.getMessage());
            }
        }
    }

    public void saveTurrets() {
        FileConfiguration cfg = getConfig();
        cfg.set("turrets", null);
        for (Map.Entry<String, Turret> e : turrets.entrySet()) {
            String path = "turrets." + e.getKey();
            e.getValue().toConfigSection(cfg.createSection(path));
        }
        saveConfig();
    }
}
