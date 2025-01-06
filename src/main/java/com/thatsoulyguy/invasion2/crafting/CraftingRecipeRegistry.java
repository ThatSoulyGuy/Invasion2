package com.thatsoulyguy.invasion2.crafting;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(CraftingRecipe.class)
public class CraftingRecipeRegistry
{
    public static final @NotNull CraftingRecipe OAK_LOG_TO_OAK_PLANKS = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_oak_log_to_oak_planks";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'l', ItemRegistry.ITEM_LOG_OAK_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'l' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_OAK_PLANKS_BLOCK, (byte) 4);
        }
    };

    public static final @NotNull CraftingRecipe OAK_PLANKS_TO_STICK = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_oak_planks_to_stick";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'p', ItemRegistry.ITEM_OAK_PLANKS_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'p' },
                new char[] { 'p' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_STICK, (byte) 4);
        }
    };

    public static final @NotNull CraftingRecipe OAK_PLANKS_TO_CRAFTING_TABLE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_oak_planks_to_crafting_table";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'p', ItemRegistry.ITEM_OAK_PLANKS_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'p', 'p' },
                new char[] { 'p', 'p' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_CRAFTING_TABLE_BLOCK, (byte) 1);
        }
    };

    private static final @NotNull ConcurrentMap<String, CraftingRecipe> recipesByName = new ConcurrentHashMap<>();
    private static final @NotNull ConcurrentMap<Short, CraftingRecipe> recipesById = new ConcurrentHashMap<>();
    
    private CraftingRecipeRegistry() { }

    public static void initialize()
    {
        register(OAK_LOG_TO_OAK_PLANKS);
        register(OAK_PLANKS_TO_STICK);
        register(OAK_PLANKS_TO_CRAFTING_TABLE);
    }

    public static void register(@NotNull CraftingRecipe object)
    {
        recipesByName.putIfAbsent(object.getRegistryName(), object);
        recipesById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        CraftingRecipe CraftingRecipe = recipesByName.getOrDefault(name, null);

        if (CraftingRecipe == null)
            return;

        recipesByName.remove(CraftingRecipe.getRegistryName());
        recipesById.remove(CraftingRecipe.getId());
    }

    public static boolean has(@NotNull String name)
    {
        return recipesByName.containsKey(name);
    }

    public static @Nullable CraftingRecipe get(@NotNull String name)
    {
        return recipesByName.getOrDefault(name, null);
    }

    public static @Nullable CraftingRecipe get(short id)
    {
        return recipesById.getOrDefault(id, null);
    }

    public static @NotNull List<CraftingRecipe> getAll()
    {
        return List.copyOf(recipesByName.values());
    }

    public static void uninitialize() { }
}