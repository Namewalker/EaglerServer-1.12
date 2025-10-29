package com.shadowlord.tickfreeze;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class FreezeUtil {
  private FreezeUtil() {}

  public static void setNoAIForAllLoadedEntities(boolean noAI, Plugin plugin) {
    for (org.bukkit.World w : Bukkit.getWorlds()) {
      for (Entity e : w.getEntities()) {
        if (e instanceof LivingEntity) {
          try {
            setNoAI((LivingEntity) e, noAI);
          } catch (Throwable t) {
            plugin.getLogger().warning("Failed toggling AI for " + e.getType() + ": " + t.getMessage());
          }
        }
      }
    }
  }

  private static void setNoAI(LivingEntity le, boolean noAI) throws Exception {
    // Try to call setAI(boolean) if present on the implementation
    try {
      Method m = le.getClass().getMethod("setAI", boolean.class);
      m.invoke(le, !noAI);
      return;
    } catch (NoSuchMethodException ignored) {}

    // Fallback: call getHandle() and setNoAI(boolean) on NMS entity if available
    try {
      Method getHandle = le.getClass().getMethod("getHandle");
      Object nmsEntity = getHandle.invoke(le);
      Method setNoAIMethod = nmsEntity.getClass().getMethod("setNoAI", boolean.class);
      setNoAIMethod.invoke(nmsEntity, noAI);
    } catch (NoSuchMethodException ignored) {}
  }
}
