package shadowlord.dimensions;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final ShadowDimensions plugin;
    private final Map<UUID, Location[]> selections = new HashMap<>();

    public SelectionManager(ShadowDimensions plugin) {
        this.plugin = plugin;
    }

    public void setPointA(UUID player, Location a) {
        Location[] arr = selections.getOrDefault(player, new Location[2]);
        arr[0] = a;
        selections.put(player, arr);
    }

    public void setPointB(UUID player, Location b) {
        Location[] arr = selections.getOrDefault(player, new Location[2]);
        arr[1] = b;
        selections.put(player, arr);
    }

    public Location[] getSelection(UUID player) {
        return selections.get(player);
    }
}
