package com.thatsoulyguy.invasion2.crafting;

import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class CraftingRecipe
{
    private static short idCounter = 0;

    private final short id;

    public CraftingRecipe()
    {
        id = idCounter++;
    }

    public abstract @NotNull Map<Character, Item> getKeyDefinitions();

    public abstract @NotNull String getRegistryName();

    public abstract char[][] getRecipeGrid();

    public abstract @NotNull Result getResult();

    public short getId()
    {
        return id;
    }

    /**
     * Returns true if the given inputSlots match the recipe's pattern
     * when both grids are trimmed of empty rows/columns.
     *
     * @param recipe     The recipe to match
     * @param inputSlots A 2D array of item IDs
     * @return true if matched, otherwise false
     */
    public static boolean matchesRecipe(@NotNull CraftingRecipe recipe, short[][] inputSlots)
    {
        char[][] recipeGrid = recipe.getRecipeGrid();
        char[][] trimmedRecipe = trimRecipeGrid(recipeGrid);

        short[][] trimmedInput = trimInputSlots(inputSlots);

        if (trimmedRecipe.length == 0 && trimmedInput.length == 0)
            return true;

        if (trimmedRecipe.length != trimmedInput.length || trimmedRecipe[0].length != trimmedInput[0].length)
            return false;

        Map<Character, Item> keyMap = recipe.getKeyDefinitions();

        for (int row = 0; row < trimmedRecipe.length; row++)
        {
            for (int col = 0; col < trimmedRecipe[row].length; col++)
            {
                char c = trimmedRecipe[row][col];
                short inputItemId = trimmedInput[row][col];

                if (c == '#')
                {
                    if (inputItemId != ItemRegistry.ITEM_AIR.getId())
                        return false;
                }
                else
                {
                    Item recipeItem = keyMap.get(c);

                    if (recipeItem == null)
                        return false;

                    if (inputItemId != recipeItem.getId())
                        return false;
                }
            }
        }

        return true;
    }

    private static char[][] trimRecipeGrid(char[][] grid)
    {
        if (grid.length == 0)
            return grid;

        int rows = grid.length;
        int cols = grid[0].length;

        int top = rows, bottom = -1, left = cols, right = -1;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (grid[r][c] != '#')
                {
                    if (r < top)
                        top = r;

                    if (r > bottom)
                        bottom = r;

                    if (c < left)
                        left = c;

                    if (c > right)
                        right = c;
                }
            }
        }

        if (bottom == -1)
            return new char[0][0];

        int trimmedRows = bottom - top + 1;
        int trimmedCols = right - left + 1;

        char[][] result = new char[trimmedRows][trimmedCols];

        for (int r = 0; r < trimmedRows; r++)
            System.arraycopy(grid[top + r], left, result[r], 0, trimmedCols);

        return result;
    }

    private static short[][] trimInputSlots(short[][] input)
    {
        if (input.length == 0)
            return input;

        int rows = input.length;
        int cols = input[0].length;

        int top = rows, bottom = -1, left = cols, right = -1;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (input[r][c] != ItemRegistry.ITEM_AIR.getId())
                {
                    if (r < top)
                        top = r;

                    if (r > bottom)
                        bottom = r;

                    if (c < left)
                        left = c;

                    if (c > right)
                        right = c;
                }
            }
        }

        if (bottom == -1)
            return new short[0][0];

        int trimmedRows = bottom - top + 1;
        int trimmedCols = right - left + 1;

        short[][] result = new short[trimmedRows][trimmedCols];

        for (int r = 0; r < trimmedRows; r++)
            System.arraycopy(input[top + r], left, result[r], 0, trimmedCols);

        return result;
    }

    /**
     * Returns true if the given inputSlots match the recipe's pattern,
     * ignoring exact dimensions/positions. Only the item counts must match.
     * <p>
     * For example, if the recipe requires 2 oak planks (anywhere),
     * then inputSlots must also contain exactly 2 oak planks and nothing else.
     *
     * @param recipe     The recipe to match.
     * @param inputSlots A 2D array of item IDs (e.g., short[][]).
     * @return true if the multiset of items matches exactly, otherwise false.
     */
    public static boolean matchesDimensionAgnostic(@NotNull CraftingRecipe recipe, short[][] inputSlots)
    {
        Map<Short, Integer> recipeItems = collectRecipeItems(recipe);

        Map<Short, Integer> inputItems = collectInputItems(inputSlots);

        return recipeItems.equals(inputItems);
    }

    private static Map<Short, Integer> collectRecipeItems(@NotNull CraftingRecipe recipe)
    {
        Map<Short, Integer> map = new HashMap<>();
        Map<Character, Item> keyMap = recipe.getKeyDefinitions();

        char[][] grid = recipe.getRecipeGrid();

        for (char[] row : grid)
        {
            for (char c : row)
            {
                if (c == '#')
                    continue;

                Item item = keyMap.get(c);

                if (item == null)
                    return Map.of();

                short itemId = item.getId();
                map.put(itemId, map.getOrDefault(itemId, 0) + 1);
            }
        }

        return map;
    }

    private static Map<Short, Integer> collectInputItems(@NotNull short[][] inputSlots) {
        Map<Short, Integer> map = new HashMap<>();

        for (short[] row : inputSlots)
        {
            for (short itemId : row)
            {
                if (itemId == ItemRegistry.ITEM_AIR.getId())
                    continue;

                map.put(itemId, map.getOrDefault(itemId, 0) + 1);
            }
        }

        return map;
    }

    public record Result(@NotNull Item item, byte count) { }
}