package com.shadowlord.graveyard;

import com.shadowlord.graveyard.model.Graveyard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class GraveyardManager {
  private final GraveyardPlugin plugin;
  private final Map<UUID, Graveyard> graves = new HashMap<>(); // owner -> grave
  private final Map<UUID, Location> playerBound = new HashMap<>(); // player -> bound grave center

  public GraveyardManager(GraveyardPlugin plugin) {
    this.plugin = plugin;
    loadAll();
  }

  public GraveyardPlugin getPlugin() {
    return plugin;
  }

  public void saveAll() {
    plugin.getConfig().set("graves", null);
    plugin.getConfig().set("bindings", null);
    for (Map.Entry<UUID, Graveyard> e : graves.entrySet()) {
      String k = e.getKey().toString();
      Location c = e.getValue().getCenter();
      plugin.getConfig().set("graves." + k + ".world", c.getWorld().getName());
      plugin.getConfig().set("graves." + k + ".x", c.getX());
      plugin.getConfig().set("graves." + k + ".y", c.getY());
      plugin.getConfig().set("graves." + k + ".z", c.getZ());
      plugin.getConfig().set("graves." + k + ".level", e.getValue().getLevel());
    }
    for (Map.Entry<UUID, Location> b : playerBound.entrySet()) {
      String k = b.getKey().toString();
      Location c = b.getValue();
      plugin.getConfig().set("bindings." + k + ".world", c.getWorld().getName());
      plugin.getConfig().set("bindings." + k + ".x", c.getX());
      plugin.getConfig().set("bindings." + k + ".y", c.getY());
      plugin.getConfig().set("bindings." + k + ".z", c.getZ());
    }
    plugin.saveConfig();
  }

  private void loadAll() {
    if (plugin.getConfig().isConfigurationSection("graves")) {
      for (String key : plugin.getConfig().getConfigurationSection("graves").getKeys(false)) {
        try {
          UUID owner = UUID.fromString(key);
          String base = "graves." + key;
          String w = plugin.getConfig().getString(base + ".world");
          double x = plugin.getConfig().getDouble(base + ".x");
          double y = plugin.getConfig().getDouble(base + ".y");
          double z = plugin.getConfig().getDouble(base + ".z");
          int lvl = plugin.getConfig().getInt(base + ".level", 1);
          Location loc = new Location(Bukkit.getWorld(w), x, y, z);
          graves.put(owner, new Graveyard(loc, lvl));
        } catch (Exception ignored) {}
      }
    }
    if (plugin.getConfig().isConfigurationSection("bindings")) {
      for (String key : plugin.getConfig().getConfigurationSection("bindings").getKeys(false)) {
        try {
          UUID pid = UUID.fromString(key);
          String base = "bindings." + key;
          String w = plugin.getConfig().getString(base + ".world");
          double x = plugin.getConfig().getDouble(base + ".x");
          double y = plugin.getConfig().getDouble(base + ".y");
          double z = plugin.getConfig().getDouble(base + ".z");
          Location loc = new Location(Bukkit.getWorld(w), x, y, z);
          playerBound.put(pid, loc);
        } catch (Exception ignored) {}
      }
    }
  }

  public Optional<Graveyard> getGraveyardAt(Location center) {
    return graves.values().stream().filter(g -> sameBlock(g.getCenter(), center)).findFirst();
  }

  public Graveyard getGraveByOwner(UUID owner) { return graves.get(owner); }

  public void createGrave(UUID owner, Location center) {
    graves.put(owner, new Graveyard(center, 1));
    saveAll();
    GraveyardBuilder.buildLevel(center, 1);
    writeSign(center, owner, 1);
  }

  public boolean upgradeGrave(UUID owner, Player p) {
    Graveyard g = graves.get(owner);
    if (g == null) return false;
    if (g.getLevel() >= 5) return false;
    int next = Math.min(5, g.getLevel() + 1);
    int cost = costForLevel(next);
    int have = consumeSoulTokens(p, cost);
    if (have < cost) return false;
    g.setLevel(next);
    GraveyardBuilder.buildLevel(g.getCenter(), g.getLevel());
    writeSign(g.getCenter(), owner, g.getLevel());
    saveAll();
    return true;
  }

  public void bindPlayerTo(UUID player, Location graveCenter) {
    playerBound.put(player, graveCenter);
    saveAll();
    Optional<Graveyard> maybe = getGraveyardAt(graveCenter);
    if (maybe.isPresent()) {
      writeSign(maybe.get().getCenter(), player, maybe.get().getLevel());
    }
  }

  public void unbindPlayer(UUID player) {
    playerBound.remove(player);
    saveAll();
  }

  public Location getBoundLocation(UUID player) {
    return playerBound.get(player);
  }

  public int costForLevel(int level) {
    switch (level) {
      case 2: return 5;
      case 3: return 10;
      case 4: return 20;
      case 5: return 40;
      default: return 0;
    }
  }

  private boolean isSoulToken(ItemStack is) {
    if (is == null) return false;
    if (is.getType() != Material.BEDROCK) return false;
    if (!is.hasItemMeta()) return false;
    ItemMeta im = is.getItemMeta();
    return im.hasDisplayName() && "Soul Token".equals(im.getDisplayName());
  }

  private int consumeSoulTokens(Player p, int amount) {
    int needed = amount;
    for (int i = 0; i < p.getInventory().getSize(); i++) {
      ItemStack s = p.getInventory().getItem(i);
      if (s != null && isSoulToken(s)) {
        int take = Math.min(needed, s.getAmount());
        s.setAmount(s.getAmount() - take);
        if (s.getAmount() <= 0) p.getInventory().setItem(i, null);
        else p.getInventory().setItem(i, s);
        needed -= take;
        if (needed <= 0) break;
      }
    }
    p.updateInventory();
    return amount - needed;
  }

  private boolean sameBlock(Location a, Location b) {
    if (a == null || b == null) return false;
    return a.getWorld().equals(b.getWorld()) && a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
  }

  public java.util.List<Location> listAllGraveCenters() {
    return graves.values().stream()
      .map((com.shadowlord.graveyard.model.Graveyard g) -> g.getCenter())
      .collect(Collectors.toList());
  }

  // ------------- handlePostDeath: respawn or spectator until night ------------
  public void handlePostDeath(Player p) {
    Location bound = getBoundLocation(p.getUniqueId());
    long time = p.getWorld().getTime();
    boolean isNight = time >= 13000 && time <= 23000;
    if (bound != null && isNight) {
      Bukkit.getScheduler().runTask(plugin, () -> {
        Graveyard ownerGrave = findGraveForCenter(bound);
        applyRespawnEffects(p, ownerGrave != null ? ownerGrave.getLevel() : 1);
        p.teleport(bound.clone().add(0,1,0));
        p.setGameMode(GameMode.SURVIVAL);
        p.sendMessage("You have returned to your bound graveyard.");
      });
    } else {
      Bukkit.getScheduler().runTask(plugin, () -> {
        p.setGameMode(GameMode.SPECTATOR);
        p.sendMessage("You are in spectator until nightfall or until your bound graveyard can receive you.");
      });
      new org.bukkit.scheduler.BukkitRunnable() {
        @Override
        public void run() {
          if (!p.isOnline()) { this.cancel(); return; }
          long t = p.getWorld().getTime();
          boolean nightNow = t >= 13000 && t <= 23000;
          if (nightNow) {
            Location dest = getBoundLocation(p.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
              if (dest != null) {
                Graveyard ownerGrave = findGraveForCenter(dest);
                applyRespawnEffects(p, ownerGrave != null ? ownerGrave.getLevel() : 1);
                p.teleport(dest.clone().add(0,1,0));
              }
              p.setGameMode(GameMode.SURVIVAL);
              p.sendMessage("Night has fallen. Returning you to your graveyard.");
            });
            this.cancel();
          }
        }
      }.runTaskTimer(plugin, 20L, 20L);
    }
  }

  private Graveyard findGraveForCenter(Location center) {
    for (Graveyard g : graves.values()) {
      if (sameBlock(g.getCenter(), center)) return g;
    }
    return null;
  }

  private void applyRespawnEffects(Player p, int level) {
    p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
    if (level >= 1) p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 30, 0));
    if (level >= 2) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0));
      p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 30, 1));
    }
    if (level >= 3) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, 0));
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1));
    }
    if (level >= 4) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0));
      p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 0));
    }
    if (level >= 5) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 30, 1));
    }
  }

  // Write sign with owner name and level
  private void writeSign(Location center, UUID ownerOrPlayer, int level) {
    try {
      org.bukkit.block.Block signBlock = center.getWorld().getBlockAt(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ());
      if (!(signBlock.getState() instanceof Sign)) return;
      Sign sign = (Sign) signBlock.getState();
      String ownerName = Bukkit.getOfflinePlayer(ownerOrPlayer).getName();
      sign.setLine(0, "[Graveyard]");
      sign.setLine(1, ownerName == null ? "" : ownerName);
      sign.setLine(2, "Level " + level);
      sign.setLine(3, "Bound");
      sign.update();
    } catch (Throwable ignored) {}
  }

  // Attempt to perform sign-upgrade when a player right-clicks; returns true when succeeded
  public boolean attemptUpgradeFromSign(Player actor, Location signCenter) {
    Optional<Graveyard> gopt = getGraveyardAt(signCenter);
    if (!gopt.isPresent()) return false;
    Graveyard g = gopt.get();
    UUID ownerUuid = null;
    for (Map.Entry<UUID, Graveyard> e : graves.entrySet()) {
      if (sameBlock(e.getValue().getCenter(), g.getCenter())) { ownerUuid = e.getKey(); break; }
    }
    if (ownerUuid == null) return false;
    if (!actor.getUniqueId().equals(ownerUuid) && !actor.hasPermission("graveyard.admin")) return false;
    boolean ok = upgradeGrave(ownerUuid, actor);
    if (ok) actor.sendMessage("Graveyard upgraded via sign.");
    else actor.sendMessage("Upgrade failed: not enough Soul Tokens or already at max.");
    return ok;
  }
}
