package shadowlord.randomdrops;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class BlockDropManager {
    private final RandomBlockDrops plugin;
    private final Map<Material, Material> blockDropMap = new EnumMap<>(Material.class);
    private final List<Material> validDrops;
    private final Random random = new Random();

    public BlockDropManager(RandomBlockDrops plugin) {
        this.plugin = plugin;
        // validDrops are Materials that can be used as items (exclude AIR)
        validDrops = Arrays.stream(Material.values())
                .filter(m -> m.isItem() && m != Material.AIR)
                .collect(Collectors.toList());
        loadMappings();
        ensureMappings();
    }

    private void ensureMappings() {
        // Ensure every block type has a mapping; if missing, assign random
        for (Material m : Material.values()) {
            if (m.isBlock()) {
                blockDropMap.putIfAbsent(m, getRandomDrop());
            }
        }
    }

    public Material getDropFor(Material block) {
        Material drop = blockDropMap.get(block);
        if (drop == null) {
            drop = getRandomDrop();
            blockDropMap.put(block, drop);
        }
        return drop;
    }

    public void setDrop(Material block, Material drop) {
        if (block != null && drop != null && block.isBlock()) {
            blockDropMap.put(block, drop);
        }
    }

    public Material randomizeDrop(Material block) {
        Material drop = getRandomDrop();
        setDrop(block, drop);
        return drop;
    }

    private Material getRandomDrop() {
        if (validDrops.isEmpty()) return Material.DIRT;
        return validDrops.get(random.nextInt(validDrops.size()));
    }

    public Map<Material, Material> getAllMappings() {
        return Collections.unmodifiableMap(blockDropMap);
    }

    public void loadMappings() {
        blockDropMap.clear();
        Map<String, Object> map = plugin.getConfig().getConfigurationSection("block_to_drop") != null ?
                plugin.getConfig().getConfigurationSection("block_to_drop").getValues(false) : Collections.emptyMap();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            String val = String.valueOf(e.getValue());
            try {
                Material block = Material.valueOf(key);
                Material drop = Material.valueOf(val);
                if (block.isBlock()) {
                    blockDropMap.put(block, drop);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveMappings() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Material, Material> e : blockDropMap.entrySet()) {
            map.put(e.getKey().name(), e.getValue().name());
        }
        plugin.getConfig().set("block_to_drop", map);
        plugin.saveConfig();
    }

    public List<Material> getValidDropsList() {
        return Collections.unmodifiableList(validDrops);
    }
}
