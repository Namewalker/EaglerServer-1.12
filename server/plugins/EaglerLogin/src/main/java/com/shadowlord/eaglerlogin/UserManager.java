package com.shadowlord.eaglerlogin;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
  private final JavaPlugin plugin;
  private final Map<UUID, String> passwordMap = new HashMap<>();

  public UserManager(JavaPlugin plugin) {
    this.plugin = plugin;
    loadAll();
  }

  public boolean isRegistered(UUID uuid) {
    return passwordMap.containsKey(uuid);
  }

  public boolean register(UUID uuid, String rawPassword) {
    if (isRegistered(uuid)) return false;
    String hash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    passwordMap.put(uuid, hash);
    saveAll();
    return true;
  }

  public boolean verify(UUID uuid, String rawPassword) {
    String hash = passwordMap.get(uuid);
    if (hash == null) return false;
    BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hash);
    return result.verified;
  }

  public void saveAll() {
    FileConfiguration cfg = plugin.getConfig();
    for (UUID u : passwordMap.keySet()) {
      cfg.set("users." + u.toString(), passwordMap.get(u));
    }
    plugin.saveConfig();
  }

  private void loadAll() {
    FileConfiguration cfg = plugin.getConfig();
    if (cfg.isConfigurationSection("users")) {
      for (String key : cfg.getConfigurationSection("users").getKeys(false)) {
        passwordMap.put(UUID.fromString(key), cfg.getString("users." + key));
      }
    }
  }
}
