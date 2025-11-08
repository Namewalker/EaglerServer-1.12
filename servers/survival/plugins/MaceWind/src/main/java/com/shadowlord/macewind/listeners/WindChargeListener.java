package com.shadowlord.macewind.listeners;

import com.shadowlord.macewind.MaceWindPlugin;
import com.shadowlord.macewind.util.ItemUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
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

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if (!proj.hasMetadata(WIND_META)) return;

        Location center = proj.getLocation();
        World world = center.getWorld();

        if (plugin.getConfig().getBoolean("windcharge.feedback.enable-sound", true)) {
            try {
                Sound s = Sound.valueOf(plugin.getConfig().getString("windcharge.feedback.sound", "ENTITY_ENDERDRAGON_FLAP"));
                world.playSound(center, s, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
        if (plugin.getConfig().getBoolean("windcharge.feedback.enable-effect", true)) {
            world.playEffect(center, Effect.SMOKE, plugin.getConfig().getInt("windcharge.feedback.effect-data", 10));
        }

        double radius = plugin.getConfig().getDouble("windcharge.radius", 6.0);
        double centerMagnitude = plugin.getConfig().getDouble("windcharge.center-magnitude", 3.5);
        double minUp = plugin.getConfig().getDouble("windcharge.min-upward", 0.8);

        // Tuned multipliers: increase from 0.5 to 0.75
        final double verticalMultiplier = 0.75;
        final double magnitudeMultiplier = 0.75;

        Collection<Entity> nearby = world.getNearbyEntities(center, radius, radius, radius);
        for (Entity ent : nearby) {
            if (ent.equals(proj)) continue;
            double dx = ent.getLocation().distance(center);
            double strength = Math.max(0, (radius - dx) / radius);
            if (strength <= 0) continue;

            Vector dir = ent.getLocation().toVector().subtract(center.toVector()).normalize();
            if (dx < 1.2) {
                dir = new Vector(0, 1 * verticalMultiplier, 0);
            } else {
                double computedUp = Math.max(minUp, 0.6 + strength);
                dir.setY(Math.max(0.0, computedUp * verticalMultiplier));
            }

            double magnitude = 1.0 + (centerMagnitude - 1.0) * strength;
            magnitude *= magnitudeMultiplier;
            dir.multiply(magnitude);

            ent.setVelocity(dir);

            if (ent instanceof LivingEntity) {
                ((LivingEntity) ent).setFireTicks(0);
            }
        }

        proj.remove();
    }
}
