package com.shadowlord.wardenmod.listeners;

import com.shadowlord.wardenmod.entity.Warden;
import com.shadowlord.wardenmod.achievements.Achievements;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

/**
 * Listener for Warden-related events
 * Handles attacks and particle effects
 */
public class WardenListener implements Listener {
    
    private final JavaPlugin plugin;
    private final Map<UUID, Warden> wardens = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, Integer> laserTasks = new HashMap<>();
    private final Map<UUID, Integer> darknessTasks = new HashMap<>();
    private final Random random = new Random();
    
    public WardenListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void registerWarden(IronGolem golem, Warden warden) {
        wardens.put(golem.getUniqueId(), warden);
    }
    
    public void unregisterWarden(UUID uuid) {
        wardens.remove(uuid);
        lastAttackTime.remove(uuid);
        
        // Cancel any running laser/darkness tasks
        if (laserTasks.containsKey(uuid)) {
            plugin.getServer().getScheduler().cancelTask(laserTasks.get(uuid));
            laserTasks.remove(uuid);
        }
        if (darknessTasks.containsKey(uuid)) {
            plugin.getServer().getScheduler().cancelTask(darknessTasks.get(uuid));
            darknessTasks.remove(uuid);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof IronGolem) {
            IronGolem golem = (IronGolem) event.getDamager();
            Warden warden = wardens.get(golem.getUniqueId());
            
            if (warden != null && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                
                // Warden deals high damage
                event.setDamage(21.0); // Warden deals 21 damage (10.5 hearts)
                
                // Create particle effects at attack location
                spawnAttackParticles(golem.getLocation(), target.getLocation());
                
                // Fire laser at target
                fireLaserAttack(golem, target);
                
                // Apply darkness effect to target
                applyDarknessEffect(target);
                
                // Award Sonic Boom Survivor achievement if target survives low health
                if (target instanceof Player) {
                    Player player = (Player) target;
                    double finalHealth = player.getHealth() - 21.0;
                    if (finalHealth > 0 && finalHealth <= 5) {
                        Achievements.giveSonicBoomSurvivor(player);
                    }
                }
                
                // Update attack time
                lastAttackTime.put(golem.getUniqueId(), System.currentTimeMillis());
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof IronGolem) {
            UUID uuid = entity.getUniqueId();
            Warden warden = wardens.get(uuid);
            
            if (warden != null) {
                // Spawn death particles
                spawnDeathParticles(entity.getLocation());
                
                // Award achievements to nearby players
                for (Entity nearby : entity.getNearbyEntities(30, 30, 30)) {
                    if (nearby instanceof Player) {
                        Player player = (Player) nearby;
                        
                        // Track Warden kills
                        int kills = player.hasMetadata("warden_kills") ? 
                            player.getMetadata("warden_kills").get(0).asInt() + 1 : 1;
                        player.setMetadata("warden_kills", 
                            new org.bukkit.metadata.FixedMetadataValue(plugin, kills)
                        );
                        
                        Achievements.giveWardenSlayer(player);
                        if (kills >= 5) {
                            Achievements.giveWardenHunter(player);
                        }
                    }
                }
                
                // Drop rare items on death
                entity.getLocation().getWorld().dropItemNaturally(
                    entity.getLocation(),
                    new org.bukkit.inventory.ItemStack(org.bukkit.Material.SKULL_ITEM)
                );
                
                unregisterWarden(uuid);
            }
        }
    }
    
    private void spawnAttackParticles(org.bukkit.Location from, org.bukkit.Location to) {
        org.bukkit.Location midpoint = from.clone().add(to).multiply(0.5);
        
        // Sonic boom particles - using explosion particles
        midpoint.getWorld().spawnParticle(
            Particle.EXPLOSION_LARGE,
            midpoint,
            3,
            0.5,
            0.5,
            0.5
        );
        
        // Add secondary dark particle effect
        midpoint.getWorld().spawnParticle(
            Particle.EXPLOSION_NORMAL,
            midpoint,
            8,
            0.8,
            0.8,
            0.8
        );
        
        // Add a trail from Warden to target
        double distance = from.distance(to);
        Vector direction = to.clone().subtract(from.clone()).toVector().normalize();
        
        for (double d = 0; d < distance; d += 0.5) {
            org.bukkit.Location particle_loc = from.clone().add(direction.clone().multiply(d));
            particle_loc.getWorld().spawnParticle(
                Particle.SPELL_WITCH,
                particle_loc,
                2,
                0.2,
                0.2,
                0.2
            );
        }
    }
    
    private void spawnDeathParticles(org.bukkit.Location location) {
        // Large explosion effect
        location.getWorld().spawnParticle(
            Particle.EXPLOSION_HUGE,
            location,
            1,
            0,
            0,
            0
        );
        
        // Smoke clouds
        location.getWorld().spawnParticle(
            Particle.SMOKE_LARGE,
            location,
            10,
            1,
            1,
            1
        );
        
        // Magical particles
        location.getWorld().spawnParticle(
            Particle.SPELL,
            location,
            15,
            1,
            1,
            1
        );
    }
    
    private void fireLaserAttack(final LivingEntity warden, final LivingEntity target) {
        // Create a laser beam effect from Warden to target
        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            new Runnable() {
                @Override
                public void run() {
                    if (warden.isDead() || target.isDead()) {
                        if (laserTasks.containsKey(warden.getUniqueId())) {
                            plugin.getServer().getScheduler().cancelTask(laserTasks.get(warden.getUniqueId()));
                            laserTasks.remove(warden.getUniqueId());
                        }
                        return;
                    }
                    
                    // Draw laser line from warden to target
                    org.bukkit.Location from = warden.getEyeLocation();
                    org.bukkit.Location to = target.getEyeLocation();
                    
                    double distance = from.distance(to);
                    Vector direction = to.clone().subtract(from.clone()).toVector().normalize();
                    
                    // Create laser beam with redstone particles
                    for (double d = 0; d < distance; d += 0.3) {
                        org.bukkit.Location particleLoc = from.clone().add(direction.clone().multiply(d));
                        particleLoc.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            particleLoc,
                            1,
                            0,
                            0,
                            0
                        );
                    }
                }
            },
            0L,
            2L  // Laser fires every 2 ticks
        );
        
