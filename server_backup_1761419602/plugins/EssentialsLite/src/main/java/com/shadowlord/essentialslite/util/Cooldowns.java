package com.shadowlord.essentialslite.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class Cooldowns {
  private final Map<UUID, Long> map = new ConcurrentHashMap<>();
  public boolean tryUse(UUID id, long cooldownMillis) {
    Long last = map.get(id);
    long now = System.currentTimeMillis();
    if (last == null || now - last >= cooldownMillis) {
      map.put(id, now);
      return true;
    }
    return false;
  }
}
