package com.thatsoulyguy.invasion2.block;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Block.class)
public class BlockRegistry
{
    public static final Block BLOCK_AIR = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_air";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "";
        }

        @Override
        public float getHardness()
        {
            return 0.0f;
        }

        @Override
        public float getResistance()
        {
            return 0;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {

            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
            };
        }
    };

    public static final Block BLOCK_GRASS = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_grass";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Grass Block";
        }

        @Override
        public float getHardness()
        {
            return 0.65f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "grass_top",
                "dirt",
                "grass_side",
                "grass_side",
                "grass_side",
                "grass_side"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }
    };

    public static final Block BLOCK_DIRT = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_dirt";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Dirt Block";
        }

        @Override
        public float getHardness()
        {
            return 0.65f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "dirt",
                "dirt",
                "dirt",
                "dirt",
                "dirt",
                "dirt"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }
    };

    public static final Block BLOCK_STONE = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_stone";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stone Block";
        }

        @Override
        public float getHardness()
        {
            return 1.25f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "stone",
                "stone",
                "stone",
                "stone",
                "stone",
                "stone"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }
    };

    public static final Block BLOCK_LEAVES = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_leaves";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Leaves";
        }

        @Override
        public float getHardness()
        {
            return 0.14f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "leaves_oak",
                "leaves_oak",
                "leaves_oak",
                "leaves_oak",
                "leaves_oak",
                "leaves_oak"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(0.27f, 0.68f, 0.18f),
                new Vector3f(0.27f, 0.68f, 0.18f),
            };
        }
    };

    public static final Block BLOCK_LOG_OAK = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_log_oak";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Oak Log";
        }

        @Override
        public float getHardness()
        {
            return 0.14f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "log_oak_top",
                "log_oak_top",
                "log_oak",
                "log_oak",
                "log_oak",
                "log_oak"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }
    };

    private static final ConcurrentMap<String, Block> blocksByName = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Short, Block> blocksById = new ConcurrentHashMap<>();

    private BlockRegistry() { }

    public static void initialize()
    {
        register(BLOCK_AIR);
        register(BLOCK_GRASS);
        register(BLOCK_DIRT);
        register(BLOCK_STONE);
        register(BLOCK_LEAVES);
        register(BLOCK_LOG_OAK);
    }

    public static void register(@NotNull Block object)
    {
        blocksByName.putIfAbsent(object.getRegistryName(), object);
        blocksById.putIfAbsent(object.getID(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Block block = blocksByName.getOrDefault(name, null);

        if (block == null)
            return;

        blocksByName.remove(block.getRegistryName());
        blocksById.remove(block.getID());
    }

    public static boolean has(@NotNull String name)
    {
        return blocksByName.containsKey(name);
    }

    public static @Nullable Block get(@NotNull String name)
    {
        return blocksByName.getOrDefault(name, null);
    }

    public static @Nullable Block get(short id)
    {
        return blocksById.getOrDefault(id, null);
    }

    public static @NotNull List<Block> getAll()
    {
        return List.copyOf(blocksByName.values());
    }

    public static void uninitialize() { }
}