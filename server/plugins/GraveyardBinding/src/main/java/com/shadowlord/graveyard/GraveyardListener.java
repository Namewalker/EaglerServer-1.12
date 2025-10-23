package com.shadowlord.graveyard;

import com.shadowlord.graveyard.model.Graveyard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.Optional;

public class GraveyardListener implements Listener {
  private final GraveyardManager manager;

  public GraveyardListener(GraveyardManager manager) { this.manager = manager; }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent e) {
    Player p = e.getEntity();
    manager.handlePostDeath(p);
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent e) {
    Player p = e.getPlayer();
    manager.handlePostDeath(p);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    ItemStack item = e.getItemInHand();
    if (item == null) return;
    if (item.getType() != Material.matchMaterial("MOSSY_COBBLESTONE")) return;
    if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
    if (!"Graveyard Core".equals(item.getItemMeta().getDisplayName())) return;

    Block placed = e.getBlockPlaced();
    Player p = e.getPlayer();
    Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
      manager.createGrave(p.getUniqueId(), placed.getLocation());
      manager.bindPlayerTo(p.getUniqueId(), placed.getLocation());
      p.sendMessage("Graveyard created and bound to you.");
    }, 1L);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    Block b = e.getClickedBlock();
    if (b == null) return;
    BlockState state = b.getState();
    if (!(state instanceof Sign)) return;
    Sign sign = (Sign) state;
    String line0 = sign.getLine(0);
    if (line0 == null || !line0.equalsIgnoreCase("[Graveyard]")) return;
    Player p = e.getPlayer();
    Block below = b.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
    if (sign.getLine(1) == null || sign.getLine(1).trim().isEmpty()) {
      manager.bindPlayerTo(p.getUniqueId(), below.getLocation());
      p.sendMessage("You are now bound to this graveyard.");
      return;
    }
    manager.attemptUpgradeFromSign(p, below.getLocation());
  }
}
