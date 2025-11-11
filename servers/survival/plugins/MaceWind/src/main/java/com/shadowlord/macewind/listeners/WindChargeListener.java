package com.shadowlord.macewind.listeners;

import com.shadowlord.macewind.MaceWindPlugin;
import com.shadowlord.macewind.util.ItemUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Collection;

public class WindChargeListener implements Listener {

    private final MaceWindPlugin plugin;
    private static final String WIND_META = "MaceWind_WindCharge";

    public WindChargeListener(MaceWindPlugin plugin) {
        this.plugin = plugin;
    }

    // Player right-clicks with Wind Charge item -> spawn a SmallFireball and tag it
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().toString().contains("RIGHT")) return;

        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isWindCharge(inHand)) return;

        World w = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        SmallFireball fb = w.spawn(eye.add(dir.multiply(1.0)), SmallFireball.class);
        fb.setShooter(player);
        fb.setYield(0F);
        fb.setIsIncendiary(false);
        fb.setVelocity(dir.multiply(1.6));
        fb.setMetadata(WIND_META, new FixedMetadataValue(plugin, true));

        event.setCancelled(true);
    }

    // Handle projectile hit (use ProjectileHitEvent to control everything)
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if (!proj.hasMetadata(WIND_META)) return;

        Location center = proj.getLocation();
        World world = center.getWorld();

        // Visual only: lightweight swirl using a few Effect play calls (no sound)
        spawnSwirlEffects(world, center);

        double radius = plugin.getConfig().getDouble("windcharge.radius", 6.0);
        double centerMagnitude = plugin.getConfig().getDouble("windcharge.center-magnitude", 3.5);
        double minUp = plugin.getConfig().getDouble("windcharge.min-upward", 0.8);

        Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);
        for (Entity ent : nearby) {
            if (ent.equals(proj)) continue;
            double dx = ent.getLocation().distance(center);
            double strength = Math.max(0, (radius - dx) / radius);
            if (strength <= 0) continue;

            Vector dir = ent.getLocation().toVector().subtract(center.toVector()).normalize();
            if (dx < 1.2) {
                dir = new Vector(0, 1, 0);
            } else {
                dir.setY(Math.max(minUp, 0.6 + strength));
            }

            double magnitude = 1.0 + (centerMagnitude - 1.0) * strength;
            dir.multiply(magnitude);
            ent.setVelocity(dir);

            if (ent instanceof LivingEntity) {
                ((LivingEntity) ent).setFireTicks(0);
            }
        }

        proj.remove();
    }

    /**
     * Spawn a small, low-overhead swirl of particles/effects around center.
     * We keep the number of Effect calls small and only execute once so the
     * visual reads as a swirl without taxing clients or server.
     */
    private void spawnSwirlEffects(World world, Location center) {
        if (!plugin.getConfig().getBoolean("windcharge.feedback.enable-effect", true)) return;

        // Choose a server-side effect that is cheap and available on 1.12
        Effect effect = Effect.SMOKE; // subtle and cheap; adjust in config if desired
        int effectData = plugin.getConfig().getInt("windcharge.feedback.effect-data", 10);

        // sample a small number of points around a few concentric radii
        double[] radii = new double[] { 0.4, 1.0, 1.6 };
        int samplesPerRing = 8; // low count to avoid lag

        for (double r : radii) {
            for (int i = 0; i < samplesPerRing; i++) {
                double angle = (2 * Math.PI * i) / samplesPerRing + (r * 0.3);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                // slight vertical offset to give a spiral feel
                double y = 0.2 + (r * 0.15);

                Location pos = center.clone().add(x, y, z);
                try {
                    world.playEffect(pos, effect, effectData);
                } catch (Throwable ignored) {
                    // ensure no exception bubbles up on unusual server builds
                }
            }
        }
    }
}
