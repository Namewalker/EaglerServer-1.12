package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MirrorListener implements Listener {
  private final CursedManager cursed;
  private final Map<UUID, Long> lastUse = new HashMap<>();
  private static final long COOLDOWN_SECONDS = 60L;
  private static final String MIRROR_NAME = "Curse Mirror";

  public MirrorListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onUse(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack item = event.getItem();

    if (item == null) return;

    // Avoid direct Material enum constants that may differ between API versions
    String typeName = item.getType().name(); // safe regardless of enum set
    if (!typeName.equals("STAINED_GLASS_PANE") && !typeName.equals("GLASS_PANE")) return;

    ItemMeta meta = item.getItemMeta();
    if (meta == null || !meta.hasDisplayName()) return;
    if (!MIRROR_NAME.equals(meta.getDisplayName())) return;

    UUID uuid = player.getUniqueId();
    long now = System.currentTimeMillis() / 1000L;
    Long last = lastUse.get(uuid);
    if (last != null && (now - last) < COOLDOWN_SECONDS) {
      long left = COOLDOWN_SECONDS - (now - last);
      player.sendMessage("§cThe Curse Mirror is recharging. " + left + "s remaining.");
      return;
    }

    lastUse.put(uuid, now);

    int seconds = 10;
    int ticks = seconds * 20;

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (!online.isOnline()) continue;
      if (cursed.isCursed(online.getUniqueId())) {
        online.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0));
      }
    }

    player.sendMessage("§aThe Curse Mirror reveals cursed players for " + seconds + " seconds.");
  }
}
