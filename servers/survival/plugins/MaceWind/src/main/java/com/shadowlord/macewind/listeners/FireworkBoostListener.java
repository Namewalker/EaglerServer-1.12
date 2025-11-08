package com.shadowlord.macewind.listeners;

import com.shadowlord.macewind.MaceWindPlugin;
import com.shadowlord.macewind.util.ItemUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class FireworkBoostListener implements Listener {

    private final MaceWindPlugin plugin;

    public FireworkBoostListener(MaceWindPlugin plugin) {
        this.plugin = plugin;
    }

    // Right-click air with a firework in hand to launch it by hand.
    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        // only handle main hand interactions and right-click-air (hand-launched)
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == null) return;
        String act = event.getAction().name();
        if (!act.equals("RIGHT_CLICK_AIR")) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        // identify fireworks by material name to avoid enum-compile issues
        String matName = item.getType() == null ? "" : item.getType().name();
        if (!matName.toUpperCase().contains("FIREWORK")) return;

        World w = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        // Spawn a visual firework entity and send it forward
        try {
            Firework fw = (Firework) w.spawnEntity(eye.add(dir.multiply(0.5)), EntityType.FIREWORK);
            // preserve existing meta if present (simple default otherwise)
            FireworkMeta meta = fw.getFireworkMeta();
            fw.setFireworkMeta(meta);
            // launch forward
            double launchSpeed = plugin.getConfig().getDouble("firework.launch-speed", 1.4);
            fw.setVelocity(dir.clone().multiply(launchSpeed));
        } catch (Exception ignored) {
            // some lightweight server implementations may not support spawning Firework entity; ignore
        }

        // If player is gliding with Elytra, apply boost
        boolean isGliding = player.isGliding();
        boolean wearingElytra = false;
        try {
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() != null) {
                wearingElytra = player.getInventory().getChestplate().getType().name().equalsIgnoreCase("ELYTRA");
            }
        } catch (Throwable ignored) {}

        if (isGliding && wearingElytra) {
            double horiz = plugin.getConfig().getDouble("firework.boost-horizontal", 1.6);
            double vert = plugin.getConfig().getDouble("firework.boost-vertical", 0.6);
            double preserveY = plugin.getConfig().getDouble("firework.boost-preserve-y", 0.2);

            Vector look = player.getLocation().getDirection().normalize();
            Vector boost = look.multiply(horiz);
            // blend existing Y so boost feels smooth
            double newY = Math.max(boost.getY(), player.getVelocity().getY() + vert);
            boost.setY(newY * preserveY + boost.getY() * (1.0 - preserveY));

            player.setVelocity(boost);

            // particles/sound feedback
            try {
                Sound s = Sound.valueOf(plugin.getConfig().getString("firework.feedback.sound", "ENTITY_FIREWORK_LAUNCH"));
                w.playSound(player.getLocation(), s, 1.0f, 1.0f);
            } catch (Exception ignored) {}
            if (plugin.getConfig().getBoolean("firework.feedback.effect-visual", true)) {
                w.playEffect(player.getLocation(), Effect.FIREWORKS_SPARK, 0);
            }
        } else {
            // not gliding: just play a visual/sound effect for thrown firework
            try {
                Sound s = Sound.valueOf(plugin.getConfig().getString("firework.feedback.sound", "ENTITY_FIREWORK_LAUNCH"));
                w.playSound(player.getLocation(), s, 0.8f, 1.0f);
            } catch (Exception ignored) {}
            if (plugin.getConfig().getBoolean("firework.feedback.effect-visual", true)) {
                w.playEffect(player.getLocation(), Effect.FIREWORKS_SPARK, 0);
            }
        }

        // consume one firework unless in creative
        try {
            if (player.getGameMode() == null || !player.getGameMode().name().equals("CREATIVE")) {
                int amt = item.getAmount();
                if (amt <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    item.setAmount(amt - 1);
                    player.getInventory().setItemInMainHand(item);
                }
            }
        } catch (Throwable ignored) {}

        // cancel default interaction to avoid weird side effects
        event.setCancelled(true);
    }
}
