package shadowlord.randomdrops;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;

public class BlockBreakListener implements Listener {
    private final RandomBlockDrops plugin;

    public BlockBreakListener(RandomBlockDrops plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        // get mapped drop
        Material dropType = plugin.getDropManager().getDropFor(blockType);

        // prevent normal drops and set block to air
        event.setCancelled(true);
        block.setType(Material.AIR);

        // drop one item of the mapped type
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, 1));
    }
}
