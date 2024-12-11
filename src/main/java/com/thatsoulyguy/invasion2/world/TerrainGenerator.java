package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.noise.OpenSimplex2;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.io.Serializable;

@CustomConstructor("create")
public class TerrainGenerator implements Serializable
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
        double noiseValue = OpenSimplex2.noise2(seed, x * scale, z * scale);

        noiseValue = (noiseValue + 1) / 2.0;

        int maxTerrainHeight = World.WORLD_HEIGHT;

        return (int) (noiseValue * maxTerrainHeight);
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