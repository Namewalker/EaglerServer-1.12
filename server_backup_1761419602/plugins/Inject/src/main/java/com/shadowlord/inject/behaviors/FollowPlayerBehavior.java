package com.shadowlord.inject.behaviors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Random;

/**
 * Per-tick behavior: find nearest player and set velocity toward them.
 * Lightweight, safe for 1.12. Only affects LivingEntity movement vector.
 */
public class FollowPlayerBehavior implements Behavior {
  private final org.bukkit.plugin.Plugin plugin;
  private final Random rnd = new Random();

  public FollowPlayerBehavior(org.bukkit.plugin.Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void onAttach(Entity entity) {
    // nothing
  }

  @Override
  public void tick(Entity entity) {
    if (!(entity instanceof LivingEntity)) return;
    LivingEntity le = (LivingEntity) entity;
    Player nearest = null;
    double best = Double.MAX_VALUE;
    for (Player p : Bukkit.getOnlinePlayers()) {
      double d = p.getLocation().distanceSquared(le.getLocation());
      if (d < best) { best = d; nearest = p; }
    }
    if (nearest == null) return;
    Location to = nearest.getLocation();
    Vector dir = to.toVector().subtract(le.getLocation().toVector()).normalize();
    // small smoothing and speed cap
    double speed = 0.2 + rnd.nextDouble() * 0.15;
    le.setVelocity(dir.multiply(speed));
  }

  @Override
  public void onRemove(Entity entity) {
    // nothing
  }
}
