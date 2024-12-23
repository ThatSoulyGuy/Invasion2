package com.thatsoulyguy.invasion2.util;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.world.Chunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

@Static
public class ChunkAlgorithms
{
    private ChunkAlgorithms() { }

    public static float[] getAmbientOcclusionLighting(@NotNull Vector3i position, @NotNull Vector3i normal, short[][][] blocks)
    {
        float[] lighting = new float[4];

        if (normal.x == 0 && normal.y == 0 && normal.z > 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position, -1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, 1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, 1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position, -1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, 1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1, 1, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1, 1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1, 1, blocks);
        }
        else if (normal.x == 0 && normal.y == 0 && normal.z < 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1, -1, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position, -1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1, -1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, -1, blocks);
        }
        else if (normal.x == 0 && normal.y > 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);
        }
        else if (normal.x == 0 && normal.y < 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);
        }
        else if (normal.x > 0 && normal.y == 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1,  0,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  0, blocks);
        }
        else if (normal.x < 0 && normal.y == 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1,  0,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  0, -1, blocks);
        }
        else
            lighting[0] = lighting[1] = lighting[2] = lighting[3] = 1.0f;

        return lighting;
    }

    private static float ambientOcclusionAlgorithm(@NotNull Vector3i base, int dx, int dy, int dz, short[][][] blocks)
    {
        Vector3i neighbor = new Vector3i(base.x + dx, base.y + dy, base.z + dz);

        return isBlockSolid(neighbor, blocks) ? 0.75f : 1.0f;
    }

    private static boolean isBlockSolid(@NotNull Vector3i position, short[][][] blocks)
    {
        if (position.x < 0 || position.x >= Chunk.SIZE ||
                position.y < 0 || position.y >= Chunk.SIZE ||
                position.z < 0 || position.z >= Chunk.SIZE)
            return false;

        return blocks[position.x][position.y][position.z] != BlockRegistry.BLOCK_AIR.getID();
    }
}