package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

public class LoginListener implements Listener {
  private final CursedManager cursed;
  private final Random random = new Random();

  public LoginListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    int level = cursed.getLevel(uuid);
    if (level <= 0) return;

    // Declare these up front so all cases can use them
    World world = player.getWorld();
    Location loc = player.getLocation();

    switch (level) {
      case 1:
        player.sendMessage("§8A distant whisper: “You are claimed.”");
        break;

      case 2:
        player.addPotionEffect(
          new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1)
        );
        player.sendMessage("§0Shadows swallow your sight.");
        break;

      case 3:
        player.addPotionEffect(
          new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2)
        );
        player.sendMessage("§8Your flesh feels heavy.");
        break;

      case 4:
        player.addPotionEffect(
          new PotionEffect(PotionEffectType.CONFUSION, Integer.MAX_VALUE, 1)
        );
        player.sendMessage("§5Your mind reels with disorientation.");
        break;

      case 5:
        player.addPotionEffect(
          new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 1)
        );
        player.sendMessage("§0Dark veins pulse beneath your skin.");
        break;

      case 6:
        player.addPotionEffect(
          new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1)
        );
        player.sendMessage("§2Venom courses through your veins.");
        break;

      case 7:
        // Random teleport within 100 blocks of world spawn
        Location spawn = world.getSpawnLocation();
        double dx = (random.nextDouble() - 0.5) * 200;
        double dz = (random.nextDouble() - 0.5) * 200;
        Location randomLoc = spawn.clone().add(dx, 0, dz);
        randomLoc.setY(world.getHighestBlockYAt(randomLoc));
        player.teleport(randomLoc);
        player.sendMessage("§dReality shivers and repositions you.");
        break;

      default:
        // 8+ = full obsidian cage + wolves
        for (int dx2 = -1; dx2 <= 1; dx2++) {
          for (int dy2 = -1; dy2 <= 1; dy2++) {
            for (int dz2 = -1; dz2 <= 1; dz2++) {
              if (dx2 == 0 && dy2 == 0 && dz2 == 0) continue;
              Location blockLoc = loc.clone().add(dx2, dy2, dz2);
              world.getBlockAt(blockLoc).setType(Material.OBSIDIAN);
            }
          }
        }
        // Spawn two spectral wolves at the player's position
        for (int i = 0; i < 2; i++) {
          world.spawnEntity(loc, EntityType.WOLF);
        }
        player.sendMessage("§4You are sealed by unbreakable stone and haunted by wolves.");
        break;
    }
  }
}
