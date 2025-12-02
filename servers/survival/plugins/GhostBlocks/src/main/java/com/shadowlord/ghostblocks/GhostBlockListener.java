package com.shadowlord.ghostblocks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GhostBlockListener implements Listener {
    private final GhostBlockManager manager;

    public GhostBlockListener(GhostBlockManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack inHand = e.getItemInHand();
        if (inHand == null) return;
        Player p = e.getPlayer();
        Block placed = e.getBlockPlaced();

        // If a non-ghost item is being placed where a ghost exists, block the placement
        if (!isGhostItem(inHand) && manager.hasAt(placed.getLocation())) {
            e.setCancelled(true);
            p.sendMessage("§7[GhostBlocks] §cYou cannot place a real block where a ghost block exists.");
            GhostBlock gb = manager.getAt(placed.getLocation());
            if (gb != null) {
                // Re-send to all nearby players immediately and shortly after to avoid client desync
                manager.renderIllusionToNearbyPlayers(gb);
                Bukkit.getScheduler().runTaskLater(GhostBlocksPlugin.getInstance(), () -> manager.renderIllusionToNearbyPlayers(gb), 1L);
                Bukkit.getScheduler().runTaskLater(GhostBlocksPlugin.getInstance(), () -> manager.renderIllusionToNearbyPlayers(gb), 5L);
            }
            return;
        }

        if (!isGhostItem(inHand)) return;

        Material illusionMat = getGhostMaterial(inHand);
        byte data = getGhostData(inHand);

        Bukkit.getScheduler().runTask(GhostBlocksPlugin.getInstance(), () -> {
            placed.setType(Material.AIR);
            GhostBlock gb = new GhostBlock(placed.getLocation(), illusionMat, data);
            manager.add(gb);
        });

        p.sendMessage("§7[GhostBlocks] §fPlaced illusion as §e" + illusionMat.name() + "§f (data " + data + ").");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b == null) return;
        if (manager.hasAt(b.getLocation())) {
            // prevent breaking a ghost by cancelling and re-sending illusion to nearby players
            e.setCancelled(true);
            GhostBlock gb = manager.getAt(b.getLocation());
            if (gb != null) {
                manager.renderIllusionToNearbyPlayers(gb);
                Bukkit.getScheduler().runTaskLater(GhostBlocksPlugin.getInstance(), () -> manager.renderIllusionToNearbyPlayers(gb), 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if (b == null) return;
            if (manager.hasAt(b.getLocation())) {
                // Cancel any interaction that would modify server state and aggressively re-render illusion
                e.setCancelled(true);
                GhostBlock gb = manager.getAt(b.getLocation());
                if (gb != null) {
                    // Re-send to all nearby players immediately and shortly after to avoid client desync
                    manager.renderIllusionToNearbyPlayers(gb);
                    Bukkit.getScheduler().runTaskLater(GhostBlocksPlugin.getInstance(), () -> manager.renderIllusionToNearbyPlayers(gb), 1L);
                    Bukkit.getScheduler().runTaskLater(GhostBlocksPlugin.getInstance(), () -> manager.renderIllusionToNearbyPlayers(gb), 5L);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;
        // render nearby illusions when player moves between chunks
        manager.renderAllToPlayer(e.getPlayer());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        // When chunk unloads then loads back we will re-render on load; keep data in memory
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (manager.hasAt(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (manager.hasAt(e.getToBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> manager.hasAt(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (manager.hasAt(b.getLocation())) { e.setCancelled(true); return; }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (manager.hasAt(b.getLocation())) { e.setCancelled(true); return; }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        manager.renderAllToPlayer(e.getPlayer());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk c = e.getChunk();
        manager.renderChunkIllusions(c);
    }

    private boolean isGhostItem(ItemStack stack) {
        if (stack.getItemMeta() == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.startsWith("§7GHOSTBLOCK:")) return true;
            }
        }
        return meta.hasDisplayName() && meta.getDisplayName().startsWith("§eGhostBlock:");
    }

    private Material getGhostMaterial(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.startsWith("§7GHOSTBLOCK:")) {
                    String[] parts = line.replace("§7GHOSTBLOCK:", "").split(":");
                    Material m = Material.matchMaterial(parts[0]);
                    return m != null ? m : Material.STONE;
                }
            }
        }
        if (meta.hasDisplayName()) {
            String name = meta.getDisplayName().replace("§eGhostBlock:", "").trim();
            Material m = Material.matchMaterial(name);
            return m != null ? m : Material.STONE;
        }
        return Material.STONE;
    }

    private byte getGhostData(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.startsWith("§7GHOSTBLOCK:")) {
                    String[] parts = line.replace("§7GHOSTBLOCK:", "").split(":");
                    if (parts.length >= 2) {
                        try { return (byte) Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                    }
                }
            }
        }
        return (byte) 0;
    }
}
