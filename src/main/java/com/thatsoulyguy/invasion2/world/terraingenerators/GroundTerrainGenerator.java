package com.thatsoulyguy.invasion2.world.terraingenerators;

import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.noise.OpenSimplex2;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.TerrainGenerator;
import com.thatsoulyguy.invasion2.world.World;
import org.joml.Vector3i;

public class GroundTerrainGenerator extends TerrainGenerator
{
    public int getHeight(int x, int z)
    {
        double flatnessControl = OpenSimplex2.noise2(getSeed() + 300, x * getScale() * 0.1, z * getScale() * 0.1);
        flatnessControl = (flatnessControl + 1) / 2.0;

        double hillThreshold = 0.7;

        double baseNoise = OpenSimplex2.noise2(getSeed(), x * getScale(), z * getScale());
        baseNoise = (baseNoise + 1) / 2.0;

        double hillNoise = 0;

        if (flatnessControl > hillThreshold)
        {
            hillNoise = OpenSimplex2.noise2(getSeed() + 100, x * getScale() * 0.5, z * getScale() * 0.5);
            hillNoise = (hillNoise + 1) / 2.0;
        }

        double combinedNoise = flatnessControl <= hillThreshold
                ? baseNoise * 0.2
                : (0.3 * baseNoise + 0.7 * hillNoise);

        int maxTerrainHeight = World.WORLD_HEIGHT;

        int rawHeight = (int) (combinedNoise * maxTerrainHeight);

        int minHeight = 6 * Chunk.SIZE;

        return rawHeight + minHeight;
    }

    @Override
    public void generateBlocks(short[][][] blocks, Vector3i chunkPosition)
    {
        int worldXOffset = chunkPosition.x * Chunk.SIZE;
        int worldZOffset = chunkPosition.z * Chunk.SIZE;
        int worldYOffset = chunkPosition.y * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++)
        {
            for (int z = 0; z < Chunk.SIZE; z++)
            {
                int worldX = worldXOffset + x;
                int worldZ = worldZOffset + z;

                int terrainHeight = getHeight(worldX, worldZ);

                for (int y = 0; y < Chunk.SIZE; y++)
                {
                    int worldY = worldYOffset + y;

                    if (worldY < terrainHeight)
                    {
                        if (worldY == terrainHeight - 1)
                            blocks[x][y][z] = BlockRegistry.BLOCK_GRASS.getId();
                        else if (worldY >= terrainHeight - 5)
                            blocks[x][y][z] = BlockRegistry.BLOCK_DIRT.getId();
                        else
                            blocks[x][y][z] = BlockRegistry.BLOCK_STONE.getId();
                    }
                    else
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
                }
            }
        }
    }
}