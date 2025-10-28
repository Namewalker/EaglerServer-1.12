package com.eagler.armorprank;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmorPrankPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("armorprank").setExecutor((sender, cmd, label, args) -> {
            if (!sender.hasPermission("armorprank.use") && !sender.isOp()) {
                sender.sendMessage("You don't have permission to use this.");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage("Usage: /armorprank <player>");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Command must be used by a player.");
                return true;
            }
            Player owner = (Player) sender;
            long nextAllowed = cooldowns.getOrDefault(owner.getUniqueId(), 0L);
            if (System.currentTimeMillis() < nextAllowed) {
                long secs = (nextAllowed - System.currentTimeMillis())/1000;
                owner.sendMessage("You are on cooldown for " + secs + "s");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                owner.sendMessage("Player not online: " + args[0]);
                return true;
            }
            if (target.equals(owner)) {
                owner.sendMessage("You cannot prank yourself.");
                return true;
            }
            if (target.getGameMode() != GameMode.SURVIVAL) {
                owner.sendMessage("Target must be in survival mode.");
                return true;
            }
            if (target.hasPermission("armorprank.exempt")) {
                owner.sendMessage("Target is exempt from pranks.");
                return true;
            }

            // find nearest survival player that is not owner and not target
            Player nearest = null;
            double best = Double.MAX_VALUE;
            Location tloc = target.getLocation();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(owner) || p.equals(target)) continue;
                if (p.getGameMode() != GameMode.SURVIVAL) continue;
                double d = p.getLocation().distanceSquared(tloc);
                if (d < best) {
                    best = d;
                    nearest = p;
                }
            }
            if (nearest == null) {
                owner.sendMessage("No suitable recipient found for the prank.");
                return true;
            }

            // collect armor from target
            PlayerInventory inv = target.getInventory();
            ItemStack helmet = inv.getHelmet();
            ItemStack chest = inv.getChestplate();
            ItemStack legs = inv.getLeggings();
            ItemStack boots = inv.getBoots();
            boolean hasArmor = (helmet != null && helmet.getType() != Material.AIR) ||
                    (chest != null && chest.getType() != Material.AIR) ||
                    (legs != null && legs.getType() != Material.AIR) ||
                    (boots != null && boots.getType() != Material.AIR);
            if (!hasArmor) {
                owner.sendMessage("Target has no armor to prank.");
                return true;
            }

            // remove armor from target (store copies)
            inv.setHelmet(null);
            inv.setChestplate(null);
            inv.setLeggings(null);
            inv.setBoots(null);

            // spawn an armor stand to carry the items
            Location spawn = target.getLocation().add(0, 1.0, 0);
            ArmorStand stand = (ArmorStand) target.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);

            // set armor on stand; for helmet, if player head desired, set skull owner
            if (helmet != null && helmet.getType() == Material.SKULL_ITEM) {
                try {
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.setOwner(target.getName());
                    skull.setItemMeta(meta);
                    stand.setHelmet(skull);
                } catch (Throwable t) {
                    stand.setHelmet(helmet);
                }
            } else if (helmet != null) {
                stand.setHelmet(helmet);
            }
            if (chest != null) stand.setChestplate(chest);
            if (legs != null) stand.setLeggings(legs);
            if (boots != null) stand.setBoots(boots);

            owner.sendMessage("Armor pranked from " + target.getName() + " — running to " + nearest.getName());
            target.sendMessage("Your armor just jumped off... watch out!");

            // schedule movement toward nearest player
            new MovingStandTask(this, stand, nearest, owner, target, helmet, chest, legs, boots).runTaskTimer(this, 1L, 1L);

            // set cooldown
            int cd = getConfig().getInt("cooldown_seconds", 60);
            cooldowns.put(owner.getUniqueId(), System.currentTimeMillis() + cd * 1000L);

            return true;
        });
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ArmorPrank enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("ArmorPrank disabled");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        // TODO: clean up any moving stands owned by this player
    }

    // inner class to move the armor stand toward a player
    static class MovingStandTask extends BukkitRunnable {
        private final JavaPlugin plugin;
        private final ArmorStand stand;
        private final Player targetPlayer;
        private final Player owner;
        private final Player originalTarget;
        private final ItemStack helmet, chest, legs, boots;
        private int ticks = 0;

        MovingStandTask(JavaPlugin plugin, ArmorStand stand, Player targetPlayer, Player owner, Player originalTarget,
                        ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
            this.plugin = plugin;
            this.stand = stand;
            this.targetPlayer = targetPlayer;
            this.owner = owner;
            this.originalTarget = originalTarget;
            this.helmet = helmet;
            this.chest = chest;
            this.legs = legs;
            this.boots = boots;
        }

        @Override
        public void run() {
            if (stand == null || stand.isDead()) {
                cancel();
                return;
            }
            if (!targetPlayer.isOnline()) {
                // drop items and remove
                dropAndCleanup(stand.getLocation());
                cancel();
                return;
            }
            Location cur = stand.getLocation();
            Location dest = targetPlayer.getLocation();
            double dist = cur.distance(dest);
            if (dist < 1.5) {
                // reached: drop items at target
                dropAndCleanup(dest.clone().add(0, 1, 0));
                // message
                targetPlayer.sendMessage("An armor pile sprinted to you! Beware the prank.");
                owner.sendMessage("Prank delivered to " + targetPlayer.getName());
                cancel();
                return;
            }
            // move toward target
            double step = 0.6; // blocks per tick
            double dx = dest.getX() - cur.getX();
            double dy = dest.getY() - cur.getY();
            double dz = dest.getZ() - cur.getZ();
            double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (len < 0.001) return;
            double nx = cur.getX() + (dx/len)*step;
            double ny = cur.getY() + (dy/len)*step;
            double nz = cur.getZ() + (dz/len)*step;
            Location next = new Location(cur.getWorld(), nx, ny, nz, dest.getYaw(), dest.getPitch());
            stand.teleport(next);
            // play step sound occasionally
            if (ticks % 4 == 0) {
                cur.getWorld().playSound(cur, "mob.zombie.step", 0.6f, 1.2f);
            }
            ticks++;
            // safety: if runs too long, return
            if (ticks > 20 * 60 * 2) { // 2 minutes
                dropAndCleanup(stand.getLocation());
                cancel();
            }
        }

        private void dropAndCleanup(Location loc) {
            if (helmet != null && helmet.getType() != Material.AIR) loc.getWorld().dropItemNaturally(loc, helmet);
            if (chest != null && chest.getType() != Material.AIR) loc.getWorld().dropItemNaturally(loc, chest);
            if (legs != null && legs.getType() != Material.AIR) loc.getWorld().dropItemNaturally(loc, legs);
            if (boots != null && boots.getType() != Material.AIR) loc.getWorld().dropItemNaturally(loc, boots);
            try { stand.remove(); } catch (Throwable ignored) {}
        }
    }
}
