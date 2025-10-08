package com.shadowlord.inject.behaviors;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Behavior lifecycle:
 *  - onAttach called when behavior is added to an entity
 *  - tick called every plugin tick (or schedule) while attached
 *  - onRemove called when detached or plugin disabled
 */
public interface Behavior {
  void onAttach(Entity entity);
  void tick(Entity entity);
  void onRemove(Entity entity);
}
