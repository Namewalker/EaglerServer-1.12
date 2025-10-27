package com.eagler;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import org.bukkit.util.Vector;

public class WorldEditSimple extends JavaPlugin implements Listener {
    // Helper class for undo functionality
    private static class BlockStateSnapshot {
        private final Material type;
        private final byte data;
        private final int x, y, z;
        BlockStateSnapshot(Block block) {
            this.type = block.getType();
            this.data = block.getData();
            this.x = block.getX();
            this.y = block.getY();
            this.z = block.getZ();
        }
        void restore(org.bukkit.World world) {
            Block block = world.getBlockAt(x, y, z);
            block.setType(type);
            block.setData(data);
        }
    }
    private Map<UUID, Location[]> selections = new HashMap<>();
    private Map<Player, SphereToolParams> sphereToolParams = new HashMap<>();
    private Map<Player, Deque<List<BlockStateSnapshot>>> undoHistory = new HashMap<>();
    private Map<Player, Deque<List<BlockStateSnapshot>>> redoHistory = new HashMap<>();

    private static class SphereToolParams {
        int radius;
        Material material;
        SphereToolParams(int radius, Material material) {
            this.radius = radius;
            this.material = material;
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cYou must be an operator (op) to use WorldEditSimple commands.");
            return true;
        }
        if (label.equalsIgnoreCase("replacenear")) {
            if (args.length != 3) {
                player.sendMessage("§eUsage: /replacenear <range> <target block> <replacement block>");
                return true;
            }
            int radius;
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid range. Must be a number.");
                return true;
            }
            Material target = matchBlock(args[1]);
            Material replacement = matchBlock(args[2]);
            if (target == null || replacement == null) {
                player.sendMessage("§cUnknown block type(s).");
                return true;
            }
            Location loc = player.getLocation();
            int replaced = 0;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location checkLoc = loc.clone().add(x, y, z);
                        Block block = checkLoc.getBlock();
                        if (block.getType() == target) {
                            block.setType(replacement);
                            replaced++;
                        }
                    }
                }
            }
            player.sendMessage("§aReplaced " + replaced + " blocks of " + target.name() + " with " + replacement.name() + ".");
            return true;
        }
        if (label.equalsIgnoreCase("wand")) {
            player.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
            player.sendMessage("§aWand given! Left/right click blocks to select region.");
            return true;
        }
        if (label.equalsIgnoreCase("set")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /set <block>");
                return true;
            }
            Location[] sel = selections.get(player.getUniqueId());
            if (sel == null || sel[0] == null || sel[1] == null) {
                player.sendMessage("§cSelect two points first with the wand.");
                return true;
            }
            Material mat = matchBlock(args[0]);
            if (mat == null) {
                player.sendMessage("§cUnknown block type.");
                return true;
            }
            int minX = Math.min(sel[0].getBlockX(), sel[1].getBlockX());
            int maxX = Math.max(sel[0].getBlockX(), sel[1].getBlockX());
            int minY = Math.min(sel[0].getBlockY(), sel[1].getBlockY());
            int maxY = Math.max(sel[0].getBlockY(), sel[1].getBlockY());
            int minZ = Math.min(sel[0].getBlockZ(), sel[1].getBlockZ());
            int maxZ = Math.max(sel[0].getBlockZ(), sel[1].getBlockZ());
            List<BlockStateSnapshot> changedBlocks = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = player.getWorld().getBlockAt(x, y, z);
                        changedBlocks.add(new BlockStateSnapshot(block));
                        block.setType(mat);
                    }
                }
            }
            undoHistory.computeIfAbsent(player, k -> new ArrayDeque<>()).push(changedBlocks);
            player.sendMessage("§6Region set to " + mat.name() + "!");
            return true;
        }
        if (label.equalsIgnoreCase("tool") && args.length >= 3 && args[0].equalsIgnoreCase("sphere")) {
            int radius;
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid radius.");
                return true;
            }
            Material mat = matchBlock(args[2]);
            if (mat == null) {
                player.sendMessage("§cUnknown block type.");
                return true;
            }
            sphereToolParams.put(player, new SphereToolParams(radius, mat));
            player.getInventory().addItem(new ItemStack(Material.STONE_AXE));
            player.sendMessage("§aSphere tool enabled! Right-click a block with your stone axe to place a sphere of " + mat.name() + " (radius " + radius + ").");
            return true;
        }
        if (label.equalsIgnoreCase("undo")) {
            Deque<List<BlockStateSnapshot>> history = undoHistory.get(player);
            if (history == null || history.isEmpty()) {
                player.sendMessage("§cNo actions to undo.");
                return true;
            }
            List<BlockStateSnapshot> lastChange = history.pop();
            for (BlockStateSnapshot snapshot : lastChange) {
                snapshot.restore(player.getWorld());
            }
            // Save undone change for redo
            redoHistory.computeIfAbsent(player, k -> new ArrayDeque<>()).push(lastChange);
            player.sendMessage("§aUndo successful!");
            return true;
        }
        if (label.equalsIgnoreCase("redo")) {
            Deque<List<BlockStateSnapshot>> history = redoHistory.get(player);
            if (history == null || history.isEmpty()) {
                player.sendMessage("§cNo actions to redo.");
                return true;
            }
            List<BlockStateSnapshot> lastUndone = history.pop();
            List<BlockStateSnapshot> redoSnapshots = new ArrayList<>();
            for (BlockStateSnapshot snapshot : lastUndone) {
                Block block = player.getWorld().getBlockAt(snapshot.x, snapshot.y, snapshot.z);
                redoSnapshots.add(new BlockStateSnapshot(block));
                block.setType(snapshot.type);
                block.setData(snapshot.data);
            }
            // Save redo as new undo
            undoHistory.computeIfAbsent(player, k -> new ArrayDeque<>()).push(redoSnapshots);
            player.sendMessage("§aRedo successful!");
            return true;
        }
    return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    // Sphere tool uses STONE_AXE, region selection uses WOOD_AXE
    if (sphereToolParams.containsKey(player)) {
        if (player.getInventory().getItemInMainHand().getType() != Material.STONE_AXE) return;
    } else {
        if (player.getInventory().getItemInMainHand().getType() != Material.WOOD_AXE) return;
    }
    // Sphere tool mode: right-click places sphere at block you are pointing at (distance)
    if (event.getAction().toString().contains("RIGHT_CLICK") && sphereToolParams.containsKey(player)) {
            // Ray trace from player eye to find block in sight (up to 100 blocks)
            Block targetBlock = null;
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection().normalize();
            for (int i = 0; i < 100; i++) {
                Location check = eye.clone().add(dir.clone().multiply((double)i));
                Block b = check.getBlock();
                if (b.getType() != Material.AIR) {
                    targetBlock = b;
                    break;
                }
            }
            if (targetBlock == null) {
                player.sendMessage("§cNo block in sight within range.");
                return;
            }
            Location loc = targetBlock.getLocation();
            SphereToolParams params = sphereToolParams.get(player);
            int radius = params.radius;
            Material mat = params.material;
            List<BlockStateSnapshot> changedBlocks = new ArrayList<>();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radius * radius) {
                            Block block = player.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                            changedBlocks.add(new BlockStateSnapshot(block));
                            block.setType(mat);
                        }
                    }
                }
            }
            undoHistory.computeIfAbsent(player, k -> new ArrayDeque<>()).push(changedBlocks);
            player.sendMessage("§aSphere placed at " + locString(loc) + "!");
            event.setCancelled(true);
            return;
        }
        // Wand selection logic
        if (event.getClickedBlock() == null) return;
        if (!selections.containsKey(player.getUniqueId())) selections.put(player.getUniqueId(), new Location[2]);
        Location[] sel = selections.get(player.getUniqueId());
        if (event.getAction().toString().contains("LEFT_CLICK")) {
            sel[0] = event.getClickedBlock().getLocation();
            player.sendMessage("§aFirst position set: " + locString(sel[0]));
        } else if (event.getAction().toString().contains("RIGHT_CLICK")) {
            sel[1] = event.getClickedBlock().getLocation();
            player.sendMessage("§aSecond position set: " + locString(sel[1]));
        }
    }

    // Support block aliases for <block> parameter
    private Material matchBlock(String name) {
    // Water aliases
    if (name.equals("water") || name.equals("flowing_water") || name.equals("water_block") || name.equals("water_source")) return Material.WATER;
    if (name.equals("stationary_water") || name.equals("still_water")) return Material.STATIONARY_WATER;
        name = name.toLowerCase().replace(" ", "_").replace("-", "_");
        switch (name) {
            // Planks
            case "oak_planks": case "planks": case "wood": return Material.WOOD;
            case "birch_planks": return Material.WOOD;
            case "spruce_planks": return Material.WOOD;
            case "jungle_planks": return Material.WOOD;
            case "acacia_planks": return Material.WOOD;
            case "dark_oak_planks": return Material.WOOD;
            // Logs
            case "oak_log": case "log": return Material.LOG;
            case "birch_log": return Material.LOG;
            case "spruce_log": return Material.LOG;
            case "jungle_log": return Material.LOG;
            case "acacia_log": return Material.LOG;
            case "dark_oak_log": return Material.LOG;
            case "stone_bricks": case "stonebrick": case "stonebricks": return Material.SMOOTH_BRICK;
            case "polished_andesite": return Material.STONE;
            case "cobble": case "cobblestone": return Material.COBBLESTONE;
            case "glass": return Material.GLASS;
            case "leaves": return Material.LEAVES;
            case "sand": return Material.SAND;
            case "gravel": return Material.GRAVEL;
            case "dirt": return Material.DIRT;
            case "grass": return Material.GRASS;
            case "brick": case "bricks": return Material.BRICK;
            case "quartz": return Material.QUARTZ_BLOCK;
            case "clay": return Material.CLAY;
            case "ice": return Material.ICE;
            case "snow": return Material.SNOW_BLOCK;
            case "obsidian": return Material.OBSIDIAN;
            case "bedrock": return Material.BEDROCK;
            case "netherrack": return Material.NETHERRACK;
            case "soul_sand": return Material.SOUL_SAND;
            case "end_stone": return Material.ENDER_STONE;
            case "lapis": return Material.LAPIS_BLOCK;
            case "redstone": return Material.REDSTONE_BLOCK;
            case "emerald": return Material.EMERALD_BLOCK;
            case "diamond": return Material.DIAMOND_BLOCK;
            case "gold": return Material.GOLD_BLOCK;
            case "iron": return Material.IRON_BLOCK;
            case "coal": return Material.COAL_BLOCK;
            case "tnt": return Material.TNT;
            case "bookshelf": return Material.BOOKSHELF;
            case "mossy_cobble": case "mossy_cobblestone": return Material.MOSSY_COBBLESTONE;
            case "slab": case "stone_slab": return Material.STEP;
            case "wood_slab": case "plank_slab": return Material.WOOD_STEP;
            case "sandstone": return Material.SANDSTONE;
            case "red_sand": case "redsand": return Material.SANDSTONE; // No RED_SANDSTONE in 1.12.2
            case "purpur": return Material.ENDER_STONE; // No PURPUR_BLOCK in 1.12.2
            case "purpur_pillar": return Material.ENDER_STONE; // No PURPUR_PILLAR in 1.12.2
            case "purpur_slab": return Material.STEP;
            case "purpur_stairs": return Material.SMOOTH_STAIRS;
            case "nether_brick": case "nether_bricks": return Material.NETHER_BRICK;
            case "nether_brick_fence": return Material.NETHER_FENCE;
            case "nether_brick_stairs": return Material.NETHER_BRICK_STAIRS;
            case "end_bricks": case "end_brick": return Material.ENDER_STONE;
            case "prismarine": return Material.ENDER_STONE; // No PRISMARINE in 1.12.2
            case "prismarine_bricks": return Material.ENDER_STONE;
            case "dark_prismarine": return Material.ENDER_STONE;
            case "sea_lantern": return Material.GLOWSTONE;
            case "concrete": return Material.HARD_CLAY;
            case "concrete_powder": return Material.CLAY;
            case "terracotta": return Material.HARD_CLAY;
            case "glazed_terracotta": return Material.HARD_CLAY;
            case "wool": return Material.WOOL;
            case "carpet": return Material.CARPET;
            case "glass_pane": return Material.THIN_GLASS;
            case "stained_glass": return Material.STAINED_GLASS;
            case "stained_glass_pane": return Material.STAINED_GLASS_PANE;
            case "clay_block": return Material.CLAY;
            case "hardened_clay": return Material.HARD_CLAY;
            case "packed_ice": return Material.PACKED_ICE;
            case "melon": return Material.MELON_BLOCK;
            case "pumpkin": return Material.PUMPKIN;
            case "hay": case "hay_block": return Material.HAY_BLOCK;
            case "mycelium": return Material.MYCEL;
            case "podzol": return Material.DIRT; // No PODZOL in 1.12.2
            case "coarse_dirt": return Material.DIRT;
            case "snow_layer": return Material.SNOW;
            case "mushroom": case "red_mushroom": return Material.RED_MUSHROOM;
            case "brown_mushroom": return Material.BROWN_MUSHROOM;
            case "stone": return Material.STONE;
            case "granite": return Material.STONE; // Use data for variants if needed
            case "diorite": return Material.STONE;
            case "andesite": return Material.STONE;
            case "fence": return Material.FENCE;
            case "fence_gate": return Material.FENCE_GATE;
            case "stairs": case "stone_stairs": return Material.SMOOTH_STAIRS;
            // Add more aliases as needed
            default:
                return Material.matchMaterial(name.toUpperCase());
        }
    }

    private String locString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
