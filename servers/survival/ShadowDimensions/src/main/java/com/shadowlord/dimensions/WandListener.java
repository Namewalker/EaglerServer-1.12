package shadowlord.dimensions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class WandListener implements Listener {

    private final ShadowDimensions plugin;

    public WandListener(ShadowDimensions plugin) {
        this.plugin = plugin;
    }

    private boolean isWand(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.getMaterial(plugin.getConfig().getString("wand.material", "BLAZE_ROD"))) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta im = item.getItemMeta();
        if (!im.hasDisplayName()) return false;
        String name = ChatColor.stripColor(im.getDisplayName());
        String cfgName = ChatColor.stripColor(Util.createWand(plugin.getConfig()).getItemMeta().getDisplayName());
        return name.equals(cfgName);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getItem() == null) return;
        if (!isWand(ev.getItem())) return;

        Action action = ev.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Location clicked = ev.getClickedBlock().getLocation();
        UUID playerId = ev.getPlayer().getUniqueId();
        if (action == Action.LEFT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPointA(playerId, clicked);
            ev.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPoint A set at &e" + Util.locString(clicked)));
            ev.setCancelled(true);
            return;
        } else {
            plugin.getSelectionManager().setPointB(playerId, clicked);
            ev.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPoint B set at &e" + Util.locString(clicked)));
            ev.setCancelled(true);
            return;
        }
    }
}
