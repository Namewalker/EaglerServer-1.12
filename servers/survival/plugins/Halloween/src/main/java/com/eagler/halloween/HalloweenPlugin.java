package com.eagler.halloween;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HalloweenPlugin extends JavaPlugin implements Listener {

    private final Random random = new Random();

    // A few scary skull textures (base64) for skull GUI and armor-stand heads.
    // These are base64-encoded JSON profiles pointing to textures.minecraft.net URLs.
    // Replace or extend these with other base64 texture strings if you want different faces.
    private final String[] SKULL_TEXTURES = new String[] {
            // Creepy skull texture
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2YzMzE3ZTg0NDI5MjM0M2Q0ODhlY2I4ZjFkM2QzYjE3M2I3ZTUxNTQ2ZDcxMDRlYjZiY2Y4YzA0ZTg2In19fQ==",
            // Hollow-eyed skull
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTQyNzYyZjA2ZjJiYjQzY2JhY2M0YzVhY2U5ZDRkMzY2Y2VhZDk4Y2QyY2E0YzVhMjI0ZDYzMzQifX19",
            // Distorted face
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk0YmM1OTMwZjFhZDYwODRkMjE4NjU4ZDM5M2E2YjM2ZTI2ZjE5YzE3MzBmOTk2Y2QzOTQ0YzY3In19fQ=="
    };

    // Track next allowed scare time per player (ms timestamp). Helps space scares so players can't predict them.
    private final Map<UUID, Long> nextAllowedScare = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Halloween plugin enabling...");
        getServer().getPluginManager().registerEvents(this, this);

        // ambient noises and rare random scares: run every 15 seconds and very rarely trigger a surprise for a random online player
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (random.nextInt(200) == 0) { // very rare ambient blip
                        playAmbient(p);
                    }
                }

                // rare global surprise: pick a random player occasionally
                if (random.nextInt(1200) == 0) {
                    Player[] arr = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                    if (arr.length > 0) {
                        Player p = arr[random.nextInt(arr.length)];
                        if (canScare(p)) {
                            triggerMassiveScream(p);
                            scheduleNextAllowed(p);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L * 15L);
    }

    @Override
    public void onDisable() {
        getLogger().info("Halloween plugin disabling...");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        Player p = ev.getPlayer();
        // Make scares very unpredictable: combine a low-probability movement trigger plus per-player spacing
        int baseChance = p.isSprinting() ? 6 : 1; // small chance per move event
        if (random.nextInt(20000) < baseChance && canScare(p)) {
            int type = random.nextInt(6);
            switch (type) {
                case 0:
                    triggerJumpscare(p);
                    break;
                case 1:
                    triggerSkullGui(p);
                    break;
                case 2:
                    triggerArmorStandHead(p);
                    break;
                case 3:
                    triggerMassiveScream(p);
                    break;
                case 4:
                    triggerHotbarPrank(p);
                    break;
                case 5:
                    triggerTeleportPrank(p);
                    break;
            }
            scheduleNextAllowed(p);
        }
    }

    private void playAmbient(Player p) {
        Sound s = findSound("ENTITY_PHANTOM_AMBIENT", "ENTITY_GHAST_SCREAM", "ENTITY_ZOMBIE_AMBIENT", "GHAST_SCREAM", "ZOMBIE_AMBIENT");
        if (s != null) p.playSound(p.getLocation(), s, 1.0f, 0.6f);
    }

    // Original jumpscare but with added nausea and flicker
    private void triggerJumpscare(final Player p) {
        final Location loc = p.getLocation();
        getLogger().info("Triggering jumpscare for " + p.getName());

        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 1));

        Sound s1 = findSound("ENTITY_GHAST_SCREAM", "GHAST_SCREAM", "ENTITY_ZOMBIE_INFECT", "ZOMBIE_INFECT");
        if (s1 != null) p.playSound(loc, s1, 3.0f, 0.4f);

        p.sendTitle("§4It's behind you!", "§cLook away...", 5, 60, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location spawn = loc.clone().add(-1 + random.nextDouble() * 2, 1, -1 + random.nextDouble() * 2);
                Firework fw = (Firework) p.getWorld().spawnEntity(spawn, EntityType.FIREWORK);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(Color.BLACK).with(FireworkEffect.Type.BALL_LARGE).withFade(Color.PURPLE).withFlicker().build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }
        }.runTaskLater(this, 6L);

        // final pop
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    p.getWorld().spawnEntity(loc.clone().add(0, 0, 2), EntityType.SKELETON);
                    p.getWorld().strikeLightningEffect(loc.clone().add(0, 0, 2));
                } catch (Throwable ignored) {}
            }
        }.runTaskLater(this, 12L);
    }

    // Opens a small inventory with a scary skull in the middle and plays a laugh
    private void triggerSkullGui(final Player p) {
        getLogger().info("Skull GUI jumpscare for " + p.getName());
        ItemStack skull = createCustomSkull(randomTexture());
        if (skull == null) return;

        Inventory inv = Bukkit.createInventory(null, 9, "§4... What was that?");
        inv.setItem(4, skull);
        p.openInventory(inv);

        Sound s2 = findSound("ENTITY_WITCH_AMBIENT", "WITCH_AMBIENT", "ENTITY_WITCH_AMBIENT");
        if (s2 != null) p.playSound(p.getLocation(), s2, 2.0f, 0.8f);

        // close and remove after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                try { p.closeInventory(); } catch (Throwable ignored) {}
            }
        }.runTaskLater(this, 30L);
    }

    // Spawn an invisible armor stand with a skull head right in front of the player for a brief, startling visual
    private void triggerArmorStandHead(final Player p) {
        getLogger().info("ArmorStand head jumpscare for " + p.getName());
        Location loc = p.getLocation().clone().add(p.getLocation().getDirection().normalize().multiply(2)).add(0, 1, 0);
        final ArmorStand stand = (ArmorStand) p.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);

        ItemStack skull = createCustomSkull(randomTexture());
        if (skull != null) stand.setHelmet(skull);

        Sound s3 = findSound("ENTITY_ENDERMEN_SCREAM", "ENDERMEN_SCREAM", "ENTITY_ENDERMAN_SCREAM", "ENDERMAN_SCREAM", "ENTITY_GHAST_SCREAM");
        if (s3 != null) p.playSound(loc, s3, 3.0f, 0.7f);

        new BukkitRunnable() {
            @Override
            public void run() {
                try { stand.remove(); } catch (Throwable ignored) {}
            }
        }.runTaskLater(this, 40L);
    }

    // A big, loud scream affecting nearby players
    private void triggerMassiveScream(final Player p) {
        getLogger().info("Massive scream for " + p.getName());
        Sound s4 = findSound("ENTITY_WITHER_SPAWN", "WITHER_SPAWN", "ENTITY_GHAST_SCREAM");
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getWorld().equals(p.getWorld()) && other.getLocation().distance(p.getLocation()) < 20) {
                if (s4 != null) other.playSound(other.getLocation(), s4, 4.0f, 0.4f);
                other.sendTitle("§4A chilling laugh...", "§7You feel someone close.", 5, 60, 10);
            }
        }
    }

    // Try multiple names to find a Sound constant compatible with server version
    private Sound findSound(String... names) {
        for (String n : names) {
            try {
                return Sound.valueOf(n);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    // helper to pick a texture
    private String randomTexture() {
        return SKULL_TEXTURES[random.nextInt(SKULL_TEXTURES.length)];
    }

    // Create a player skull ItemStack from a base64 texture string. Returns null if it fails.
    private ItemStack createCustomSkull(String base64) {
        try {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (base64 != null && !base64.isEmpty()) {
                GameProfile profile = new GameProfile(java.util.UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", base64));
                try {
                    Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta, profile);
                } catch (NoSuchFieldException ex) {
                    // older/newer servers may differ; try Bukkit-constructor style
                    // ignore if we can't set via reflection
                }
            }
            meta.setDisplayName("§cBOO!");
            skull.setItemMeta(meta);
            return skull;
        } catch (Throwable t) {
            getLogger().warning("Failed to create custom skull: " + t.getMessage());
            return null;
        }
    }

    // ---- New prank/jumpscare helpers ----

    // Swap player's hotbar with garbage for a few seconds
    private void triggerHotbarPrank(final Player p) {
        getLogger().info("Hotbar prank for " + p.getName());
        final ItemStack[] saved = new ItemStack[9];
        for (int i = 0; i < 9; i++) saved[i] = p.getInventory().getItem(i);

        ItemStack rotten = new ItemStack(Material.ROTTEN_FLESH, 1);
        for (int i = 0; i < 9; i++) p.getInventory().setItem(i, rotten);
        Sound s = findSound("ENTITY_PIG_DEATH", "PIG_DEATH", "ENTITY_PLAYER_BURP");
        if (s != null) p.playSound(p.getLocation(), s, 2.0f, 0.8f);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 9; i++) p.getInventory().setItem(i, saved[i]);
                } catch (Throwable ignored) {}
            }
        }.runTaskLater(this, 60L);
    }

    // Teleport a short distance away and then return the player
    private void triggerTeleportPrank(final Player p) {
        getLogger().info("Teleport prank for " + p.getName());
        final Location orig = p.getLocation().clone();
        Location away = orig.clone().add((random.nextDouble() - 0.5) * 6, 0, (random.nextDouble() - 0.5) * 6);
        try { p.teleport(away); } catch (Throwable ignored) {}
        Sound s = findSound("ENTITY_ENDERMEN_TELEPORT", "ENDERMEN_TELEPORT");
        if (s != null) p.playSound(p.getLocation(), s, 3.0f, 0.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                try { p.teleport(orig); } catch (Throwable ignored) {}
            }
        }.runTaskLater(this, 40L);
    }

    // Small fake-death prank: send fake death message and brief blackout
    private void triggerFakeDeath(final Player p) {
        getLogger().info("Fake death prank for " + p.getName());
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1));
        p.sendTitle("§4You died...", "§7But it was only a prank.", 10, 60, 10);
        Bukkit.getServer().broadcastMessage("§8[§4⚰§8] §c" + p.getName() + " was slain by an unknown horror...");
    }

    // Decide if a player can be scared now based on per-player cooldown
    private boolean canScare(Player p) {
        Long next = nextAllowedScare.get(p.getUniqueId());
        long now = System.currentTimeMillis();
        return next == null || now >= next;
    }

    // Schedule the next allowed scare time for a player: random between 1 and 8 minutes
    private void scheduleNextAllowed(Player p) {
        long now = System.currentTimeMillis();
        int min = 60_000; // 1 minute
        int range = 7 * 60_000; // + up to 7 minutes = 1-8 minutes
        long next = now + min + random.nextInt(range);
        nextAllowedScare.put(p.getUniqueId(), next);
    }

}
