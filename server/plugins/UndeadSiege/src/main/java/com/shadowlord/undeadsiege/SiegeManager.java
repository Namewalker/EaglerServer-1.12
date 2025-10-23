package com.shadowlord.undeadsiege;

import com.shadowlord.undeadsiege.model.SiegeRegion;
import com.shadowlord.undeadsiege.model.Wave;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SiegeManager implements Listener {
  private final UndeadSiegePlugin plugin;
  private final RegionManager regionManager;
  private final MobSpawner spawner;
  private final PlayerTracker tracker;

  private boolean active = false;
  private SiegeRegion activeRegion;
  private final List<Wave> waves = new ArrayList<>();
  private BukkitRunnable timeLockTask;
  private int currentWave = 0;

  public SiegeManager(UndeadSiegePlugin plugin) {
    this.plugin = plugin;
    this.regionManager = new RegionManager(plugin);
    this.spawner = new MobSpawner(plugin);
    this.tracker = new PlayerTracker();
    loadWaves();
  }

  private void loadWaves() {
    Wave w1 = new Wave(); w1.add(EntityType.ZOMBIE, 15); w1.add(EntityType.SKELETON, 10); w1.setDelaySeconds(5);
    Wave w2 = new Wave(); w2.add(EntityType.ZOMBIE, 20); w2.add(EntityType.SKELETON, 15); w2.add(EntityType.WITCH, 2); w2.setDelaySeconds(7);
    Wave w3 = new Wave(); w3.add(EntityType.ZOMBIE, 25); w3.add(EntityType.SKELETON, 20); w3.add(EntityType.SPIDER, 5); w3.setDelaySeconds(10);
    Wave finalWave = new Wave(); finalWave.add(EntityType.ZOMBIE, 1); finalWave.add(EntityType.SKELETON, 6); finalWave.setDelaySeconds(12);
    waves.clear();
    waves.add(w1); waves.add(w2); waves.add(w3); waves.add(finalWave);
  }

  public void setRegion(SiegeRegion r) { regionManager.saveRegionToConfig(r); }

  public SiegeRegion getRegion() { return regionManager.getRegion(); }

  public boolean isActive() { return active; }

  public void startSiege(boolean manual) {
    if (active) return;
    SiegeRegion r = regionManager.getRegion();
    if (r == null) { plugin.getLogger().warning("Attempted to start siege but no region is configured."); return; }

    active = true;
    activeRegion = r;
    currentWave = 0;

    World siegeWorld = Bukkit.getWorld(r.getWorldName());
    if (siegeWorld != null) siegeWorld.setTime(18000L);

    timeLockTask = new BukkitRunnable() {
      @Override
      public void run() {
        World w = Bukkit.getWorld(r.getWorldName());
        if (w != null) w.setTime(18000L);
      }
    };
    timeLockTask.runTaskTimer(plugin, 0L, 20L);

    regionManager.buildBorder(r);

    Location center = r.getCenter().clone().add(0, 1, 0);
    for (Player p : Bukkit.getOnlinePlayers()) {
      tracker.saveAndTeleport(p, center);
      p.sendMessage(ChatColor.DARK_PURPLE + "[Siege] " + ChatColor.GOLD + "The siege has begun! Defend the region.");
      p.sendTitle(ChatColor.RED + "Siege", ChatColor.YELLOW + "Defend the region!", 10, 40, 10);
    }

    startNextWaveIfNoActiveMobs();
  }

  private void startNextWaveIfNoActiveMobs() {
    if (!active) return;
    // wait until there are no living siege mobs in the world
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!active) { this.cancel(); return; }
        boolean any = false;
        for (World w : Bukkit.getWorlds()) {
          for (Entity e : w.getEntities()) {
            if (e.hasMetadata("siege_mob")) { any = true; break; }
          }
          if (any) break;
        }
        if (!any) {
          // start the next wave
          if (currentWave >= waves.size()) { // all waves done
            endSiege(true);
            this.cancel();
            return;
          }
          Wave wave = waves.get(currentWave);
          int multiplier = 2;
          int waveNumber = currentWave + 1;
          for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.RED + "[Siege] Wave " + waveNumber + " incoming!");
            p.sendTitle(ChatColor.RED + "Wave " + waveNumber, ChatColor.GRAY + "Prepare!", 5, 30, 5);
          }
          // spawn wave once
          spawner.spawnWave(activeRegion.getCenter(), wave, multiplier);
          currentWave++;
        }
      }
    }.runTaskTimer(plugin, 0L, 20L); // check every second
  }

  public void endSiege(boolean victory) {
    if (!active) return;
    active = false;
    if (timeLockTask != null) { timeLockTask.cancel(); timeLockTask = null; }
    regionManager.removeBorder(activeRegion);
    for (Player p : Bukkit.getOnlinePlayers()) {
      tracker.restore(p);
      p.sendMessage(ChatColor.GREEN + "[Siege] The siege has ended. Returning you to your previous location.");
      p.sendTitle(ChatColor.GREEN + "Siege Over", ChatColor.YELLOW + "You are returned.", 10, 40, 10);
    }
    if (victory) Bukkit.broadcastMessage(ChatColor.GOLD + "[Siege] The defenders prevailed! Rewards will be distributed.");
    else Bukkit.broadcastMessage(ChatColor.GRAY + "[Siege] The siege was halted.");
    activeRegion = null;
    currentWave = 0;
  }

  public void stopSiege(boolean force) { if (!active) return; endSiege(!force); }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!active || activeRegion == null) return;
    Player p = event.getEntity();
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!active || activeRegion == null) return;
      if (!p.getWorld().getName().equals(activeRegion.getWorldName())) return;
      Location c = activeRegion.getCenter();
      int half = activeRegion.getHalfSize();
      if (Math.abs(p.getLocation().getBlockX() - c.getBlockX()) <= half
          && Math.abs(p.getLocation().getBlockZ() - c.getBlockZ()) <= half) {
        tracker.setSpectator(p);
        p.sendMessage(ChatColor.GRAY + "[Siege] You are now a spectator until the siege ends.");
      }
    }, 20L);
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (!active) return;
    org.bukkit.entity.Entity ent = event.getEntity();
    if (!ent.hasMetadata("siege_mob")) return;
    Random rnd = new Random();
    if (rnd.nextBoolean()) {
      org.bukkit.inventory.ItemStack token = new org.bukkit.inventory.ItemStack(Material.BEDROCK, 1);
      org.bukkit.inventory.meta.ItemMeta im = token.getItemMeta();
      im.setDisplayName("Soul Token");
      im.setLore(Arrays.asList("Use this to upgrade graveyards.", "Right-click a graveyard sign to spend."));
      token.setItemMeta(im);
      event.getDrops().add(token);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (!active || activeRegion == null) return;
    Location loc = e.getBlock().getLocation();
    if (!loc.getWorld().getName().equals(activeRegion.getWorldName())) return;
    int dx = loc.getBlockX() - activeRegion.getX();
    int dz = loc.getBlockZ() - activeRegion.getZ();
    int half = activeRegion.getHalfSize();
    if (Math.abs(dx) == half || Math.abs(dz) == half) {
      e.setCancelled(true);
      e.getPlayer().sendMessage(ChatColor.RED + "You cannot break the siege boundary during an active siege.");
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) { }
}
