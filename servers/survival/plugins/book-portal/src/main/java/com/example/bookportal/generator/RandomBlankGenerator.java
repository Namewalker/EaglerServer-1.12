package com.example.bookportal.generator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class RandomBlankGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        
        // Randomly generate terrain features
        generateRandomFeatures(chunkData, random);
        
        return chunkData;
    }

    private void generateRandomFeatures(ChunkData chunkData, Random random) {
        // Example of random generation logic
        int featureCount = random.nextInt(10) + 1; // Generate between 1 and 10 features
        for (int i = 0; i < featureCount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = random.nextInt(256); // Height from 0 to 255
            
            // Example: Place a random block type
            chunkData.setBlock(x, y, z, getRandomBlockType(random));
        }
    }

    private Material getRandomBlockType(Random random) {
        Material[] blockTypes = {
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.STONE,
            Material.OAK_LOG,
            Material.OAK_LEAVES
        };
        return blockTypes[random.nextInt(blockTypes.length)];
    }
}