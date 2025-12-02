package shadowlord.ghostblocks;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GhostBlockManager {
    private final GhostBlocksPlugin plugin;
    private final Map<String, Map<BlockVector, GhostBlock>> blocks = new HashMap<>();
    private boolean debugParticles = false;

    public GhostBlockManager(GhostBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public int count() {
        return blocks.values().stream().mapToInt(Map::size).sum();
    }

    public void setDebugParticles(boolean enabled) {
        debugParticles = enabled;
    }

    public boolean isDebugParticles() {
        return debugParticles;
    }

    public boolean add(GhostBlock gb) {
        blocks.computeIfAbsent(gb.getWorldName(), k -> new HashMap<>());
        Map<BlockVector, GhostBlock> map = blocks.get(gb.getWorldName());
        BlockVector key = new BlockVector(gb.getX(), gb.getY(), gb.getZ());
        boolean added = map.put(key, gb) == null;
        if (added) {
            renderIllusionToNearbyPlayers(gb);
            if (debugParticles) showDebugParticle(gb.toLocation());
        }
        return added;
    }

    public boolean remove(Location loc) {
        String world = loc.getWorld().getName();
        Map<BlockVector, GhostBlock> map = blocks.get(world);
        if (map == null) return false;
        BlockVector key = new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        GhostBlock gb = map.remove(key);
        if (gb != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getName().equals(world) && p.getLocation().distanceSquared(loc) <= 64 * 64) {
                    p.sendBlockChange(loc, Material.AIR, (byte) 0);
                }
            }
            return true;
        }
        return false;
    }

    public List<GhostBlock> listInChunk(Chunk chunk) {
        Map<BlockVector, GhostBlock> map = blocks.get(chunk.getWorld().getName());
        if (map == null) return Collections.emptyList();
        int xMin = chunk.getX() << 4;
        int zMin = chunk.getZ() << 4;
        int xMax = xMin + 15;
        int zMax = zMin + 15;
        return map.values().stream()
                .filter(gb -> gb.getX() >= xMin && gb.getX() <= xMax && gb.getZ() >= zMin && gb.getZ() <= zMax)
                .collect(Collectors.toList());
    }

    public int clearRadius(Location center, int radius) {
        String world = center.getWorld().getName();
        Map<BlockVector, GhostBlock> map = blocks.get(world);
        if (map == null) return 0;
        int r2 = radius * radius;
        List<BlockVector> toRemove = new ArrayList<>();
        for (Map.Entry<BlockVector, GhostBlock> e : map.entrySet()) {
            GhostBlock gb = e.getValue();
            Location loc = gb.toLocation();
            if (loc.getWorld().equals(center.getWorld()) &&
                loc.distanceSquared(center) <= r2) {
                toRemove.add(e.getKey());
            }
        }
        for (BlockVector key : toRemove) {
            GhostBlock gb = map.remove(key);
            if (gb != null) {
                Location loc = gb.toLocation();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(loc.getWorld())) {
                        p.sendBlockChange(loc, Material.AIR, (byte) 0);
                    }
                }
            }
        }
        return toRemove.size();
    }

    public void renderIllusionToNearbyPlayers(GhostBlock gb) {
        Location loc = gb.toLocation();
        if (loc.getWorld() == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equals(gb.getWorldName())) continue;
            if (p.getLocation().distanceSquared(loc) <= 64 * 64) {
                p.sendBlockChange(loc, gb.getMaterial(), gb.getData());
            }
        }
    }

    public void renderChunkIllusions(Chunk chunk) {
        List<GhostBlock> list = listInChunk(chunk);
        if (list.isEmpty()) return;
        for (Player p : chunk.getWorld().getPlayers()) {
            for (GhostBlock gb : list) {
                p.sendBlockChange(gb.toLocation(), gb.getMaterial(), gb.getData());
                if (debugParticles) showDebugParticle(gb.toLocation());
            }
        }
    }

    public void renderAllToPlayer(Player p) {
        Map<BlockVector, GhostBlock> map = blocks.get(p.getWorld().getName());
        if (map == null) return;
        for (GhostBlock gb : map.values()) {
            if (p.getLocation().distanceSquared(gb.toLocation()) <= 128 * 128) {
                p.sendBlockChange(gb.toLocation(), gb.getMaterial(), gb.getData());
                if (debugParticles) showDebugParticle(gb.toLocation());
            }
        }
    }

    public void showDebugParticle(Location loc) {
        if (loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.001);
    }

    public void saveToDisk() {
        plugin.config().set("ghostblocks", null);
        ConfigurationSection root = plugin.config().createSection("ghostblocks");
        int i = 0;
        for (Map.Entry<String, Map<BlockVector, GhostBlock>> worldEntry : blocks.entrySet()) {
            for (GhostBlock gb : worldEntry.getValue().values()) {
                ConfigurationSection s = root.createSection(String.valueOf(i++));
                s.set("world", gb.getWorldName());
                s.set("x", gb.getX());
                s.set("y", gb.getY());
                s.set("z", gb.getZ());
                s.set("material", gb.getMaterial().name());
                s.set("data", (int) gb.getData());
            }
        }
        plugin.saveConfig();
    }

    public void loadFromDisk() {
        blocks.clear();
        ConfigurationSection root = plugin.config().getConfigurationSection("ghostblocks");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            String world = s.getString("world");
            int x = s.getInt("x");
            int y = s.getInt("y");
            int z = s.getInt("z");
            String matName = s.getString("material");
            int dataInt = s.getInt("data", 0);
            Material mat = Material.matchMaterial(matName);
            byte data = (byte) dataInt;
            World w = Bukkit.getWorld(world);
            if (w == null || mat == null) continue;
            Location loc = new Location(w, x, y, z);
            // Ensure server truth is air to keep behavior consistent
            loc.getBlock().setType(Material.AIR);
            add(new GhostBlock(loc, mat, data));
        }
    }

    public static class BlockVector {
        public final int x, y, z;
        public BlockVector(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BlockVector)) return false;
            BlockVector v = (BlockVector) o; return x==v.x && y==v.y && z==v.z;
        }
        @Override public int hashCode() { return Objects.hash(x, y, z); }
    }
}
