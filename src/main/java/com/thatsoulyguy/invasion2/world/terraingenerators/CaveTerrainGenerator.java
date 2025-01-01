package com.thatsoulyguy.invasion2.world.terraingenerators;

import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.noise.OpenSimplex2;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.TerrainGenerator;
import org.joml.Vector3i;

public class CaveTerrainGenerator extends TerrainGenerator
{
    private static final double CAVE_THRESHOLD = 0.5;
    private static final double CAVE_SCALE = 0.045;

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
                for (int y = 0; y < Chunk.SIZE; y++)
                {
                    int worldX = worldXOffset + x;
                    int worldY = worldYOffset + y;
                    int worldZ = worldZOffset + z;

                    double caveNoise = OpenSimplex2.noise3_ImproveXZ(getSeed(),
                            worldX * CAVE_SCALE,
                            worldY * CAVE_SCALE,
                            worldZ * CAVE_SCALE);

                    if (caveNoise > CAVE_THRESHOLD)
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
                }
            }
        }
    }
}