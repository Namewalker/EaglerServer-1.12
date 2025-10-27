package com.shadowlord.inject.behaviors;

import org.bukkit.entity.Entity;

public interface Behavior {
  void onAttach(Entity entity);
  void tick(Entity entity);
  void onRemove(Entity entity);
}
