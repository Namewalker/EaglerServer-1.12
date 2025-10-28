package com.eagler.bookportal.listeners;

import com.eagler.bookportal.BookPortalPlugin;
import com.eagler.bookportal.dimension.DimensionManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookPortalListener implements Listener {

    private final BookPortalPlugin plugin;
    private final DimensionManager dimensionManager;

    public BookPortalListener(BookPortalPlugin plugin, DimensionManager dimensionManager) {
        this.plugin = plugin;
        this.dimensionManager = dimensionManager;
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

        // create or open world based on title (null/blank -> random)
        dimensionManager.createOrOpenFromTitle(title, ev.getTo());
    }
}
