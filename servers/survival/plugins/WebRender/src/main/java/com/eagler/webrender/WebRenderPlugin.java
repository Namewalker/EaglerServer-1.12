package com.eagler.webrender;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class WebRenderPlugin extends JavaPlugin implements TabExecutor {

    private String rendererUrl = "http://localhost:3000"; // default renderer service

    @Override
    public void onEnable() {
        saveDefaultConfig();
        rendererUrl = getConfig().getString("renderer.url", rendererUrl);
        getCommand("web").setExecutor(this);
        getCommand("web").setTabCompleter(this);
        getLogger().info("WebRender plugin enabled. Renderer URL: " + rendererUrl);
    }

    @Override
    public void onDisable() {
        getLogger().info("WebRender plugin disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/web open <url> - request a render (admin)");
            sender.sendMessage("/web ping - ping renderer");
            sender.sendMessage("/web grant <player> - grant web access to a player (ops only)");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("open")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /web open <url>");
                return true;
            }
            String url = args[1];
            if (!sender.hasPermission("webrender.use")) {
                sender.sendMessage("You don't have permission to use /web open");
                return true;
            }
            if (sender instanceof Player) {
                Player p = (Player) sender;
                // For prototype: call renderer and inform player of result
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        String ping = pingRenderer(url);
                        p.sendMessage("Renderer response: " + ping);
                    } catch (IOException e) {
                        p.sendMessage("Renderer error: " + e.getMessage());
                    }
                });
            } else {
                sender.sendMessage("Command only available to players for now.");
            }
            return true;
        } else if (sub.equals("ping")) {
            try {
                String res = pingRenderer("/ping");
                sender.sendMessage("Renderer says: " + res);
            } catch (IOException e) {
                sender.sendMessage("Renderer ping failed: " + e.getMessage());
            }
            return true;
        } else if (sub.equals("grant")) {
            // grant a player permission to use web commands
            if (!sender.hasPermission("webrender.admin") && !sender.isOp()) {
                sender.sendMessage("You don't have permission to grant web access.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /web grant <player>");
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                sender.sendMessage("Player not online: " + targetName);
                return true;
            }
            // Attach a transient permission allowing web use
            target.addAttachment(this, "webrender.use", true);
            sender.sendMessage("Granted web access to " + target.getName() + " (until server restart)");
            target.sendMessage("You were granted web access by " + sender.getName());
            return true;
        }

        sender.sendMessage("Unknown subcommand");
        return true;
    }

    private String pingRenderer(String pathOrUrl) throws IOException {
        String u = pathOrUrl.startsWith("http") ? pathOrUrl : (rendererUrl + (pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl));
        URL url = new URL(u);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        try (Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
            String body = s.hasNext() ? s.next() : "";
            return code + ":" + (body.length() > 200 ? body.substring(0, 200) + "..." : body);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("open", "ping");
        return Collections.emptyList();
    }
}
