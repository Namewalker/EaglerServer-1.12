package com.shadowlord.inject.listeners;

import com.shadowlord.inject.InjectPlugin;
import com.shadowlord.inject.behaviors.BehaviorRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;

/**
 * Listens for events that behaviors may rely on or to provide admin convenience.
 * Keep small; most behavior logic lives inside Behavior implementations.
 */
public class InjectListeners implements Listener {
  private final InjectPlugin plugin;
  private final BehaviorRegistry registry;

  public InjectListeners(InjectPlugin plugin, BehaviorRegistry registry) {
    this.plugin = plugin;
    this.registry = registry;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent e) {
    // example: behaviors could react by checking registry in tick loop; leave empty for now
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent e) {
    // let behaviors that registered death listeners run (handled by the behavior implementation)
  }
}
