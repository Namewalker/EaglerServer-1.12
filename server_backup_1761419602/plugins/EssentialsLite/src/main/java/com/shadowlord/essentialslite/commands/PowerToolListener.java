package com.shadowlord.essentialslite.commands;

import com.shadowlord.essentialslite.EssentialsLite;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class PowerToolListener implements Listener {
  private final EssentialsLite plugin;
  public PowerToolListener(EssentialsLite plugin) { this.plugin = plugin; }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Player p = e.getPlayer();
      FileConfiguration cfg = plugin.getStore().getConfig();
      String path = "powertools." + p.getUniqueId();
      if (cfg.isSet(path)) {
        String cmd = cfg.getString(path);
        p.performCommand(cmd);
        p.sendMessage(ChatColor.GRAY + "PowerTool executed: " + cmd);
        e.setCancelled(true);
      }
    }
  }
}
