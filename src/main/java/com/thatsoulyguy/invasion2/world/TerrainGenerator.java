package com.thatsoulyguy.invasion2.world;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.io.Serializable;

public abstract class TerrainGenerator implements Serializable
{
    private double scale;
    private long seed;

    protected TerrainGenerator() { }

    /**
     * Fills the blocks array based on the generated height.
     *
     * @param blocks The blocks array to fill
     * @param chunkPosition The position of the chunk in chunk coordinates
     */
    public abstract void generateBlocks(short[][][] blocks, Vector3i chunkPosition);

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }

    public long getSeed()
    {
        return seed;
    }

    public void setSeed(long seed)
    {
        this.seed = seed;
    }

    public static <T extends TerrainGenerator> @NotNull T create(Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Terrain Generator! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}