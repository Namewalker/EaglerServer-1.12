package com.shadowlord.macewind.listeners;

import com.shadowlord.macewind.MaceWindPlugin;
import com.shadowlord.macewind.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaceListener implements Listener {

    private final MaceWindPlugin plugin;
    private final Map<UUID, Double> fallStartY = new ConcurrentHashMap<>();
    private final Map<UUID, Double> recordedFallDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastHitTs = new ConcurrentHashMap<>();

    // regex to pick up "Density", "Density I", "Density V", "density 3", etc.
    private static final Pattern DENSITY_PATTERN = Pattern.compile("density\\s*(?:i{1,3}|iv|v|[1-5])?", Pattern.CASE_INSENSITIVE);

    public MaceListener(MaceWindPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (event.getTo() == null) return;
        double fromY = event.getFrom().getY();
        double toY = event.getTo().getY();
        UUID id = p.getUniqueId();

        if (toY < fromY && !p.isFlying() && !p.isGliding()) {
            fallStartY.putIfAbsent(id, fromY);
        }

        if (p.isOnGround()) {
            if (fallStartY.containsKey(id)) {
                double startY = fallStartY.remove(id);
                double fallDistance = startY - p.getLocation().getY();
                if (fallDistance < 0) fallDistance = 0;
                recordedFallDistance.put(id, fallDistance);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player p = (Player) event.getEntity();
        if (p.getInventory() != null && ItemUtils.isMace(p.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            recordedFallDistance.putIfAbsent(p.getUniqueId(), 0.0);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        if (!ItemUtils.isMace(attacker.getInventory().getItemInMainHand())) return;

        UUID attackerId = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        long debounce = plugin.getConfig().getLong("mace.hit-debounce-ms", 50);
        if (lastHitTs.containsKey(attackerId) && now - lastHitTs.get(attackerId) < debounce) {
            return;
        }
        lastHitTs.put(attackerId, now);

        double baseDamage = plugin.getConfig().getDouble("mace.base-damage", 11.0);
        double bonus = 0.0;

        Double fallDistance = recordedFallDistance.remove(attackerId);
        if (fallDistance == null) {
            double vy = attacker.getVelocity().getY();
            if (vy < 0) {
                fallDistance = Math.abs(vy) * 2.0;
            } else {
                fallDistance = 0.0;
            }
        }

        double threshold = plugin.getConfig().getDouble("mace.fall-threshold", 1.5);
        if (fallDistance > threshold) {
            double perBlock = plugin.getConfig().getDouble("mace.bonus-per-block", 3.0);

            // Determine Density level (using Sharpness/Enchantment.DAMAGE_ALL as the underlying enchant)
            int densityLevel = detectDensityLevel(attacker.getInventory().getItemInMainHand());
            double densityMultiplier = 1.0;
            if (densityLevel > 0) {
                // map numeric level to roman key used in config (I, II, III, IV, V)
                String key = "";
                switch (densityLevel) {
                    case 1: key = "I"; break;
                    case 2: key = "II"; break;
                    case 3: key = "III"; break;
                    case 4: key = "IV"; break;
                    default: key = "V"; break;
                }
                densityMultiplier = plugin.getConfig().getDouble("mace.density-multiplier." + key, 1.0);
            }

            bonus = (fallDistance - threshold) * perBlock * densityMultiplier;
            if (bonus < 0) bonus = 0;
            double maxBonus = plugin.getConfig().getDouble("mace.max-bonus", 15.0);
            if (bonus > maxBonus) bonus = maxBonus;
        }

        double total = baseDamage + bonus;
        event.setDamage(total);

        Entity target = event.getEntity();
        Location loc = target.getLocation();
        Vector knock = target.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        double hMult = plugin.getConfig().getDouble("mace.knockback.horizontal-multiplier", 0.6);
        double hCap = plugin.getConfig().getDouble("mace.knockback.horizontal-bonus-cap", 1.6);
        double vBase = plugin.getConfig().getDouble("mace.knockback.vertical-base", 0.3);
        double vCap = plugin.getConfig().getDouble("mace.knockback.vertical-bonus-cap", 0.6);

        knock.setY(vBase + Math.min(vCap, bonus / 6.0));
        knock.multiply(hMult + Math.min(hCap, bonus / 6.0));
        target.setVelocity(knock);

        loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 42);

        try {
            Sound s = Sound.valueOf(plugin.getConfig().getString("mace.feedback.sound", "ANVIL_LAND"));
            loc.getWorld().playSound(loc, s, 0.8f, 1.2f);
        } catch (IllegalArgumentException | NullPointerException ex) {
            try {
                Sound fallback = Sound.valueOf("ANVIL_USE");
                loc.getWorld().playSound(loc, fallback, 0.8f, 1.2f);
            } catch (Exception ignored) {
            }
        }

        if (bonus > 0.5 && plugin.getConfig().getBoolean("mace.feedback.action-message-enabled", true)) {
            String tpl = plugin.getConfig().getString("mace.feedback.action-message-template", "Crushing Blow! +{bonus} damage");
            String msg = tpl.replace("{bonus}", String.format("%.1f", bonus));
            attacker.sendMessage(ChatColor.GRAY + msg);
        }
    }

    /**
     * Detects Density level on the given ItemStack.
     * We interpret the Sharpness enchantment (Enchantment.DAMAGE_ALL) as Density
     * only if the item display name or any lore line contains the word "Density".
     * Returns 0 if no Density should apply, or 1..5 for the level.
     */
    private int detectDensityLevel(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        int sharpLevel = 0;
        try {
            sharpLevel = item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
            // defensive: if enchantment API not present, treat as zero
            sharpLevel = 0;
        }
        if (sharpLevel <= 0) return 0;

        // check display name for "Density"
        if (meta.hasDisplayName()) {
            String name = ChatColor.stripColor(meta.getDisplayName());
            Matcher m = DENSITY_PATTERN.matcher(name);
            if (m.find()) {
                return clampDensityLevel(sharpLevel);
            }
        }

        // check lore lines for "Density"
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    String plain = ChatColor.stripColor(line);
                    Matcher m = DENSITY_PATTERN.matcher(plain);
                    if (m.find()) {
                        return clampDensityLevel(sharpLevel);
                    }
                }
            }
        }

        // no density rename detected; do not treat sharpness as density
        return 0;
    }

    private int clampDensityLevel(int lvl) {
        if (lvl <= 0) return 0;
        if (lvl > 5) return 5;
        return lvl;
    }
}
