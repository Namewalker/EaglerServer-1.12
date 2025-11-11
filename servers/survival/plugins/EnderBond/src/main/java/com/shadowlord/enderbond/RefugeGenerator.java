package shadowlord.enderbond;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class RefugeGenerator extends ChunkGenerator {

    // 1.12-friendly generator using generateChunkData(World, Random, int, int)
    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, org.bukkit.generator.ChunkGenerator.BiomeGrid biome) {
        ChunkData cd = createChunkData(world);

        int absX = Math.abs(cx);
        int absZ = Math.abs(cz);
        // generate islands only near center chunks to keep world mostly void
        if (absX <= 2 && absZ <= 2) {
            int baseY = 62;
            // small obsidian plate
            for (int x = 4; x < 12; x++) {
                for (int z = 4; z < 12; z++) {
                    cd.setBlock(x, baseY, z, Material.OBSIDIAN);
                    if (random.nextDouble() < 0.06) {
                        // endstone material name differs across versions; prefer END_STONE, fallback to ENDSTONE
                        try {
                            cd.setBlock(x, baseY + 1, z, Material.valueOf("END_STONE"));
                        } catch (IllegalArgumentException ex) {
                            // fallback older naming
                            try { cd.setBlock(x, baseY + 1, z, Material.valueOf("ENDSTONE")); } catch (Throwable t) {}
                        }
                    }
                    if (random.nextDouble() < 0.02) {
                        try { cd.setBlock(x, baseY + 1, z, Material.CHORUS_FLOWER); } catch (Throwable t) {}
                    }
                    if (random.nextDouble() < 0.03) {
                        try { cd.setBlock(x, baseY + 1, z, Material.PURPUR_BLOCK); } catch (Throwable t) {}
                    }
                }
            }
            // small pillars
            for (int i = 0; i < 6; i++) {
                int px = 5 + random.nextInt(6);
                int pz = 5 + random.nextInt(6);
                int h = 1 + random.nextInt(4);
                for (int y = 1; y <= h; y++) {
                    cd.setBlock(px, baseY + y, pz, Material.OBSIDIAN);
                }
            }
        }
        return cd;
    }
}
