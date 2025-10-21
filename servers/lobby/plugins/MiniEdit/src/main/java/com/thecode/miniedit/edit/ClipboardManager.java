package com.thecode.miniedit.edit;

import org.bukkit.Material;

import java.util.*;

public class ClipboardManager {
    private final Map<UUID, List<BlockData>> clipboard = new HashMap<>();

    public void copy(UUID playerId, List<BlockData> blocks) {
        clipboard.put(playerId, new ArrayList<>(blocks));
    }

    public List<BlockData> getClipboard(UUID playerId) {
        return clipboard.get(playerId);
    }

    public static class BlockData {
        public final int x, y, z;
        public final Material material;
        public BlockData(int x, int y, int z, Material material) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }
    }
}
