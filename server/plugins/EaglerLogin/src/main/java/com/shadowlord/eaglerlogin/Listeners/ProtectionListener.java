package com.shadowlord.eaglerlogin.listeners;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import java.util.List;

public class ProtectionListener implements Listener {
  private final LoginListener loginListener;
  private final EaglerLoginPlugin plugin;
  private final boolean blockChat;
  private final boolean blockMove;
  private final boolean blockInteract;
  private final boolean blockInventory;
  private final boolean blockItemUse;
  private final List<String> whitelist;
  private final boolean debug;

  public ProtectionListener(LoginListener loginListener, EaglerLoginPlugin plugin) {
    this.loginListener = loginListener;
    this.plugin = plugin;
    blockChat = plugin.getConfig().getBoolean("protection.block-chat", true);
    blockMove = plugin.getConfig().getBoolean("protection.block-movements", true);
    blockInteract = plugin.getConfig().getBoolean("protection.block-interact", true);
    blockInventory = plugin.getConfig().getBoolean("protection.block-inventory", true);
    blockItemUse = plugin.getConfig().getBoolean("protection.block-item-use", true);
    whitelist = plugin.getConfig().getStringList("protection.command-whitelist");
    debug = plugin.getConfig().getBoolean("protection.debug", false);
  }

  private boolean allowed(Player p, String attemptedCommand) {
    if (p.hasPermission("eaglerlogin.bypass")) return true;
    if (loginListener.isLoggedIn(p.getUniqueId())) return true;
    if (attemptedCommand == null) return false;

    // Normalize to base command (without leading slash and without args)
    String cmd = attemptedCommand.startsWith("/") ? attemptedCommand.substring(1) : attemptedCommand;
    cmd = cmd.split(" ")[0].toLowerCase();

    for (String w : whitelist) {
      if (w == null) continue;
      if (w.equalsIgnoreCase(cmd)) {
        if (debug) plugin.getLogger().info("Protection: allowed command '" + cmd + "' for " + p.getName());
        return true;
      }
    }
    if (debug) plugin.getLogger().info("Protection: blocked command '" + cmd + "' for " + p.getName());
    return false;
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent e) {
    if (!blockChat) return;
    Player p = e.getPlayer();
    if (!allowed(p, null)) {
      e.setCancelled(true);
      // Do not spam the player every tick; optional single message on first attempt
      p.sendMessage(MsgUtil.color("&cYou must /register or /login to chat."));
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent e) {
    if (!blockMove) return;
    Player p = e.getPlayer();
    if (!allowed(p, null)) {
      if (e.getFrom().distanceSquared(e.getTo()) > 0) {
        e.setTo(e.getFrom()); // prevent movement
      }
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (!blockInteract) return;
    Player p = e.getPlayer();
    if (!allowed(p, null)) e.setCancelled(true);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!blockInventory) return;
    if (!(e.getWhoClicked() instanceof Player)) return;
    Player p = (Player)e.getWhoClicked();
    if (!allowed(p, null)) e.setCancelled(true);
  }

  @EventHandler
  public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
    Player p = e.getPlayer();
    String msg = e.getMessage();
    if (allowed(p, msg)) return;
    e.setCancelled(true);
    p.sendMessage(MsgUtil.color("&cYou must /register or /login before using commands."));
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent e) {
    if (!blockInteract) return;
    Player p = e.getPlayer();
    if (!allowed(p, null)) e.setCancelled(true);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player p = (Player) e.getDamager();
      if (!allowed(p, null)) e.setCancelled(true);
    }
  }
}
