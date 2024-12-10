package com.thatsoulyguy.invasion2.block;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class Block
{
    private static short idCounter = 0;

    private final short id;

    public Block()
    {
        id = idCounter++;
    }

    public abstract @NotNull String getDisplayName();

    public abstract @NotNull String getRegistryName();

    public abstract float getHardness();

    public abstract float getResistance();

    public abstract @NotNull String[] getTextures();

    public abstract @NotNull Vector3f[] getColors();

    public short getID()
    {
        return id;
    }
}