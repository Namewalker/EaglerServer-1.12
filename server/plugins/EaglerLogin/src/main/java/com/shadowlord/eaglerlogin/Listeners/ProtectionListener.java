package com.shadowlord.eaglerlogin.listeners;

import com.shadowlord.eaglerlogin.EaglerLoginPlugin;
import com.shadowlord.eaglerlogin.util.MsgUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectionListener implements Listener {
  private final LoginListener loginListener;
  private final EaglerLoginPlugin plugin;
  private final boolean blockChat;
  private final boolean blockMove;
  private final boolean blockInteract;
  private final boolean blockInventory;
  private final List<String> whitelist;
  private final boolean debug;

  // Track last known safe location for locked players
  private final Map<UUID, Location> lastSafe = new ConcurrentHashMap<>();

  public ProtectionListener(LoginListener loginListener, EaglerLoginPlugin plugin) {
    this.loginListener = loginListener;
    this.plugin = plugin;
    blockChat = plugin.getConfig().getBoolean("protection.block-chat", true);
    blockMove = plugin.getConfig().getBoolean("protection.block-movements", true);
    blockInteract = plugin.getConfig().getBoolean("protection.block-interact", true);
    blockInventory = plugin.getConfig().getBoolean("protection.block-inventory", true);
    whitelist = plugin.getConfig().getStringList("protection.command-whitelist");
    debug = plugin.getConfig().getBoolean("protection.debug", false);
  }

  private boolean allowed(Player p, String attemptedCommand) {
    if (p.hasPermission("eaglerlogin.bypass")) return true;
    if (loginListener.isLoggedIn(p.getUniqueId())) return true;
    if (attemptedCommand == null) return false;
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
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    // Store initial safe location on join (used while locked)
    lastSafe.put(p.getUniqueId(), p.getLocation());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    lastSafe.remove(e.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    if (!blockChat) return;
    if (!allowed(p, null)) {
      e.setCancelled(true);
      p.sendMessage(MsgUtil.color("&cYou must /register or /login to chat."));
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent e) {
    if (!blockMove) return;
    Player p = e.getPlayer();
    if (allowed(p, null)) {
      // update last safe location when player is authenticated
      lastSafe.put(p.getUniqueId(), p.getLocation());
      return;
    }
    // prevent movement by teleporting back to last safe location and zeroing velocity
    Location safe = lastSafe.get(p.getUniqueId());
    if (safe == null) safe = p.getLocation();
    if (e.getFrom().distanceSquared(e.getTo()) > 0) {
      // Teleport back synchronously on main thread (this handler runs on main thread)
      e.setTo(e.getFrom());
      p.setVelocity(new Vector(0, 0, 0));
      p.setFallDistance(0f);
    }
    // ensure the player remains at safe location if some movement slip occurs
    if (safe.getWorld().equals(p.getWorld()) && p.getLocation().distanceSquared(safe) > 0.01) {
      p.teleport(safe);
      p.setVelocity(new Vector(0, 0, 0));
      p.setFallDistance(0f);
    }
  }

  @EventHandler
  public void onToggleFlight(PlayerToggleFlightEvent e) {
    Player p = e.getPlayer();
    if (!blockMove) return;
    if (!allowed(p, null)) e.setCancelled(true);
  }

  @EventHandler
  public void onTeleport(PlayerTeleportEvent e) {
    Player p = e.getPlayer();
    if (!blockMove) return;
    if (!allowed(p, null)) {
      // cancel teleport attempts while locked
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    if (!blockInteract) return;
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
    Player p = e.getPlayer();
    if (!blockInteract) return;
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
