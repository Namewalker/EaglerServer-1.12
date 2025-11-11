package shadowlord.announce;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class ShadowAnnounce extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();

        AnnounceCommand cmd = new AnnounceCommand(this);
        getCommand("announce").setExecutor(cmd);

        // Load any saved schedules
        if (cfg.isConfigurationSection("schedules")) {
            for (String key : cfg.getConfigurationSection("schedules").getKeys(false)) {
                String message = cfg.getString("schedules." + key + ".message");
                int minutes = cfg.getInt("schedules." + key + ".interval");
                if (message != null && minutes > 0) {
                    ScheduledAnnouncement sa = new ScheduledAnnouncement(this, key, message, minutes);
                    sa.start();
                }
            }
        }

        getLogger().info("ShadowAnnounce enabled.");
    }

    @Override
    public void onDisable() {
        // Cancel all tasks started by this plugin
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("ShadowAnnounce disabled.");
    }

    public void saveSchedule(String id, String message, int minutes) {
        FileConfiguration cfg = getConfig();
        cfg.set("schedules." + id + ".message", message);
        cfg.set("schedules." + id + ".interval", minutes);
        saveConfig();
    }

    public void removeSchedule(String id) {
        FileConfiguration cfg = getConfig();
        cfg.set("schedules." + id, null);
        saveConfig();
    }
}
