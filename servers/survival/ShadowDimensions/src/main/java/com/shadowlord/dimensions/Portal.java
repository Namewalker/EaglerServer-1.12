package shadowlord.dimensions;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

public class Portal {

    private final String name; // target dimension name
    private final String worldName; // world where portal exists
    private final int x1,y1,z1,x2,y2,z2;
    private final String targetDimension;

    public Portal(String targetDimension, Location a, Location b, String worldName) {
        this.targetDimension = targetDimension;
        this.worldName = worldName;
        this.name = targetDimension + "-" + Math.abs(UUID.randomUUID().toString().hashCode() % 10000);
        this.x1 = Math.min(a.getBlockX(), b.getBlockX());
        this.y1 = Math.min(a.getBlockY(), b.getBlockY());
        this.z1 = Math.min(a.getBlockZ(), b.getBlockZ());
        this.x2 = Math.max(a.getBlockX(), b.getBlockX());
        this.y2 = Math.max(a.getBlockY(), b.getBlockY());
        this.z2 = Math.max(a.getBlockZ(), b.getBlockZ());
    }

    public String getName() { return name; }
    public String getWorldName() { return worldName; }
    public String getTargetDimension() { return targetDimension; }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public String serialize() {
        // simple CSV: name|world|target|x1|y1|z1|x2|y2|z2
        return name + "|" + worldName + "|" + targetDimension + "|" + x1 + "|" + y1 + "|" + z1 + "|" + x2 + "|" + y2 + "|" + z2;
    }

    public static Portal deserialize(String s) {
        try {
            String[] parts = s.split("\\|");
            if (parts.length != 10) return null;
            String name = parts[0];
            String world = parts[1];
            String target = parts[2];
            int x1 = Integer.parseInt(parts[3]);
            int y1 = Integer.parseInt(parts[4]);
            int z1 = Integer.parseInt(parts[5]);
            int x2 = Integer.parseInt(parts[6]);
            int y2 = Integer.parseInt(parts[7]);
            int z2 = Integer.parseInt(parts[8]);
            // last part is extra because of 10 expected; adjust if needed
            // Build dummy locations (world lookup later)
            org.bukkit.World w = org.bukkit.Bukkit.getWorld(world);
            if (w == null) return null;
            Location a = new Location(w, x1, y1, z1);
            Location b = new Location(w, x2, y2, z2);
            Portal p = new Portal(target, a, b, world);
            return p;
        } catch (Exception e) {
            return null;
        }
    }

    public String boundsString() {
        return x1 + "," + y1 + "," + z1 + " -> " + x2 + "," + y2 + "," + z2;
    }
}
