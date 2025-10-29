package com.eagler.bookportal.listeners;

import com.eagler.bookportal.BookPortalPlugin;
import com.eagler.bookportal.dimension.DimensionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BookPortalListener implements Listener {

    private final BookPortalPlugin plugin;
    private final DimensionManager dimensionManager;
    // map from dropped Item entity UUID -> player UUID who dropped it
    private final Map<UUID, UUID> itemOwners = Collections.synchronizedMap(new HashMap<>());

    public BookPortalListener(BookPortalPlugin plugin, DimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent ev) {
        Item it = ev.getItemDrop();
        if (it == null) return;
        UUID itemId = it.getUniqueId();
        UUID ownerId = ev.getPlayer().getUniqueId();
        itemOwners.put(itemId, ownerId);
        // forget after 30 seconds to avoid leak
        Bukkit.getScheduler().runTaskLater(plugin, () -> itemOwners.remove(itemId), 20L * 30);
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent ev) {
        Entity ent = ev.getEntity();
        if (!(ent instanceof Item)) return;
        Item it = (Item) ent;
        ItemStack is = it.getItemStack();
        if (is == null) return;
        Material m = is.getType();
        if (m != Material.WRITTEN_BOOK && m != Material.BOOK_AND_QUILL) return;

        // attempt to read book title
        String title = null;
        try {
            if (is.hasItemMeta() && is.getItemMeta() instanceof BookMeta) {
                BookMeta bm = (BookMeta) is.getItemMeta();
                title = bm.getTitle();
            }
        } catch (Throwable ignore) {}

        // prevent the item from teleporting further / remove it
        ev.setCancelled(true);
        it.remove();

        // find owner (player who dropped it) if known
        Player owner = null;
        UUID ownerId = itemOwners.remove(it.getUniqueId());
        if (ownerId != null) owner = Bukkit.getPlayer(ownerId);

        // create or open world based on title (null/blank -> random) and teleport owner
        dimensionManager.createOrOpenFromTitle(title, ev.getTo(), owner);
    }
}
