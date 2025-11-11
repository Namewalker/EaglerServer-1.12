package shadowlord.windturrets;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class Turret {

    private final String id;           // unique id
    private final String owner;        // player name
    private final String worldName;
    private final int x, y, z;         // turret base location (quartz pillar block)
    private int radius;
    private boolean enabled;

    public Turret(String id, String owner, Location loc, int radius, boolean enabled) {
        this.id = id;
        this.owner = owner;
        this.worldName = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.radius = radius;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getOwner() { return owner; }
    public int getRadius() { return radius; }
    public void setRadius(int r) { this.radius = r; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }

    public Location getLocation() {
        return org.bukkit.Bukkit.getWorld(worldName) == null ? null :
                new Location(org.bukkit.Bukkit.getWorld(worldName), x + 0.5, y + 0.5, z + 0.5);
    }

    public boolean isAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;
        return loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z;
    }

    public void toConfigSection(ConfigurationSection sec) {
        sec.set("owner", owner);
        sec.set("world", worldName);
        sec.set("x", x);
        sec.set("y", y);
        sec.set("z", z);
        sec.set("radius", radius);
        sec.set("enabled", enabled);
    }

    public static Turret fromConfig(String id, ConfigurationSection sec) {
        if (sec == null) return null;
        String owner = sec.getString("owner", "unknown");
        String world = sec.getString("world", null);
        if (world == null) return null;
        int x = sec.getInt("x"), y = sec.getInt("y"), z = sec.getInt("z");
        int radius = sec.getInt("radius", 10);
        boolean enabled = sec.getBoolean("enabled", true);
        org.bukkit.World w = org.bukkit.Bukkit.getWorld(world);
        if (w == null) return null; // world must exist
        Location loc = new Location(w, x, y, z);
        return new Turret(id, owner, loc, radius, enabled);
    }
}
