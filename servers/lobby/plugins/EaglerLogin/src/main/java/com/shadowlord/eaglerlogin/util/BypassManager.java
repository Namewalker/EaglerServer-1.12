package com.shadowlord.eaglerlogin.util;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class BypassManager {
  private final EaglerLoginPlugin plugin;
  private final File file;
  private FileConfiguration cfg;

  public BypassManager(EaglerLoginPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "bypass.yml");
    if (!file.exists()) {
      try {
        plugin.getDataFolder().mkdirs();
        file.createNewFile();
      } catch (IOException e) {
        plugin.getLogger().severe("Could not create bypass.yml: " + e.getMessage());
      }
    }
    this.cfg = YamlConfiguration.loadConfiguration(file);
  }

  public boolean setBypassPassword(String password) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      String saltB = Base64.getEncoder().encodeToString(salt);
      String hash = sha256Hex(saltB + password);
      cfg.set("salt", saltB);
      cfg.set("hash", hash);
      cfg.save(file);
      plugin.getLogger().info("Bypass password set/updated.");
      return true;
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to set bypass password: " + e.getMessage());
      return false;
    }
  }

  public boolean verifyBypassPassword(String password) {
    String salt = cfg.getString("salt", null);
    String hash = cfg.getString("hash", null);
    if (salt == null || hash == null) return false;
    return hash.equals(sha256Hex(salt + password));
  }

  public boolean hasBypassPassword() {
    return cfg.getString("hash", null) != null;
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(out.length * 2);
      for (byte b : out) sb.append(String.format("%02x", b & 0xff));
      return sb.toString();
    } catch (Exception e) {
      return "";
    }
  }
}