        laserTasks.put(warden.getUniqueId(), taskId);
    }
    
    private void applyDarknessEffect(LivingEntity target) {
        if (target instanceof Player) {
            final Player player = (Player) target;
            
            // Apply darkness potion effect (if available in 1.12.2)
            try {
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.BLINDNESS, 60, 1),
                    true
                );
            } catch (Exception e) {
                // If BLINDNESS not available, use SLOW as alternative
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOW, 40, 2),
                    true
                );
            }
            
            // Apply vision-darkening with darkness particle effect
            int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        if (player.isDead()) {
                            if (darknessTasks.containsKey(player.getUniqueId())) {
                                plugin.getServer().getScheduler().cancelTask(darknessTasks.get(player.getUniqueId()));
                                darknessTasks.remove(player.getUniqueId());
                            }
                            return;
                        }
                        
                        // Spawn darkness particles around player
                        org.bukkit.Location playerLoc = player.getLocation();
                        playerLoc.getWorld().spawnParticle(
                            Particle.SMOKE_LARGE,
                            playerLoc.add(0, 1, 0),
                            15,
                            1.5,
                            1.5,
                            1.5,
                            0.1
                        );
                    }
                },
                0L,
                5L  // Darkness particles every 5 ticks
            );
            
            darknessTasks.put(player.getUniqueId(), taskId);
            
            // Cancel darkness effect after 3 seconds
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        if (darknessTasks.containsKey(player.getUniqueId())) {
                            plugin.getServer().getScheduler().cancelTask(darknessTasks.get(player.getUniqueId()));
                            darknessTasks.remove(player.getUniqueId());
                        }
                    }
                },
                60L  // 3 seconds (60 ticks)
            );
        }
    }
}
