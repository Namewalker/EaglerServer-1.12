package shadowlord.randomdrops;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class DropConfigGUI implements Listener {
    private final RandomBlockDrops plugin;
    private final Map<UUID, GuiState> playerState = new HashMap<>();

    public DropConfigGUI(RandomBlockDrops plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Main GUI: pages of blocks. Left click = randomize that block's drop. Right click = open drop selector.
    public void openMainGui(Player player, int page) {
        List<Material> blocks = Arrays.stream(Material.values())
                .filter(this::isBlockLike)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());

        int perPage = 45;
        int start = page * perPage;
        int totalPages = Math.max(1, (blocks.size() + perPage - 1) / perPage);
        Inventory inv = Bukkit.createInventory(null, 54, "RandomDrops — Blocks " + (page + 1) + "/" + totalPages);

        // populate block slots
        for (int i = 0; i < perPage; i++) {
            int idx = start + i;
            if (idx >= blocks.size()) break;
            Material block = blocks.get(idx);

            ItemStack item = new ItemStack(block, 1);
            ItemMeta meta = item.getItemMeta();

            Material mapped = plugin.getDropManager().getDropFor(block);
            String mappedName = (mapped != null) ? mapped.name() : "NONE";

            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("Drop: " + mappedName);
                lore.add("Left click to randomize");
                lore.add("Right click to choose drop");
                meta.setLore(lore);
                item.setItemMeta(meta);
            } else {
                // Fallback: when the Material yields no ItemMeta (rare), wrap with a PAPER placeholder that can show lore/display
                ItemStack fallback = createNamed(Material.PAPER, block.name());
                ItemMeta fbMeta = fallback.getItemMeta();
                if (fbMeta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("Mapped to: " + mappedName);
                    lore.add("Original: " + block.name());
                    fbMeta.setLore(lore);
                    fallback.setItemMeta(fbMeta);
                }
                item = fallback;
            }

            inv.setItem(i, item);
        }

        // navigation and save/close controls
        inv.setItem(45, createNamed(Material.ARROW, "Previous Page"));
        inv.setItem(46, createNamed(Material.BARRIER, "Close and Save"));
        inv.setItem(47, createNamed(Material.PAPER, "Save (keep open)"));
        inv.setItem(51, createNamed(Material.ARROW, "Next Page"));
        inv.setItem(52, createNamed(Material.WOOD_DOOR, "Randomize All"));

        player.openInventory(inv);
        playerState.put(player.getUniqueId(), new GuiState(GuiType.MAIN, page));
    }

    // Drop selector: shows sample valid drops (paged)
    public void openDropSelector(Player player, Material block, int dropPage) {
        List<Material> drops = plugin.getDropManager().getValidDropsList();
        int perPage = 45;
        int start = dropPage * perPage;
        int totalPages = Math.max(1, (drops.size() + perPage - 1) / perPage);
        Inventory inv = Bukkit.createInventory(null, 54, "Select drop for " + block.name() + " " + (dropPage + 1) + "/" + totalPages);

        for (int i = 0; i < perPage; i++) {
            int idx = start + i;
            if (idx >= drops.size()) break;
            Material m = drops.get(idx);

            ItemStack item = new ItemStack(m, 1);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("Click to set drop to " + m.name());
                meta.setLore(lore);
                item.setItemMeta(meta);
            } else {
                // fallback to PAPER with displayable meta
                ItemStack fallback = createNamed(Material.PAPER, m.name());
                ItemMeta fbMeta = fallback.getItemMeta();
                if (fbMeta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("Click to set drop to " + m.name());
                    fbMeta.setLore(lore);
                    fallback.setItemMeta(fbMeta);
                }
                item = fallback;
            }

            inv.setItem(i, item);
        }

        inv.setItem(45, createNamed(Material.ARROW, "Previous Page"));
        inv.setItem(46, createNamed(Material.BARRIER, "Cancel"));
        inv.setItem(51, createNamed(Material.ARROW, "Next Page"));
        inv.setItem(52, createNamed(Material.ANVIL, "Randomize this block"));

        player.openInventory(inv);
        playerState.put(player.getUniqueId(), new GuiState(GuiType.SELECTOR, dropPage, block));
    }

    private boolean isBlockLike(Material m) {
        if (m == null) return false;
        if (m == Material.AIR) return false;
        // include everything that is a block per API
        if (m.isBlock()) return true;

        // Common suffixes used by block-like materials in 1.12.2
        String name = m.name();
        String[] suffixes = {
                "_PLANKS", "_SLAB", "_STAIRS", "_FENCE", "_FENCE_GATE", "_DOOR", "_CHEST", "_TRAPDOOR",
                "_BANNER", "_WALL", "_CARPET", "_HEAD", "_SKULL", "_BED", "_SIGN", "_ANVIL", "_ENCHANTMENT_TABLE",
                "_PORTAL", "_RAIL", "_PUMPKIN", "_MELON", "_LOG", "_LOG2", "_LEAVES", "_LEAVES2", "_SAPLING",
                "_STEP", "_HAY_BLOCK", "_HOPPER", "_BREWING_STAND", "_COAL_BLOCK", "_REDSTONE_BLOCK", "_ICE",
                "_PACKED_ICE", "_GLOWSTONE", "_SEA_LANTERN", "_END_PORTAL", "_END_ROD", "_BEACON"
        };
        for (String s : suffixes) {
            if (name.endsWith(s)) return true;
        }

        // explicit list of known block-like materials that may not be flagged by isBlock in some builds
        Material[] explicit = {
                Material.FLOWER_POT, Material.BONE, Material.ITEM_FRAME, Material.END_CRYSTAL, Material.COMMAND,
                Material.PISTON_STICKY_BASE, Material.PISTON_MOVING_PIECE, Material.PISTON_EXTENSION, Material.DRAGON_EGG
        };
        for (Material ex : explicit) {
            if (m == ex) return true;
        }

        return false;
    }

    private ItemStack createNamed(Material mat, String name) {
        ItemStack is = new ItemStack(mat, 1);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            is.setItemMeta(meta);
        }
        return is;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() == null) return;
        UUID puid = e.getWhoClicked().getUniqueId();
        if (!playerState.containsKey(puid)) return;

        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        GuiState state = playerState.get(puid);
        if (state == null) return;

        int slot = e.getRawSlot();
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (state.type == GuiType.MAIN) {
            handleMainClick(player, state.page, slot, e.isRightClick());
        } else if (state.type == GuiType.SELECTOR) {
            handleSelectorClick(player, state.page, slot, state.block, e.isRightClick());
        }
    }

    private void handleMainClick(Player player, int page, int slot, boolean rightClick) {
        if (slot == 46) { // Close and Save
            plugin.getDropManager().saveMappings();
            player.closeInventory();
            player.sendMessage("RandomBlockDrops mappings saved.");
            return;
        } else if (slot == 47) { // Save (keep open)
            plugin.getDropManager().saveMappings();
            openMainGui(player, page);
            player.sendMessage("Mappings saved.");
            return;
        } else if (slot == 45) { // prev
            openMainGui(player, Math.max(0, page - 1));
            return;
        } else if (slot == 51) { // next
            openMainGui(player, page + 1);
            return;
        } else if (slot == 52) { // Randomize all
            for (Material m : Material.values()) {
                if (isBlockLike(m)) plugin.getDropManager().randomizeDrop(m);
            }
            plugin.getDropManager().saveMappings();
            openMainGui(player, 0);
            player.sendMessage("All blocks randomized.");
            return;
        }

        int perPage = 45;
        int index = page * perPage + slot;
        List<Material> blocks = Arrays.stream(Material.values())
                .filter(this::isBlockLike)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
        if (index < 0 || index >= blocks.size()) return;
        Material block = blocks.get(index);

        if (!rightClick) {
            // left click = randomize this block
            Material newDrop = plugin.getDropManager().randomizeDrop(block);
            plugin.getDropManager().saveMappings();
            player.sendMessage(block.name() + " now drops " + newDrop.name());
            openMainGui(player, page);
        } else {
            // right click = open selector
            openDropSelector(player, block, 0);
        }
    }

    private void handleSelectorClick(Player player, int page, int slot, Material block, boolean rightClick) {
        if (slot == 46) { // cancel
            openMainGui(player, 0);
            return;
        } else if (slot == 45) { // prev
            openDropSelector(player, block, Math.max(0, page - 1));
            return;
        } else if (slot == 51) { // next
            openDropSelector(player, block, page + 1);
            return;
        } else if (slot == 52) { // randomize this block
            Material newDrop = plugin.getDropManager().randomizeDrop(block);
            plugin.getDropManager().saveMappings();
            player.sendMessage(block.name() + " randomized to " + newDrop.name());
            openMainGui(player, 0);
            return;
        }

        int perPage = 45;
        int index = page * perPage + slot;
        List<Material> drops = plugin.getDropManager().getValidDropsList();
        if (index < 0 || index >= drops.size()) return;
        Material chosen = drops.get(index);

        plugin.getDropManager().setDrop(block, chosen);
        plugin.getDropManager().saveMappings();
        player.sendMessage(block.name() + " will now drop " + chosen.name());
        openMainGui(player, 0);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        UUID puid = e.getPlayer().getUniqueId();
        playerState.remove(puid);
    }

    private static class GuiState {
        final GuiType type;
        final int page;
        final Material block; // only for selector

        GuiState(GuiType type, int page) {
            this(type, page, null);
        }

        GuiState(GuiType type, int page, Material block) {
            this.type = type;
            this.page = page;
            this.block = block;
        }
    }

    private enum GuiType { MAIN, SELECTOR }
}
