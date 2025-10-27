package com.shadowlord.inject.behaviors;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

/**
 * Behavior: when the attached entity dies, create an explosion at its location.
 * Implementation: register a temporary listener when attached.
 */
public class ExplodeOnDeathBehavior implements Behavior {
  private Listener listener;

  @Override
  public void onAttach(Entity entity) {
    Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("Inject");
    listener = new Listener() {
      @org.bukkit.event.EventHandler
      public void onDeath(EntityDeathEvent e) {
        if (!e.getEntity().getUniqueId().equals(entity.getUniqueId())) return;
        Location loc = e.getEntity().getLocation();
        e.getEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.5f, false, false);
        // remove this listener after explosion
        HandlerList.unregisterAll(this);
      }
    };
    Bukkit.getPluginManager().registerEvents(listener, plugin);
  }

  @Override
  public void tick(Entity entity) {
    // no per-tick behavior
  }

  @Override
  public void onRemove(Entity entity) {
    if (listener != null) HandlerList.unregisterAll(listener);
  }
}
