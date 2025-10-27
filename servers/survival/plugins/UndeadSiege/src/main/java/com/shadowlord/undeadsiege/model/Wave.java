package com.shadowlord.undeadsiege.model;

import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

public class Wave {
  private final Map<EntityType, Integer> spawns = new HashMap<>();
  private int delaySeconds = 5;

  public Wave() {}

  public void add(EntityType type, int count) { spawns.put(type, count); }
  public Map<EntityType, Integer> getSpawns() { return spawns; }
  public int getDelaySeconds() { return delaySeconds; }
  public void setDelaySeconds(int s) { delaySeconds = s; }
}
