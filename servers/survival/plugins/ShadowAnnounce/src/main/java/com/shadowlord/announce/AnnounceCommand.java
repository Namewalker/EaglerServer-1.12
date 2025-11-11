package shadowlord.announce;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class AnnounceCommand implements CommandExecutor {

    private final ShadowAnnounce plugin;

    public AnnounceCommand(ShadowAnnounce plugin) {
        this.plugin = plugin;
    }

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(colorize(plugin.getConfig().getString("prefix", "") + "&cUsage: /announce <message> | /announce title <title> <message> | /announce schedule <message> <minutes>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("title")) {
            if (!sender.hasPermission("shadowannounce.use")) {
                sender.sendMessage(colorize("&cNo permission."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(colorize("&cUsage: /announce title <title> <message>"));
                return true;
            }
            String title = Util.join(args, 1, 2);
            String message = Util.join(args, 2, args.length);
            broadcastTitle(title, message);
            sender.sendMessage(colorize(plugin.getConfig().getString("prefix", "") + "&aTitle announcement sent."));
            return true;
        } else if (sub.equals("schedule")) {
            if (!sender.hasPermission("shadowannounce.admin")) {
                sender.sendMessage(colorize("&cNo permission."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(colorize("&cUsage: /announce schedule <message> <minutes>"));
                return true;
            }
            String minutesRaw = args[args.length - 1];
            int minutes;
            try {
                minutes = Integer.parseInt(minutesRaw);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&cLast argument must be minutes as an integer."));
                return true;
            }
            if (minutes <= 0) {
                sender.sendMessage(colorize("&cInterval must be a positive integer."));
                return true;
            }
            String message = Util.join(args, 1, args.length - 1);
            String id = UUID.randomUUID().toString().substring(0, 8);
            plugin.saveSchedule(id, message, minutes);
            ScheduledAnnouncement sa = new ScheduledAnnouncement(plugin, id, message, minutes);
            sa.start();
            sender.sendMessage(colorize(plugin.getConfig().getString("prefix", "") + "&aScheduled announcement created with id: &e" + id));
            return true;
        } else {
            // Plain broadcast
            if (!sender.hasPermission("shadowannounce.use")) {
                sender.sendMessage(colorize("&cNo permission."));
                return true;
            }
            String message = Util.join(args, 0, args.length);
            broadcastMessage(message);
            sender.sendMessage(colorize(plugin.getConfig().getString("prefix", "") + "&aAnnouncement broadcasted."));
            return true;
        }
    }

    private void broadcastMessage(String raw) {
        String prefix = plugin.getConfig().getString("prefix", "");
        String format = plugin.getConfig().getString("default_format", "%message%");
        String msg = format.replace("%message%", raw);
        msg = colorize(prefix + msg);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }

        if (plugin.getConfig().getBoolean("enable_sound", false)) {
            // No specific sound by default; skip for 1.12 compatibility if not configured
        }
    }

    private void broadcastTitle(String titleRaw, String messageRaw) {
        String title = colorize(titleRaw);
        String subtitle = colorize(messageRaw);
        int fadeIn = 10;
        int stay = 60;
        int fadeOut = 10;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }
}
