package com.thatsoulyguy.invasion2.item;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.block.Block;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Item.class)
public class ItemRegistry
{
    public static final Item ITEM_AIR = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_air";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(0.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_GRASS_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_grass";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Grass Item";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "grass_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_GRASS;
        }
    };

    public static final Item ITEM_DIRT_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_dirt";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Dirt Item";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "dirt_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_DIRT;
        }
    };

    public static final Item ITEM_STONE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_stone";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stone Item";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "stone_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_STONE;
        }
    };

    public static final Item ITEM_LEAVES_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_leaves";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Leaves";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "oak_leaves";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_LEAVES;
        }
    };

    public static final Item ITEM_LOG_OAK_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_log_oak";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Oak Log";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "oak_log";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_LOG_OAK;
        }
    };

    public static final Item ITEM_OAK_PLANKS_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_planks_oak";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Oak Planks";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "oak_planks";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_OAK_PLANKS;
        }
    };

    public static final Item ITEM_STICK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_stick";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stick";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "stick";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_CRAFTING_TABLE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_crafting_table";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Crafting Table";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "crafting_table";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_CRAFTING_TABLE;
        }
    };

    private static final ConcurrentMap<String, Item> itemsByName = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Short, Item> itemsById = new ConcurrentHashMap<>();

    private ItemRegistry() { }

    public static void initialize()
    {
        register(ITEM_AIR);
        register(ITEM_GRASS_BLOCK);
        register(ITEM_DIRT_BLOCK);
        register(ITEM_STONE_BLOCK);
        register(ITEM_LEAVES_BLOCK);
        register(ITEM_LOG_OAK_BLOCK);
        register(ITEM_OAK_PLANKS_BLOCK);
        register(ITEM_STICK);
        register(ITEM_CRAFTING_TABLE_BLOCK);
    }

    public static void register(@NotNull Item object)
    {
        itemsByName.putIfAbsent(object.getRegistryName(), object);
        itemsById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Item item = itemsByName.getOrDefault(name, null);

        if (item == null)
            return;

        itemsByName.remove(item.getRegistryName());
        itemsById.remove(item.getId());
    }

    public static boolean has(@NotNull String name)
    {
        return itemsByName.containsKey(name);
    }

    public static @Nullable Item get(@NotNull String name)
    {
        return itemsByName.getOrDefault(name, null);
    }

    public static @Nullable Item get(short id)
    {
        return itemsById.getOrDefault(id, null);
    }

    public static @NotNull List<Item> getAll()
    {
        return List.copyOf(itemsByName.values());
    }

    public static void uninitialize() { }
}