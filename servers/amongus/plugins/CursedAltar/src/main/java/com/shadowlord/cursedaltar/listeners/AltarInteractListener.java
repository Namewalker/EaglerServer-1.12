package com.shadowlord.cursedaltar.listeners;

import com.shadowlord.cursedaltar.CursedManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;

public class AltarInteractListener implements Listener {
  private final CursedManager cursed;

  public AltarInteractListener(CursedManager cursed) {
    this.cursed = cursed;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getClickedBlock() == null) return;
    if (event.getClickedBlock().getType() != Material.ENCHANTMENT_TABLE) return;

    Location loc = event.getClickedBlock().getLocation();
    if (!cursed.isAltar(loc)) return;

    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    if (!cursed.isCursed(uuid)) {
      cursed.registerCurse(uuid);
      player.sendMessage("§5You have been marked by the Cursed Altar. Your soul is bound.");
    } else {
      player.sendMessage("§8Your bond to the altar deepens...");
    }
  }
}
