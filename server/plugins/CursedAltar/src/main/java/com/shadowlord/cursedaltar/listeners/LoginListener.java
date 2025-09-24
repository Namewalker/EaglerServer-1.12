package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.UUID;

public class LoginListener implements Listener {
  private final CursedManager cursed;

  public LoginListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    int level = cursed.getLevel(uuid);
    if (level <= 0) return;

    switch (level) {
      case 1:
        player.sendMessage("§8A distant whisper: “You are claimed.”");
        break;
      case 2:
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
        player.sendMessage("§0Shadows swallow your sight.");
        break;
      case 3:
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
        player.sendMessage("§8Your flesh feels heavy.");
        break;
      default:
        Location loc = player.getLocation();
        player.teleport(loc);
        player.getWorld().getBlockAt(loc).setType(Material.OBSIDIAN);
        player.sendMessage("§4You are sealed by unbreakable stone.");
        break;
    }
  }
}
