package shadowlord.ghostblocks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GhostBlocksCommand implements CommandExecutor, TabCompleter {
    private final GhostBlockManager manager;

    public GhostBlocksCommand(GhostBlockManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[GhostBlocks] §fUsage: /ghostblocks <give|list|clear|reload|debug>");
            return true;
        }
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "give":
                if (!(sender instanceof Player)) { sender.sendMessage("Only players can receive items."); return true; }
                if (!sender.hasPermission("ghostblocks.give")) { sender.sendMessage("§cNo permission."); return true; }
                if (args.length < 2) { sender.sendMessage("§7Usage: /ghostblocks give <MATERIAL> [data]"); return true; }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) { sender.sendMessage("§cUnknown material: " + args[1]); return true; }
                byte data = 0;
                if (args.length >= 3) {
                    try { data = (byte) Integer.parseInt(args[2]); } catch (Exception ex) { sender.sendMessage("§cInvalid data value."); return true; }
                }
                Player p = (Player) sender;
                ItemStack item = new ItemStack(Material.STONE, 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§eGhostBlock:" + mat.name());
                List<String> lore = new ArrayList<>();
                lore.add("§7GHOSTBLOCK:" + mat.name() + ":" + (int) data);
                lore.add("§8Places an illusion that looks solid but is air.");
                meta.setLore(lore);
                item.setItemMeta(meta);
                p.getInventory().addItem(item);
                sender.sendMessage("§7[GhostBlocks] §fGave a ghost block item for §e" + mat.name() + "§f (data " + data + ").");
                return true;

            case "list":
                if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
                Player lp = (Player) sender;
                Chunk c = lp.getLocation().getChunk();
                List<GhostBlock> list = manager.listInChunk(c);
                if (list.isEmpty()) { sender.sendMessage("§7[GhostBlocks] §fNo ghost blocks in this chunk."); }
                else {
                    sender.sendMessage("§7[GhostBlocks] §fGhost blocks in chunk (" + c.getX() + "," + c.getZ() + "): " + list.size());
                    for (GhostBlock gb : list) {
                        sender.sendMessage("§8- §f" + gb.getMaterial().name() + "§7@" + gb.getX() + "," + gb.getY() + "," + gb.getZ() + " data " + gb.getData());
                    }
                }
                return true;

            case "clear":
                if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
                if (!sender.hasPermission("ghostblocks.admin")) { sender.sendMessage("§cNo permission."); return true; }
                if (args.length < 2) { sender.sendMessage("§7Usage: /ghostblocks clear <radius>"); return true; }
                int radius;
                try { radius = Integer.parseInt(args[1]); } catch (Exception ex) { sender.sendMessage("§cInvalid radius."); return true; }
                Player cp = (Player) sender;
                int removed = manager.clearRadius(cp.getLocation(), radius);
                sender.sendMessage("§7[GhostBlocks] §fCleared §e" + removed + "§f ghost blocks within §e" + radius + "§f.");
                GhostBlocksPlugin.getInstance().getLogger().info("Cleared " + removed + " ghost blocks by " + sender.getName());
                return true;

            case "reload":
                if (!sender.hasPermission("ghostblocks.admin")) { sender.sendMessage("§cNo permission."); return true; }
                GhostBlocksPlugin.getInstance().reloadConfig();
                manager.loadFromDisk();
                sender.sendMessage("§7[GhostBlocks] §fReloaded configuration and illusions.");
                return true;

            case "debug":
                if (!sender.hasPermission("ghostblocks.admin")) { sender.sendMessage("§cNo permission."); return true; }
                manager.setDebugParticles(!manager.isDebugParticles());
                sender.sendMessage("§7[GhostBlocks] §fDebug particles: §e" + (manager.isDebugParticles() ? "ON" : "OFF"));
                if (sender instanceof Player) manager.renderAllToPlayer((Player) sender);
                return true;

            default:
                sender.sendMessage("§7[GhostBlocks] §fUnknown subcommand. Use: give, list, clear, reload, debug");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("give", "list", "clear", "reload", "debug");
        if ("give".equalsIgnoreCase(args[0]) && args.length == 2) {
            String prefix = args[1].toUpperCase();
            List<String> mats = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.isBlock() && m.name().startsWith(prefix)) mats.add(m.name());
            }
            Collections.sort(mats);
            return mats;
        }
        return Collections.emptyList();
    }
}
