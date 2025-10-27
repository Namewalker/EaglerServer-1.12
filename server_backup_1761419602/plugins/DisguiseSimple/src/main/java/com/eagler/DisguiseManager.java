package com.eagler;

import org.bukkit.Location;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Villager;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DisguiseManager {
    // Store leashed pairs: target -> leasher
    private Map<Player, Player> leashedPlayers = new HashMap<>();

    // Call this to start dragging after leashing
    public void startDragging(Player leasher, Player target) {
        leashedPlayers.put(target, leasher);
        // Start a repeating task to drag the target
        Bukkit.getScheduler().runTaskTimer(
            Bukkit.getPluginManager().getPlugin("DisguiseSimple"),
            () -> {
                if (!leashedPlayers.containsKey(target)) return;
                Player leashHolder = leashedPlayers.get(target);
                if (leashHolder == null || !leashHolder.isOnline() || !target.isOnline()) return;
                double maxDistance = 5.0;
                if (leashHolder.getWorld() != target.getWorld()) return;
                double distance = leashHolder.getLocation().distance(target.getLocation());
                if (distance > maxDistance) {
                    // Teleport target closer to leash holder
                    Location leashLoc = leashHolder.getLocation();
                    Location targetLoc = target.getLocation();
                    Location newLoc = targetLoc.clone().add(leashLoc.toVector().subtract(targetLoc.toVector()).normalize().multiply(distance - maxDistance + 0.5));
                    newLoc.setY(leashLoc.getY()); // Keep Y level
                    target.teleport(newLoc);
                }
            },
            10L, 10L // Run every 0.5 seconds
        );
    }

    // Call this to stop dragging
    public void stopDragging(Player target) {
        leashedPlayers.remove(target);
    }
    // Leash another player: just start dragging
    public void leashPlayer(Player leasher, Player target) {
        leasher.sendMessage("§a[Disguise] You have leashed " + target.getName() + "!");
        target.sendMessage("§c[Disguise] " + leasher.getName() + " has put you on a leash!");
        startDragging(leasher, target);

        // Attach the villager to the leasher with a lead
        LeashHitch hitch = (LeashHitch) target.getWorld().spawnEntity(targetLoc, EntityType.LEASH_HITCH);
        hitch.setLeashHolder(leasher);

        // Leash the villager to the hitch
        villager.setLeashHolder(hitch);

        leasher.sendMessage("§a[Disguise] You have leashed " + target.getName() + "!");
        target.sendMessage("§c[Disguise] " + leasher.getName() + " has put you on a lead!");
    }
    // Basic disguise using Bukkit API (not full visual, but works for name and some mob disguises)
    private Map<UUID, String> originalNames = new HashMap<>();
    private Map<UUID, EntityType> mobDisguises = new HashMap<>();
    private org.bukkit.plugin.Plugin plugin;

    public DisguiseManager(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    public void disguiseAsMob(Player player, EntityType mobType) {
    mobDisguises.put(player.getUniqueId(), mobType);
    player.sendMessage("§a[Disguise] You are now disguised as a " + mobType.name());
    // For basic effect, you can use PotionEffect to make player invisible and spawn a mob at their location
    player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
    org.bukkit.entity.Entity mob = player.getWorld().spawnEntity(player.getLocation(), mobType);
    mob.setCustomName(player.getName());
    mob.setCustomNameVisible(true);
    mob.setInvulnerable(true);
    mob.setSilent(true);
    mob.setMetadata("DisguiseMob", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
    }

    public void disguiseAsPlayer(Player player, String targetName) {
        originalNames.put(player.getUniqueId(), player.getDisplayName());
        player.setDisplayName(targetName);
        player.setPlayerListName(targetName);
        player.sendMessage("§a[Disguise] You are now disguised as player " + targetName);
    }

    public void undisguise(Player player) {
        // Remove mob disguise
        mobDisguises.remove(player.getUniqueId());
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
        // Remove spawned mobs with metadata
        for (org.bukkit.entity.Entity entity : player.getWorld().getEntities()) {
            if (entity.hasMetadata("DisguiseMob") && entity.getCustomName() != null && entity.getCustomName().equals(player.getName())) {
                entity.remove();
            }
        }
        // Restore player name
        if (originalNames.containsKey(player.getUniqueId())) {
            player.setDisplayName(originalNames.get(player.getUniqueId()));
            player.setPlayerListName(originalNames.get(player.getUniqueId()));
            originalNames.remove(player.getUniqueId());
        }
        player.sendMessage("§a[Disguise] Disguise removed.");
    }
}
