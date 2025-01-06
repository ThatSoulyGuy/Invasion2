package com.thatsoulyguy.invasion2.item;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Inventory implements Serializable
{
    public final @NotNull Short[][] slots = new Short[4][9];
    public final @NotNull Byte[][] slotCounts = new Byte[4][9];

    public Inventory()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                slotCounts[x][y] = 0;
            }
        }
    }
}