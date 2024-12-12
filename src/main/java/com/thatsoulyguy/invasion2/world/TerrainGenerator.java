package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.noise.OpenSimplex2;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.io.Serializable;

@CustomConstructor("create")
public class TerrainGenerator implements Serializable //TODO: Add a system where multiple terrain generators all contribute to final terrain
{
    private double scale;
    private int chunkSize;
    private long seed;

    private TerrainGenerator() { }

    /**
     * Generates the height for a given x and z coordinate using Perlin noise.
     *
     * @param x World X coordinate
     * @param z World Z coordinate
     * @return Height at the given (x, z) position
     */
    private int getHeight(int x, int z)
    {
        double flatnessControl = OpenSimplex2.noise2(seed + 300, x * scale * 0.1, z * scale * 0.1);
        flatnessControl = (flatnessControl + 1) / 2.0;

        double hillThreshold = 0.7;

        double baseNoise = OpenSimplex2.noise2(seed, x * scale, z * scale);
        baseNoise = (baseNoise + 1) / 2.0;

        double hillNoise = 0;

        if (flatnessControl > hillThreshold)
        {
            hillNoise = OpenSimplex2.noise2(seed + 100, x * scale * 0.5, z * scale * 0.5);
            hillNoise = (hillNoise + 1) / 2.0;
        }

        double combinedNoise = flatnessControl <= hillThreshold
                ? baseNoise * 0.2
                : (0.3 * baseNoise + 0.7 * hillNoise);

        int maxTerrainHeight = World.WORLD_HEIGHT;

        int rawHeight = (int) (combinedNoise * maxTerrainHeight);

        int minHeight = 6 * chunkSize;

        return rawHeight + minHeight;
    }


    /**
     * Fills the blocks array based on the generated height.
     *
     * @param blocks The blocks array to fill
     * @param chunkPosition The position of the chunk in chunk coordinates
     */
    public void generateBlocks(short[][][] blocks, Vector3i chunkPosition)
    {
        int worldXOffset = chunkPosition.x * chunkSize;
        int worldZOffset = chunkPosition.z * chunkSize;
        int worldYOffset = chunkPosition.y * chunkSize;

        for (int x = 0; x < chunkSize; x++)
        {
            for (int z = 0; z < chunkSize; z++)
            {
                int worldX = worldXOffset + x;
                int worldZ = worldZOffset + z;

                int terrainHeight = getHeight(worldX, worldZ);

                for (int y = 0; y < chunkSize; y++)
                {
                    int worldY = worldYOffset + y;

                    if (worldY < terrainHeight)
                    {
                        if (worldY == terrainHeight - 1)
                            blocks[x][y][z] = BlockRegistry.BLOCK_GRASS.getID();
                        else if (worldY >= terrainHeight - 5)
                            blocks[x][y][z] = BlockRegistry.BLOCK_DIRT.getID();
                        else
                            blocks[x][y][z] = BlockRegistry.BLOCK_STONE.getID();
                    }
                    else
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getID();
                }
            }
        }
    }

    public static @NotNull TerrainGenerator create(double scale, int chunkSize, long seed)
    {
        TerrainGenerator result = new TerrainGenerator();

        result.scale = scale;
        result.chunkSize = chunkSize;
        result.seed = seed;

        return result;
    }
}