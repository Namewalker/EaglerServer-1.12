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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central account manager and lightweight listener for account storage and session tracking.
 * Exposes public API used by command classes: registerAccount, attemptLogin, setPasswordForUuid, etc.
 */
public class LoginListener implements Listener {

  private final EaglerLoginPlugin plugin;
  private final File usersFile;
  private FileConfiguration usersCfg;
  private final Set<UUID> loggedIn = new HashSet<>();

  public LoginListener(EaglerLoginPlugin plugin) {
    this.plugin = plugin;
    this.usersFile = new File(plugin.getDataFolder(), "users.yml");
    if (!usersFile.exists()) {
      try {
        plugin.getDataFolder().mkdirs();
        usersFile.createNewFile();
      } catch (Exception e) {
        plugin.getLogger().severe("Could not create users.yml: " + e.getMessage());
      }
    }
    this.usersCfg = YamlConfiguration.loadConfiguration(usersFile);
    // Register this listener so join events etc. will work
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  // --- Public API used by commands and other components ---

  // Register a new player account (internal helper)
  public boolean registerPlayer(UUID uuid, String playerName, String password) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      String saltB = Base64.getEncoder().encodeToString(salt);
      String hash = sha256Hex(saltB + password);

      String base = uuid.toString();
      usersCfg.set(base + ".salt", saltB);
      usersCfg.set(base + ".hash", hash);
      usersCfg.set(base + ".name", playerName);
      usersCfg.set(base + ".registered", System.currentTimeMillis());
      usersCfg.save(usersFile);
      plugin.getLogger().info("Registered account for " + playerName + " (" + uuid + ")");
      return true;
    } catch (Exception e) {
      plugin.getLogger().severe("registerPlayer failed: " + e.getMessage());
      return false;
    }
  }

  // Verify a password against stored salt+hash for UUID
  public boolean verifyLogin(UUID uuid, String password) {
    String base = uuid.toString();
    String salt = usersCfg.getString(base + ".salt", null);
    String hash = usersCfg.getString(base + ".hash", null);
    if (salt == null || hash == null) return false;
    return hash.equals(sha256Hex(salt + password));
  }

  // Admin helper to set password for any UUID (create or update)
  public boolean setPasswordForUuid(UUID uuid, String password, String displayName) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      String saltB = Base64.getEncoder().encodeToString(salt);
      String hash = sha256Hex(saltB + password);
      String key = uuid.toString();
      usersCfg.set(key + ".salt", saltB);
      usersCfg.set(key + ".hash", hash);
      usersCfg.set(key + ".name", displayName);
      usersCfg.save(usersFile);
      // Ensure admin-set passwords do not auto-login target
      loggedIn.remove(uuid);
      plugin.getLogger().info("Password set for " + displayName + " (" + uuid + ") by admin");
      return true;
    } catch (Exception e) {
      plugin.getLogger().severe("setPasswordForUuid failed: " + e.getMessage());
      return false;
    }
  }

  // Return stored salt or null
  public String getStoredSalt(UUID uuid) {
    return usersCfg.getString(uuid.toString() + ".salt", null);
  }

  // Return stored hash or null
  public String getStoredHash(UUID uuid) {
    return usersCfg.getString(uuid.toString() + ".hash", null);
  }

  // Mark a player authenticated (used by bypass/login)
  public void markLoggedIn(UUID uuid, String name) {
    loggedIn.add(uuid);
    try {
      usersCfg.set(uuid.toString() + ".name", name);
      usersCfg.set(uuid.toString() + ".lastLogin", System.currentTimeMillis());
      usersCfg.save(usersFile);
    } catch (Exception e) {
      plugin.getLogger().warning("markLoggedIn save failed: " + e.getMessage());
    }
  }

  // Check if UUID currently authenticated in-memory
  public boolean isLoggedIn(UUID uuid) {
    return loggedIn.contains(uuid);
  }

  // Explicitly save users.yml
  public void saveUsers() {
    try {
      usersCfg.save(usersFile);
    } catch (Exception e) {
      plugin.getLogger().warning("saveUsers failed: " + e.getMessage());
    }
  }

  // Give access to plugin instance
  public EaglerLoginPlugin getPlugin() {
    return this.plugin;
  }

  // Convenience checks and wrappers expected by command classes

  public boolean isRegistered(java.util.UUID uuid) {
    return usersCfg.getString(uuid.toString() + ".hash", null) != null;
  }

  // Attempt login for a Player: verifies and marks logged in on success
  public boolean attemptLogin(Player p, String password) {
    java.util.UUID uuid = p.getUniqueId();
    boolean ok = verifyLogin(uuid, password);
    if (ok) {
      markLoggedIn(uuid, p.getName());
    }
    return ok;
  }

  // High-level registerAccount used by RegisterCommand
  public boolean registerAccount(Player p, String password) {
    java.util.UUID uuid = p.getUniqueId();
    if (isRegistered(uuid)) return false;
    return registerPlayer(uuid, p.getName(), password);
  }

  // --- Optional listener behavior (example: announce saved state on join) ---

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    // If player was previously logged in (persist across restarts isn't implemented),
    // you may choose to auto-logout on join. We keep them logged out by default.
    // This is a safe default: remove any persisted "loggedIn" storage if you add it.
    // Ensure a safe message if you want:
    if (!isLoggedIn(p.getUniqueId())) {
      p.sendMessage(MsgUtil.color("&ePlease /register or /login to authenticate."));
    }
  }

  // --- Utility: SHA-256 hash ---
  private String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(out.length * 2);
      for (byte b : out) sb.append(String.format("%02x", b & 0xff));
      return sb.toString();
    } catch (Exception e) {
      plugin.getLogger().severe("sha256Hex failed: " + e.getMessage());
      return "";
    }
  }
}
