package com.shadowlord.tickfreeze.listeners;

import com.shadowlord.tickfreeze.TickFreezePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.UUID;

/**
 * Listens for many events and cancels or modifies them while plugin is in frozen mode.
 */
public class FreezeListener implements Listener {
  private final TickFreezePlugin plugin;

  public FreezeListener(TickFreezePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    if (!plugin.isFrozen()) return;
    Player p = e.getPlayer();
    if (!plugin.shouldFreezePlayer(p.getUniqueId())) return;
    if (e.getFrom().distanceSquared(e.getTo()) > 0) {
      e.setTo(e.getFrom());
      p.setVelocity(new org.bukkit.util.Vector(0,0,0));
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (!plugin.isFrozen()) return;
    if (!plugin.shouldFreezePlayer(e.getPlayer().getUniqueId())) return;
    e.setCancelled(true);
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent e) {
    if (!plugin.isFrozen()) return;
    if (!plugin.shouldFreezePlayer(e.getPlayer().getUniqueId())) return;
    e.setCancelled(true);
  }

  @EventHandler
  public void onPickup(EntityPickupItemEvent e) {
    if (!plugin.isFrozen()) return;
    if (e.getEntity() instanceof Player) {
      Player p = (Player) e.getEntity();
      if (plugin.shouldFreezePlayer(p.getUniqueId())) e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    if (plugin.isFrozen() && plugin.shouldFreezePlayer(e.getPlayer().getUniqueId())) e.setCancelled(true);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (plugin.isFrozen() && plugin.shouldFreezePlayer(e.getPlayer().getUniqueId())) e.setCancelled(true);
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onFluidFlow(BlockFromToEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onEntityTarget(EntityTargetEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onPistonExtend(BlockPistonExtendEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onPistonRetract(BlockPistonRetractEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (plugin.isFrozen()) {
      if (e.getWhoClicked() instanceof Player) {
        Player p = (Player) e.getWhoClicked();
        if (plugin.shouldFreezePlayer(p.getUniqueId())) e.setCancelled(true);
      } else {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent e) {
    if (plugin.isFrozen()) {
      if (e.getPlayer() instanceof Player) {
        Player p = (Player) e.getPlayer();
        if (plugin.shouldFreezePlayer(p.getUniqueId())) e.setCancelled(true);
      } else {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onWeatherChange(WeatherChangeEvent e) {
    if (plugin.isFrozen()) e.setCancelled(true);
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    if (!plugin.isFrozen()) return;
    Player p = e.getPlayer();
    if (!plugin.shouldFreezePlayer(p.getUniqueId())) return;
    String msg = e.getMessage().toLowerCase();
    if (msg.startsWith("/tp ") || msg.startsWith("/tphere") || msg.startsWith("/setblock") ||
        msg.startsWith("/fill") || msg.startsWith("/gamemode") || msg.startsWith("/time") ||
        msg.startsWith("/weather") || msg.startsWith("/spawnpoint") || msg.startsWith("/summon")) {
      e.setCancelled(true);
      p.sendMessage("§cServer is frozen.");
    }
  }
}
