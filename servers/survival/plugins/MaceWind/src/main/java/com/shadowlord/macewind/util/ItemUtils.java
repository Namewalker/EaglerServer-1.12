package com.shadowlord.macewind.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemUtils {

    private ItemUtils() {}

    public static boolean isMace(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.DIAMOND_AXE) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Mace");
    }

    public static boolean isWindCharge(ItemStack item) {
        if (item == null) return false;

        Material mat = item.getType();
        Material desired = Material.getMaterial("FIRE_CHARGE"); // runtime lookup
        Material fallback = Material.getMaterial("FIREBALL"); // some server versions use FIREBALL
        boolean typeOk;
        if (desired != null) {
            typeOk = (mat == desired);
        } else if (fallback != null) {
            typeOk = (mat == fallback);
        } else {
            // last resort: accept common compile-time constant FIREBALL if present
            try {
                typeOk = (mat == Material.FIREBALL);
            } catch (NoSuchFieldError | Exception ex) {
                return false;
            }
        }

        if (!typeOk) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Wind Charge");
    }

    public static ItemStack makeMace() {
        ItemStack i = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.RESET + "Mace");
        i.setItemMeta(m);
        return i;
    }

    public static ItemStack makeWindCharge() {
        Material mat = Material.getMaterial("FIRE_CHARGE");
        if (mat == null) mat = Material.getMaterial("FIREBALL");
        if (mat == null) {
            // last-resort compile-time constant (should exist on 1.12)
            try {
                mat = Material.FIREBALL;
            } catch (NoSuchFieldError | Exception ex) {
                // fallback to FIREBALL name lookup failing means we cannot construct; use null guard
                mat = null;
            }
        }
        if (mat == null) mat = Material.FIREBALL; // defensive, if still null this may throw at runtime on very old API
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.RESET + "Wind Charge");
        i.setItemMeta(m);
        return i;
    }
}
