package shadowlord.announce;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

public class ScheduledAnnouncement {

    private final ShadowAnnounce plugin;
    private final String id;
    private final String message;
    private final int minutes;
    private BukkitTask task;

    public ScheduledAnnouncement(ShadowAnnounce plugin, String id, String message, int minutes) {
        this.plugin = plugin;
        this.id = id;
        this.message = message;
        this.minutes = minutes;
    }

    public void start() {
        long ticks = minutesToTicks(minutes);
        // schedule sync repeating
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                String prefix = plugin.getConfig().getString("prefix", "");
                String format = plugin.getConfig().getString("default_format", "%message%");
                String msg = format.replace("%message%", message);
                msg = ChatColor.translateAlternateColorCodes('&', prefix + msg);
                Bukkit.broadcastMessage(msg);
            }
        }, ticks, ticks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        plugin.removeSchedule(id);
    }

    private long minutesToTicks(int minutes) {
        return (long) minutes * 60L * 20L;
    }
}
