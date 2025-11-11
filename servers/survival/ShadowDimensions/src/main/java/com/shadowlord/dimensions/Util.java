package shadowlord.dimensions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String locString(Location l) {
        return l.getWorld().getName() + ":" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    public static ItemStack createWand(FileConfiguration cfg) {
        Material mat = Material.getMaterial(cfg.getString("wand.material", "BLAZE_ROD"));
        ItemStack item = new ItemStack(mat != null ? mat : Material.BLAZE_ROD);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', cfg.getString("wand.name", "&6Portal Selector")));
        List<String> lore = new ArrayList<>();
        for (String l : cfg.getStringList("wand.lore")) lore.add(ChatColor.translateAlternateColorCodes('&', l));
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }
}
