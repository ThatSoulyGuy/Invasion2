package com.thatsoulyguy.invasion2.item;

import com.thatsoulyguy.invasion2.block.Block;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class Item
{
    private static short idCounter = 0;

    private final short id;

    public Item()
    {
        id = idCounter++;
    }

    public abstract @NotNull String getDisplayName();

    public abstract @NotNull String getRegistryName();

    public abstract @NotNull String getTexture();

    public abstract @NotNull Vector3f getColor();

    public abstract boolean isBlockItem();

    public @NotNull Block getAssociatedBlock()
    {
        return BlockRegistry.BLOCK_AIR;
    }

    public short getId()
    {
        return id;
    }
}