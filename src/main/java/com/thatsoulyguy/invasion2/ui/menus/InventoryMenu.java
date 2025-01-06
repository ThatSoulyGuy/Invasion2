package com.thatsoulyguy.invasion2.ui.menus;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Settings;
import com.thatsoulyguy.invasion2.crafting.CraftingRecipe;
import com.thatsoulyguy.invasion2.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.item.Inventory;
import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import com.thatsoulyguy.invasion2.render.Texture;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.ui.Menu;
import com.thatsoulyguy.invasion2.ui.UIElement;
import com.thatsoulyguy.invasion2.ui.UIPanel;
import com.thatsoulyguy.invasion2.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.invasion2.ui.uielements.ImageUIElement;
import com.thatsoulyguy.invasion2.ui.uielements.TextUIElement;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import com.thatsoulyguy.invasion2.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InventoryMenu extends Menu
{
    private static final @NotNull Texture TEXTURE_TRANSPARENCY = Objects.requireNonNull(TextureManager.get("ui.transparency"));
    private static final @NotNull Texture TEXTURE_SLOT_DARKEN = Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken"));

    public int currentSlotSelected = 0;

    private @EffectivelyNotNull Inventory inventory;

    private final @NotNull UIElement[] hotbarElements = new UIElement[9];
    private final @NotNull TextUIElement[] hotbarElementTexts = new TextUIElement[9];
    private final @NotNull UIElement[][] slotElements = new UIElement[4][9];
    private final @NotNull TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private final @NotNull Short[][] craftingSlots = new Short[2][2];
    private final @NotNull Byte[][] craftingSlotCounts = new Byte[2][2];
    private final @NotNull UIElement[][] craftingSlotElements = new UIElement[2][2];
    private final @NotNull TextUIElement[][] craftingSlotTexts = new TextUIElement[2][2];

    private @EffectivelyNotNull UIElement craftingResultElement;
    private @EffectivelyNotNull TextUIElement craftingResultText;
    private short craftingResultSlot = 0;
    private byte craftingResultSlotCount = 0;

    private @EffectivelyNotNull UIPanel hud;
    private @EffectivelyNotNull UIPanel survivalMenu;
    private @EffectivelyNotNull UIPanel creativeMenu;
    private @EffectivelyNotNull UIElement hotbarSelector;

    private short grabbedItemId = 0;
    private byte grabbedItemCount = 0;
    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    @Override
    public void initialize()
    {
        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                craftingSlots[x][y] = 0;
                craftingSlotCounts[x][y] = 0;
            }
        }

        hud = UIPanel.create("hud");

        UIElement hotbar = hud.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "hotbar",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(362.0f, 42.0f).mul(Settings.UI_SCALE.getValue())
                )
        );

        hotbar.setTexture(Objects.requireNonNull(TextureManager.get("ui.hotbar")));
        hotbar.setTransparent(true);
        hotbar.setAlignment(UIElement.Alignment.BOTTOM);
        hotbar.setOffset(new Vector2f(0.0f, -8.0f));

        for (int y = 0; y < 9; y++)
        {
            UIElement element = hud.addElement(
                    UIElement.create(
                            ImageUIElement.class,
                            "slot_0_" + y,
                            new Vector2f(0.0f, 0.0f),
                            new Vector2f(28.0f, 28.0f).mul(Settings.UI_SCALE.getValue())
                    )
            );

            element.setTransparent(true);
            element.setTexture(TEXTURE_TRANSPARENCY);
            element.setAlignment(UIElement.Alignment.BOTTOM);
            element.setOffset(new Vector2f(y * (40 * Settings.UI_SCALE.getValue()) - 240, -18.0f));

            hotbarElements[y] = element;
        }

        for (int y = 0; y < 9; y++)
        {
            TextUIElement text = (TextUIElement) hud.addElement(
                    UIElement.create(
                            TextUIElement.class,
                            "slot_text_0_" + y,
                            new Vector2f(0.0f, 0.0f),
                            new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())
                    )
            );

            text.setTransparent(true);
            text.setActive(false);
            text.setText("0");
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
            text.setFontSize(20);
            text.build();
            text.setAlignment(UIElement.Alignment.BOTTOM);
            text.setOffset(new Vector2f(y * (40 * Settings.UI_SCALE.getValue()) - 224.5f, -9.45f));

            hotbarElementTexts[y] = text;
        }

        hotbarSelector = hud.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "hotbar_selector",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(46.0f, 46.0f).mul(Settings.UI_SCALE.getValue())
                )
        );

        hotbarSelector.setTexture(Objects.requireNonNull(TextureManager.get("ui.hotbar_selector")));
        hotbarSelector.setTransparent(true);
        hotbarSelector.setAlignment(UIElement.Alignment.BOTTOM);
        hotbarSelector.setOffset(new Vector2f(0.0f, -5.0f));


        survivalMenu = UIPanel.create("survival_menu");

        UIElement background = survivalMenu.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "background",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(100.0f, 100.0f)
                )
        );

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
        background.setStretch(List.of(
                UIElement.Stretch.LEFT,
                UIElement.Stretch.RIGHT,
                UIElement.Stretch.TOP,
                UIElement.Stretch.BOTTOM
        ));

        UIElement inventory = survivalMenu.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "inventory",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(352.0f, 332.0f)
                                .mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))
                )
        );

        inventory.setTransparent(true);
        inventory.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.survival_inventory")));
        inventory.setOffset(new Vector2f(0.0f, -35.0f));

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                ButtonUIElement button = (ButtonUIElement) survivalMenu.addElement(
                        UIElement.create(
                                ButtonUIElement.class,
                                "survival_slot_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                new Vector2f(32.0f, 32.0f)
                                        .mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))
                        )
                );

                button.setTransparent(true);
                button.setTexture(TEXTURE_TRANSPARENCY);

                setupInventorySlotEvents(button, x, y);

                if (x == 0)
                {
                    button.setOffset(new Vector2f(
                            y * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11))) - 176.5f,
                            129.0f
                    ));
                }
                else
                {
                    button.setOffset(new Vector2f(
                            y * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11))) - 176.5f,
                            119.0f - (x * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11))))
                    ));
                }

                slotElements[x][y] = button;

                TextUIElement text = (TextUIElement) survivalMenu.addElement(
                        UIElement.create(
                                TextUIElement.class,
                                "survival_slot_text_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())
                        )
                );

                text.setTransparent(true);
                text.setActive(false);
                text.setText("");
                text.setAlignment(
                        TextUIElement.TextAlignment.VERTICAL_CENTER,
                        TextUIElement.TextAlignment.HORIZONTAL_CENTER
                );

                text.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
                text.setFontSize(18);
                text.build();

                if (x == 0)
                {
                    text.setOffset(new Vector2f(
                            y * (29.5f * Settings.UI_SCALE.getValue()) - 161.0f,
                            129.0f + 12.45f
                    ));
                }
                else
                {
                    text.setOffset(new Vector2f(
                            y * (29.5f * Settings.UI_SCALE.getValue()) - 161.0f,
                            (119.0f + 12.45f) - (x * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11))))
                    ));
                }

                slotElementTexts[x][y] = text;
            }
        }

        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                ButtonUIElement button = (ButtonUIElement) survivalMenu.addElement(
                        UIElement.create(
                                ButtonUIElement.class,
                                "crafting_slot_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                new Vector2f(32.0f, 32.0f)
                                        .mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))
                        )
                );

                button.setTransparent(true);
                button.setTexture(TEXTURE_TRANSPARENCY);

                setupCraftingSlotEvents(button, x, y);

                button.setOffset(new Vector2f(
                        (y * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11)))) + 20,
                        (x * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11)))) - 155
                ));

                craftingSlotElements[x][y] = button;

                TextUIElement text = (TextUIElement) survivalMenu.addElement(
                        UIElement.create(
                                TextUIElement.class,
                                "crafting_slot_text_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())
                        )
                );

                text.setTransparent(true);
                text.setActive(false);
                text.setText("");
                text.setAlignment(
                        TextUIElement.TextAlignment.VERTICAL_CENTER,
                        TextUIElement.TextAlignment.HORIZONTAL_CENTER
                );

                text.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
                text.setFontSize(18);
                text.build();

                text.setOffset(new Vector2f(
                        y * (29.5f * Settings.UI_SCALE.getValue()) + 36.0f,
                        (x * (36f * (Settings.UI_SCALE.getValue() * ((float) 9 / 11))))
                                - (130.0f + 12.45f)
                ));

                craftingSlotTexts[x][y] = text;
            }
        }

        ButtonUIElement craftingResultButton = (ButtonUIElement) survivalMenu.addElement(
                UIElement.create(
                        ButtonUIElement.class,
                        "crafting_result_slot",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(32.0f, 32.0f)
                                .mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))
                )
        );

        craftingResultButton.setTransparent(true);
        craftingResultButton.setTexture(TEXTURE_TRANSPARENCY);
        setupCraftingResultSlotEvents(craftingResultButton);

        craftingResultButton.setOffset(new Vector2f(156.5f, -130.0f));
        craftingResultElement = craftingResultButton;

        TextUIElement resultText = (TextUIElement) survivalMenu.addElement(
                UIElement.create(
                        TextUIElement.class,
                        "crafting_result_slot_text",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())
                )
        );

        resultText.setTransparent(true);
        resultText.setActive(false);
        resultText.setText("");
        resultText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
        resultText.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
        resultText.setFontSize(18);
        resultText.build();
        resultText.setOffset(new Vector2f(170.5f, -118.0f));

        craftingResultText = resultText;

        grabbedItemElement = survivalMenu.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "selected_item",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(32.0f, 32.0f)
                                .mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))
                )
        );

        grabbedItemElement.setTransparent(true);
        grabbedItemElement.setActive(false);
        grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
        grabbedItemElement.setAlignAndStretch(false);

        grabbedItemText = (TextUIElement) survivalMenu.addElement(
                UIElement.create(
                        TextUIElement.class,
                        "selected_item_text",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(16.0f, 15.5f).mul(Settings.UI_SCALE.getValue())
                )
        );

        grabbedItemText.setTransparent(true);
        grabbedItemText.setActive(false);
        grabbedItemText.setText("");
        grabbedItemText.setAlignment(
                TextUIElement.TextAlignment.VERTICAL_CENTER,
                TextUIElement.TextAlignment.HORIZONTAL_CENTER
        );

        grabbedItemText.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
        grabbedItemText.setFontSize(20);
        grabbedItemText.build();
        grabbedItemText.setAlignAndStretch(false);

        survivalMenu.setActive(false);


        creativeMenu = UIPanel.create("creative_menu");
        creativeMenu.setActive(false);
    }

    @Override
    public void update()
    {
        if (currentSlotSelected > 8)
            currentSlotSelected = 0;

        if (currentSlotSelected < 0)
            currentSlotSelected = 8;

        if (grabbedItemText.isActive())
            grabbedItemText.setPosition(InputManager.getMousePosition().add(new Vector2f(7.5f, 7.5f)));

        if (grabbedItemElement.isActive())
            grabbedItemElement.setPosition(InputManager.getMousePosition().sub(new Vector2f(16.0f, 16.0f)));

        List<CraftingRecipe> craftingRecipes = CraftingRecipeRegistry.getAll();

        boolean wasMatch = false;
        for (CraftingRecipe recipe : craftingRecipes)
        {
            boolean match = CraftingRecipe.matchesRecipe(
                    recipe,
                    Arrays.stream(craftingSlots)
                            .map(subArray ->
                            {
                                short[] primitiveSubArray = new short[subArray.length];

                                for (int i = 0; i < subArray.length; i++)
                                    primitiveSubArray[i] = subArray[i];

                                return primitiveSubArray;
                            }).toArray(short[][]::new)
            );

            if (match)
            {
                short oldCraftingResultSlot = craftingResultSlot;
                byte oldCraftingResultSlotCount = craftingResultSlotCount;

                craftingResultSlot = recipe.getResult().item().getId();
                craftingResultSlotCount = recipe.getResult().count();

                if (oldCraftingResultSlot != craftingResultSlot || craftingResultSlotCount != oldCraftingResultSlotCount)
                    build();

                wasMatch = true;
                break;
            }
        }

        if (!wasMatch)
        {
            byte oldCraftingResultSlotCount = craftingResultSlotCount;
            craftingResultSlotCount = 0;

            if (craftingResultSlotCount != oldCraftingResultSlotCount)
                build();
        }

        hotbarSelector.setOffset(
                new Vector2f(
                        (currentSlotSelected * (40 * Settings.UI_SCALE.getValue())) - 240,
                        -5.0f
                )
        );
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_inventory";
    }

    public void setInventory(@NotNull Inventory inventory)
    {
        this.inventory = inventory;
    }

    public void setSurvivalMenuActive(boolean active)
    {
        survivalMenu.setActive(active);
    }

    public boolean getSurvivalMenuActive()
    {
        return survivalMenu.isActive();
    }

    public void setCreativeMenu(boolean active)
    {
        creativeMenu.setActive(active);
    }

    public boolean getCreativeMenuActive()
    {
        return creativeMenu.isActive();
    }

    void build()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    if (x == 0)
                    {
                        hotbarElements[y].setTexture(TEXTURE_TRANSPARENCY);
                        hotbarElementTexts[y].setActive(false);
                    }

                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();
                    continue;
                }

                Item item = ItemRegistry.get(inventory.slots[x][y]);

                if (item == null)
                {
                    System.err.println("Invalid item detected in menu!");
                    continue;
                }

                if (inventory.slotCounts[x][y] <= 0)
                {
                    inventory.slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                    inventory.slotCounts[x][y] = 0;

                    if (x == 0)
                    {
                        hotbarElements[y].setTexture(TEXTURE_TRANSPARENCY);
                        hotbarElementTexts[y].setActive(false);
                    }

                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();

                    continue;
                }

                if (inventory.slotCounts[x][y] == 1)
                {
                    if (x == 0)
                        hotbarElementTexts[y].setActive(false);

                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();
                }

                TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));

                slotElements[x][y].setTexture(atlas.getOutputTexture());

                Vector2f[] uvs = atlas.getSubTextureCoordinates(item.getTexture(), 90);

                if (uvs == null)
                {
                    System.err.println("Invalid UVs detected in menu!");
                    continue;
                }

                slotElements[x][y].setUVs(uvs);

                if (x == 0)
                {
                    hotbarElements[y].setTexture(atlas.getOutputTexture());
                    hotbarElements[y].setUVs(uvs);
                }

                if (inventory.slotCounts[x][y] > 1)
                {
                    if (x == 0)
                    {
                        hotbarElementTexts[y].setActive(true);
                        hotbarElementTexts[y].setText(String.valueOf(inventory.slotCounts[x][y]));
                        hotbarElementTexts[y].build();
                    }

                    slotElementTexts[x][y].setText(String.valueOf(inventory.slotCounts[x][y]));
                    slotElementTexts[x][y].build();
                }
            }
        }

        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                if (craftingSlots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    craftingSlotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();

                    continue;
                }

                Item item = ItemRegistry.get(craftingSlots[x][y]);

                if (item == null)
                {
                    System.err.println("Invalid item detected in menu!");
                    continue;
                }

                if (craftingSlotCounts[x][y] <= 0)
                {
                    craftingSlots[x][y] = ItemRegistry.ITEM_AIR.getId();
                    craftingSlotCounts[x][y] = 0;
                    craftingSlotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();

                    continue;
                }

                if (craftingSlotCounts[x][y] == 1)
                {
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();
                }

                TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));

                craftingSlotElements[x][y].setTexture(atlas.getOutputTexture());

                Vector2f[] uvs = atlas.getSubTextureCoordinates(item.getTexture(), 90);

                if (uvs == null)
                {
                    System.err.println("Invalid UVs detected in menu!");
                    continue;
                }

                craftingSlotElements[x][y].setUVs(uvs);

                if (craftingSlotCounts[x][y] > 1)
                {
                    craftingSlotTexts[x][y].setText(String.valueOf(craftingSlotCounts[x][y]));
                    craftingSlotTexts[x][y].build();
                }
            }
        }

        if (craftingResultSlot == ItemRegistry.ITEM_AIR.getId())
            return;

        Item resultItem = ItemRegistry.get(craftingResultSlot);

        if (resultItem == null)
        {
            System.err.println("Invalid item detected in menu!");
            return;
        }

        if (craftingResultSlotCount <= 0)
        {
            craftingResultSlot = ItemRegistry.ITEM_AIR.getId();
            craftingResultSlotCount = 0;

            craftingResultElement.setTexture(TEXTURE_TRANSPARENCY);
            craftingResultText.setText("");
            craftingResultText.build();

            return;
        }
        if (craftingResultSlotCount == 1)
        {
            craftingResultText.setText("");
            craftingResultText.build();
        }

        TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));

        craftingResultElement.setTexture(atlas.getOutputTexture());

        Vector2f[] uvs = atlas.getSubTextureCoordinates(resultItem.getTexture(), 90);

        if (uvs == null)
        {
            System.err.println("Invalid uvs detected in menu!");
            return;
        }

        craftingResultElement.setUVs(uvs);

        if (craftingResultSlotCount > 1)
        {
            craftingResultText.setText(String.valueOf(craftingResultSlotCount));
            craftingResultText.build();
        }
    }

    public void addItem(short item, byte count)
    {
        for (int x = 0; x < 1; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (inventory.slots[x][y] == item && inventory.slotCounts[x][y] <= 63)
                {
                    inventory.slotCounts[x][y] = (byte) (inventory.slotCounts[x][y] + count);
                    build();

                    return;
                }

                else if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    inventory.slots[x][y] = item;
                    inventory.slotCounts[x][y] = count;
                    build();

                    return;
                }
            }
        }
    }

    public void setSlot(@NotNull Vector2i position, short item, byte count)
    {
        if (!isPositionValid(position))
            return;

        if (inventory.slots[position.x][position.y] == item && inventory.slotCounts[position.x][position.y] == count)
            return;

        inventory.slots[position.x][position.y] = item;
        inventory.slotCounts[position.x][position.y] = count;

        build();
    }

    public @Nullable SlotData getSlot(@NotNull Vector2i position)
    {
        if (!isPositionValid(position))
            return null;

        return new SlotData(inventory.slots[position.x][position.y], inventory.slotCounts[position.x][position.y]);
    }

    private boolean isPositionValid(@NotNull Vector2i position)
    {
        return position.x >= 0 && position.x < inventory.slots.length && position.y >= 0 && position.y < inventory.slots[0].length;
    }

    private void setupInventorySlotEvents(@NotNull ButtonUIElement button, int x, int y)
    {
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(
                x, y, inventory.slots, inventory.slotCounts, slotElements, button, false
        ));

        button.addOnRightClickedEvent(() -> handleSlotRightClick(
                x, y, inventory.slots, inventory.slotCounts, slotElements, button, false
        ));

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupCraftingSlotEvents(@NotNull ButtonUIElement button, int x, int y)
    {
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(
                x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, true
        ));

        button.addOnRightClickedEvent(() -> handleSlotRightClick(
                x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, true
        ));

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupCraftingResultSlotEvents(@NotNull ButtonUIElement button)
    {
        button.addOnLeftClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (craftingResultSlot != ItemRegistry.ITEM_AIR.getId())
            {
                grabbedItemId = craftingResultSlot;
                grabbedItemElement.setTexture(craftingResultElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(craftingResultSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                if (grabbedItemCount > 0)
                    grabbedItemCount += craftingResultSlotCount;
                else
                    grabbedItemCount = craftingResultSlotCount;

                updateGrabbedItemText();

                craftingSlotCounts[0][0]--;
                craftingSlotCounts[1][0]--;
                craftingSlotCounts[0][1]--;
                craftingSlotCounts[1][1]--;

                craftingResultSlotCount = 0;

                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void handleSlotLeftClick(int x, int y, @NotNull Short[][] itemArr, @NotNull Byte[][] countArr, @NotNull UIElement[][] uiArr, @NotNull UIElement button, boolean crafting)
    {
        if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
            return;

        if (itemArr[x][y] != ItemRegistry.ITEM_AIR.getId() && grabbedItemId == ItemRegistry.ITEM_AIR.getId())
        {
            grabbedItemId = itemArr[x][y];
            grabbedItemElement.setTexture(uiArr[x][y].getTexture());
            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
            grabbedItemElement.setPosition(InputManager.getMousePosition());
            grabbedItemElement.setActive(true);

            if (countArr[x][y] > 1)
            {
                grabbedItemCount = countArr[x][y];
                updateGrabbedItemText();
            }
            else
            {
                grabbedItemCount = countArr[x][y];
                updateGrabbedItemText();
            }

            countArr[x][y] = 0;
            build();

            return;
        }

        if (itemArr[x][y] == ItemRegistry.ITEM_AIR.getId() && grabbedItemElement.isActive())
        {
            grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
            grabbedItemElement.setActive(false);

            countArr[x][y] = grabbedItemCount;

            grabbedItemCount = 0;
            updateGrabbedItemText();

            itemArr[x][y] = grabbedItemId;
            grabbedItemId = ItemRegistry.ITEM_AIR.getId();

            build();

            return;
        }

        if (itemArr[x][y] != grabbedItemId && grabbedItemElement.isActive())
        {
            short oldGrabbedId = grabbedItemId;
            byte oldGrabbedCount = grabbedItemCount;

            grabbedItemId = itemArr[x][y];
            grabbedItemCount = countArr[x][y];

            grabbedItemElement.setTexture(uiArr[x][y].getTexture());
            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
            grabbedItemElement.setPosition(InputManager.getMousePosition());
            grabbedItemElement.setActive(true);

            updateGrabbedItemText();

            itemArr[x][y] = oldGrabbedId;
            countArr[x][y] = oldGrabbedCount;

            build();
            return;
        }

        if (itemArr[x][y] == grabbedItemId)
        {
            grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
            grabbedItemElement.setActive(false);

            countArr[x][y] = (byte) (countArr[x][y] + grabbedItemCount);

            grabbedItemId = ItemRegistry.ITEM_AIR.getId();
            grabbedItemCount = 0;
            updateGrabbedItemText();

            build();
        }
    }

    private void handleSlotRightClick(int x, int y, @NotNull Short[][] itemArr, @NotNull Byte[][] countArr, @NotNull UIElement[][] uiArr, @NotNull UIElement button, boolean crafting)
    {
        if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
            return;

        if (grabbedItemCount > 0 && (itemArr[x][y] == ItemRegistry.ITEM_AIR.getId() || itemArr[x][y] == grabbedItemId))
        {
            itemArr[x][y] = grabbedItemId;
            countArr[x][y] = (byte) (countArr[x][y] + 1);

            grabbedItemCount--;
            updateGrabbedItemText();

            if (grabbedItemCount <= 0)
            {
                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemElement.setActive(false);
            }

            build();
        }
    }

    private void handleSlotHoverBegin(@NotNull UIElement button)
    {
        if (button.getTexture() == TEXTURE_TRANSPARENCY)
            button.setTexture(TEXTURE_SLOT_DARKEN);
        else
            button.setColor(new Vector3f(0.82f));
    }

    private void handleSlotHoverEnd(@NotNull UIElement button)
    {
        if (button.getTexture() == TEXTURE_SLOT_DARKEN)
            button.setTexture(TEXTURE_TRANSPARENCY);
        else
            button.setColor(new Vector3f(1.0f));
    }

    private void updateGrabbedItemText()
    {
        if (grabbedItemCount > 1)
        {
            grabbedItemText.setText(String.valueOf(grabbedItemCount));
            grabbedItemText.build();
            grabbedItemText.setActive(true);
        }
        else
        {
            grabbedItemText.setText("");
            grabbedItemText.build();
            grabbedItemText.setActive(false);
        }
    }

    public record SlotData(short id, byte count) implements Serializable { }
}