package com.shadowlord.eaglerlogin.listeners;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginListener implements Listener {
  private final EaglerLoginPlugin plugin;
  private final Map<UUID, Deque<Long>> recentJoins = new ConcurrentHashMap<>();
  private final Set<UUID> loggedIn = ConcurrentHashMap.newKeySet();
  private final Map<UUID, BukkitTask> scheduledKick = new ConcurrentHashMap<>();

  private final File usersFile;
  private FileConfiguration usersCfg;

  private final boolean rateLimitEnabled;
  private final int maxReconnects;
  private final int windowSeconds;
  private final int loginTimeoutSeconds;
  private final String loginTimeoutKickMessage;

  public LoginListener(EaglerLoginPlugin plugin) {
    this.plugin = plugin;

    this.rateLimitEnabled = plugin.getConfig().getBoolean("rate-limit.enabled", true);
    this.maxReconnects = plugin.getConfig().getInt("rate-limit.reconnects-per-window", 5);
    this.windowSeconds = plugin.getConfig().getInt("rate-limit.window-seconds", 10);

    this.loginTimeoutSeconds = plugin.getConfig().getInt("login.timeout-seconds", 30);
    this.loginTimeoutKickMessage = plugin.getConfig().getString("login.timeout-kick-message",
        "&cYou did not login in time. Please reconnect and use &6/login <password>&c.");

    usersFile = new File(plugin.getDataFolder(), "users.yml");
    if (!usersFile.exists()) {
      plugin.getDataFolder().mkdirs();
      try { usersFile.createNewFile(); } catch (IOException e) { plugin.getLogger().severe("Could not create users.yml: " + e.getMessage()); }
    }
    usersCfg = YamlConfiguration.loadConfiguration(usersFile);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    final Player p = event.getPlayer();
    final UUID uuid = p.getUniqueId();
    long now = System.currentTimeMillis() / 1000L;

    loggedIn.remove(uuid);
    if (!rateLimitEnabled) {
      sendRequire(p);
      scheduleTimeout(p, uuid);
      return;
    }

    Deque<Long> dq = recentJoins.computeIfAbsent(uuid, k -> new LinkedList<>());
    synchronized (dq) {
      while (!dq.isEmpty() && (now - dq.peekFirst()) >= windowSeconds) dq.pollFirst();
      dq.addLast(now);
      if (dq.size() > maxReconnects) {
        event.setJoinMessage(null);
        int wait = windowSeconds - (int)(now - dq.peekFirst());
        if (wait < 0) wait = 1;
        p.kickPlayer(MsgUtil.color(plugin.getConfig().getString("messages.throttle","&cToo fast. Wait %seconds%").replace("%seconds%", String.valueOf(wait))));
        scheduleCleanup(uuid, windowSeconds + 2);
        return;
      }
    }

    sendRequire(p);
    scheduleTimeout(p, uuid);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    UUID uuid = p.getUniqueId();
    loggedIn.remove(uuid);
    cancelTimeout(uuid);
    scheduleCleanup(uuid, windowSeconds + 2);
  }

  private void sendRequire(Player p) {
    p.sendMessage(MsgUtil.color(plugin.getConfig().getString("messages.welcome","&aWelcome %player%").replace("%player%", p.getName())));
    p.sendMessage(MsgUtil.color(plugin.getConfig().getString("messages.require-login","&eRun /register or /login")));
  }

  // Registration & Login API

  public boolean registerAccount(Player p, String password) {
    UUID uuid = p.getUniqueId();
    String key = uuid.toString();
    if (isRegistered(uuid)) return false;
    try {
      byte[] salt = new byte[16];
      SecureRandom sr = new SecureRandom();
      sr.nextBytes(salt);
      String saltB = Base64.getEncoder().encodeToString(salt);
      String hash = sha256Hex(saltB + password);
      usersCfg.set(key + ".salt", saltB);
      usersCfg.set(key + ".hash", hash);
      usersCfg.set(key + ".name", p.getName());
      saveUsers();
      loggedIn.add(uuid);
      cancelTimeout(uuid);
      return true;
    } catch (Exception e) {
      plugin.getLogger().severe("Register failed: " + e.getMessage());
      return false;
    }
  }

  public boolean attemptLogin(Player p, String password) {
    UUID uuid = p.getUniqueId();
    String key = uuid.toString();
    if (!isRegistered(uuid)) return false;
    if (verifyPassword(key, password)) {
      loggedIn.add(uuid);
      cancelTimeout(uuid);
      return true;
    }
    return false;
  }

  public boolean isRegistered(UUID uuid) {
    String key = uuid.toString();
    return usersCfg.isSet(key + ".hash") && usersCfg.isSet(key + ".salt");
  }

  public boolean isLoggedIn(UUID uuid) {
    return loggedIn.contains(uuid);
  }

  public void saveUsers() {
    try { usersCfg.save(usersFile); } catch (IOException e) { plugin.getLogger().severe("Could not save users.yml: " + e.getMessage()); }
  }

  // Expose plugin getter so other classes can access config safely
  public EaglerLoginPlugin getPlugin() {
    return this.plugin;
  }

  // password helpers
  private boolean verifyPassword(String key, String password) {
    String salt = usersCfg.getString(key + ".salt", null);
    String stored = usersCfg.getString(key + ".hash", null);
    if (salt == null || stored == null) return false;
    return stored.equals(sha256Hex(salt + password));
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(out.length * 2);
      for (byte b : out) sb.append(String.format("%02x", b & 0xff));
      return sb.toString();
    } catch (Exception e) { return ""; }
  }

  // timeout scheduling
  private void scheduleTimeout(final Player p, final UUID uuid) {
    cancelTimeout(uuid);
    BukkitTask t = Bukkit.getScheduler().runTaskLater(plugin, () -> {
      Player pl = Bukkit.getPlayer(uuid);
      if (pl != null && pl.isOnline() && !isLoggedIn(uuid)) {
        pl.kickPlayer(MsgUtil.color(loginTimeoutKickMessage));
      }
      scheduledKick.remove(uuid);
    }, loginTimeoutSeconds * 20L);
    scheduledKick.put(uuid, t);
  }

  private void cancelTimeout(UUID uuid) {
    BukkitTask t = scheduledKick.remove(uuid);
    if (t != null) { try { t.cancel(); } catch (Throwable ignored) {} }
  }

  private void scheduleCleanup(UUID uuid, int delaySeconds) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> recentJoins.remove(uuid), delaySeconds * 20L);
  }
}
