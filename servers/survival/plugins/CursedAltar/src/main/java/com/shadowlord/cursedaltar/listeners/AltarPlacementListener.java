package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class AltarPlacementListener implements Listener {
  private final CursedManager cursed;

  public AltarPlacementListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent event) {
    if (event.getBlockPlaced().getType() != Material.ENCHANTMENT_TABLE) return;

    ItemStack hand = event.getItemInHand();
    if (!hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) return;
    if (!hand.getItemMeta().getDisplayName().equals("Cursed Altar")) return;

    cursed.addAltar(event.getBlockPlaced().getLocation());
    Player player = event.getPlayer();
    player.sendMessage("§5Cursed Altar placed. Beware its power.");
  }
}
