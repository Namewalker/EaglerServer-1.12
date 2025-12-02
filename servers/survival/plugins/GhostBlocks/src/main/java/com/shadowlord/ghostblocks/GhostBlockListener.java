package shadowlord.ghostblocks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack inHand = e.getItemInHand();
        if (inHand == null) return;
        if (!isGhostItem(inHand)) return;

        Player p = e.getPlayer();
        Block placed = e.getBlockPlaced();
        Material illusionMat = getGhostMaterial(inHand);
        byte data = getGhostData(inHand);

        Bukkit.getScheduler().runTask(GhostBlocksPlugin.getInstance(), () -> {
            placed.setType(Material.AIR);
            GhostBlock gb = new GhostBlock(placed.getLocation(), illusionMat, data);
            manager.add(gb);
        });

        p.sendMessage("§7[GhostBlocks] §fPlaced illusion as §e" + illusionMat.name() + "§f (data " + data + ").");
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
