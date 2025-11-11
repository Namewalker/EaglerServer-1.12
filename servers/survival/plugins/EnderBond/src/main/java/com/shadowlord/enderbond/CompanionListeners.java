package shadowlord.enderbond;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CompanionListeners implements Listener {

    private final EnderBond plugin;
    private final Map<UUID, Long> lastProtect = new HashMap<>(); // owner -> cooldown timestamp

    public CompanionListeners(EnderBond plugin) {
        this.plugin = plugin;

        // retreat/return scheduled check (runs every 10s)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, UUID> entry : plugin.getCompanionMap().entrySet()) {
                    UUID ownerId = entry.getKey();
                    UUID compId = entry.getValue();
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner == null) continue;

                    // find companion entity by iterating all worlds/entities (1.12 safe)
                    Enderman comp = null;
                    World compWorld = null;
                    for (World w : Bukkit.getWorlds()) {
                        for (Entity ent : w.getEntities()) {
                            if (ent.getUniqueId().equals(compId) && ent instanceof Enderman) {
                                comp = (Enderman) ent;
                                compWorld = w;
                                break;
                            }
                        }
                        if (comp != null) break;
                    }
                    // if companion not present in any world, skip
                    boolean inRefuge = plugin.getVanished().contains(compId);

                    // choose the owner's world time/storm to decide retreat/return
                    World ownerWorld = owner.getWorld();
                    boolean storm = ownerWorld.hasStorm();
                    long time = ownerWorld.getTime();
                    boolean night = (time >= 13000 || time <= 2300);

                    if (( !night || storm ) && comp != null && !inRefuge) {
                        // retreat companion to refuge world
                        World refuge = plugin.getRefugeWorld();
                        if (refuge == null) continue;
                        Location refLoc = new Location(refuge, 0.5, 65, 0.5 + plugin.getVanished().size() * 3);
                        comp.teleport(refLoc);
                        plugin.getVanished().add(comp.getUniqueId());
                        owner.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Your Ender Companion vanishes into the shadows."));
                    } else if (night && !storm && inRefuge) {
                        // return companion from refuge to owner
                        // find the companion entity in the refuge world (iterate)
                        Enderman refComp = null;
                        World refuge = plugin.getRefugeWorld();
                        if (refuge == null) continue;
                        for (Entity ent : refuge.getEntities()) {
                            if (ent.getUniqueId().equals(compId) && ent instanceof Enderman) { refComp = (Enderman) ent; break; }
                        }
                        if (refComp == null) continue;
                        Location near = owner.getLocation().clone().add(1,0,1);
                        refComp.teleport(near);
                        plugin.getVanished().remove(compId);
                        owner.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dYour Ender Companion returns from the void."));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L * 10);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent ev) {
        // detect when owner is attacked -> protective teleporting
        Entity victim = ev.getEntity();
        if (!(victim instanceof Player)) return;
        Player owner = (Player) victim;
        UUID ownerId = owner.getUniqueId();

        UUID compId = plugin.getCompanionMap().get(ownerId);
        if (compId == null) return;

        // cooldown
        long now = System.currentTimeMillis();
        long cd = plugin.getConfig().getLong("protect.cooldown_ms", 15_000);
        if (lastProtect.getOrDefault(ownerId, 0L) + cd > now) return;

        // find companion entity by iterating worlds/entities (1.12 compatibility)
        Enderman comp = null;
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getUniqueId().equals(compId) && e instanceof Enderman) {
                    comp = (Enderman) e;
                    break;
                }
            }
            if (comp != null) break;
        }
        if (comp == null) return;

        // swap: teleport companion to attacker and owner to safe spot
        Entity attacker = ev.getDamager();
        Location attackerLoc = attacker.getLocation();
        Location ownerSafe = owner.getLocation().clone().add(-2,0,-2);
        ownerSafe.setY(Math.max(ownerSafe.getY(), owner.getWorld().getHighestBlockYAt(ownerSafe) + 0.5));

        comp.teleport(attackerLoc.add(0,0.5,0));
        // attempt to set target if attacker is a living entity
        if (attacker instanceof org.bukkit.entity.LivingEntity) {
            comp.setTarget((org.bukkit.entity.LivingEntity) attacker);
        }
        owner.teleport(ownerSafe);

        lastProtect.put(ownerId, now);

        owner.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5Your Ender Companion shields you and engages your attacker!"));
    }

    @EventHandler
    public void onCompanionTarget(EntityTargetEvent ev) {
        // keep companion targeting behavior reasonable for bonded companions
        Entity e = ev.getEntity();
        if (!(e instanceof Enderman)) return;
        Enderman end = (Enderman) e;
        UUID eid = end.getUniqueId();
        UUID owner = findOwnerByCompanion(eid);
        if (owner == null) return;
        // Avoid the companion targeting its owner; cancel if target is owner
        if (ev.getTarget() instanceof Player) {
            Player t = (Player) ev.getTarget();
            if (t.getUniqueId().equals(owner)) {
                ev.setCancelled(true);
            }
        }
    }

    private UUID findOwnerByCompanion(UUID comp) {
        for (Map.Entry<UUID, UUID> e : plugin.getCompanionMap().entrySet()) {
            if (e.getValue() != null && e.getValue().equals(comp)) return e.getKey();
        }
        return null;
    }
}
