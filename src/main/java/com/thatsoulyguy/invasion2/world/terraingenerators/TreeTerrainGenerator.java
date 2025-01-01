package com.thatsoulyguy.invasion2.world.terraingenerators;

import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.TerrainGenerator;
import org.joml.Random;
import org.joml.Vector3i;

public class TreeTerrainGenerator extends TerrainGenerator
{
    private static final int TREE_HEIGHT = 5;
    private static final int LEAVES_RADIUS = 2;

    @Override
    public void generateBlocks(short[][][] blocks, Vector3i chunkPosition)
    {
        for (int x = 0; x < Chunk.SIZE; x++)
        {
            for (int z = 0; z < Chunk.SIZE; z++)
            {
                int terrainHeight = -1;

                for (int y = Chunk.SIZE - 1; y >= 0; y--)
                {
                    if (blocks[x][y][z] != BlockRegistry.BLOCK_AIR.getId())
                    {
                        terrainHeight = y;
                        break;
                    }
                }

                if (terrainHeight >= 0 && blocks[x][terrainHeight][z] == BlockRegistry.BLOCK_GRASS.getId())
                {
                    if (new Random().nextInt(50) == 4)
                        generateTree(blocks, x, terrainHeight + 1, z);
                }
            }
        }
    }

    private void generateTree(short[][][] blocks, int x, int y, int z)
    {
        if (y + TREE_HEIGHT + LEAVES_RADIUS >= Chunk.SIZE)
            return;

        for (int i = 0; i < TREE_HEIGHT; i++)
            blocks[x][y + i][z] = BlockRegistry.BLOCK_LOG_OAK.getId();

        for (int dx = -LEAVES_RADIUS; dx <= LEAVES_RADIUS; dx++)
        {
            for (int dz = -LEAVES_RADIUS; dz <= LEAVES_RADIUS; dz++)
            {
                for (int dy = TREE_HEIGHT - 2; dy <= TREE_HEIGHT; dy++)
                {
                    int leafX = x + dx;
                    int leafY = y + dy;
                    int leafZ = z + dz;

                    if (leafX >= 0 && leafX < Chunk.SIZE && leafY >= 0 && leafY < Chunk.SIZE && leafZ >= 0 && leafZ < Chunk.SIZE)
                    {
                        if (Math.abs(dx) + Math.abs(dz) <= LEAVES_RADIUS)
                            blocks[leafX][leafY][leafZ] = BlockRegistry.BLOCK_LEAVES.getId();
                    }
                }
            }
        }
    }
}