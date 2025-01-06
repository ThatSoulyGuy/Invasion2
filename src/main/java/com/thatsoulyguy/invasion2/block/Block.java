package com.thatsoulyguy.invasion2.block;

import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import com.thatsoulyguy.invasion2.item.Tool;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

public abstract class Block
{
    private static short idCounter = 0;

    private final short id;

    public Block()
    {
        id = idCounter++;
    }

    public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition) { }

    public abstract @NotNull String getDisplayName();

    public abstract @NotNull String getRegistryName();

    public abstract float getHardness();

    public abstract float getResistance();

    public abstract @NotNull String[] getTextures();

    public abstract @NotNull Vector3f[] getColors();

    public abstract boolean isInteractable();

    public @NotNull Item getAssociatedItem()
    {
        return ItemRegistry.ITEM_AIR;
    }

    public @NotNull Tool toolRequired()
    {
        return Tool.NONE;
    }

    public short getId()
    {
        return id;
    }
}