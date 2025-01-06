package com.thatsoulyguy.invasion2.ui.menus;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Settings;
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
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class CraftingTableMenu extends Menu
{
    private static final @NotNull Texture TEXTURE_TRANSPARENCY = Objects.requireNonNull(TextureManager.get("ui.transparency"));
    private static final @NotNull Texture TEXTURE_SLOT_DARKEN = Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken"));

    private @EffectivelyNotNull Inventory inventory;
    private @EffectivelyNotNull InventoryMenu inventoryMenu;

    private final @NotNull UIElement[][] slotElements = new UIElement[4][9];
    private final @NotNull TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private final @NotNull Short[][] craftingSlots = new Short[3][3];
    private final @NotNull Byte[][] craftingSlotCounts = new Byte[3][3];
    private final @NotNull UIElement[][] craftingSlotElements = new UIElement[3][3];
    private final @NotNull TextUIElement[][] craftingSlotTexts = new TextUIElement[3][3];

    private short grabbedItemId = 0;
    private byte grabbedItemCount = 0;
    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    private @EffectivelyNotNull UIPanel menu;

    @Override
    public void initialize()
    {
        menu = UIPanel.create("crafting_table_menu");

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                craftingSlots[x][y] = ItemRegistry.ITEM_AIR.getId();
                craftingSlotCounts[x][y] = 0;
            }
        }

        UIElement background = menu.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f)));

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
        background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));

        UIElement inventory = menu.addElement(UIElement.create(ImageUIElement.class, "inventory", new Vector2f(0.0f, 0.0f), new Vector2f(352.0f, 332.0f).mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))));

        inventory.setTransparent(true);
        inventory.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.crafting_table")));
        inventory.setOffset(new Vector2f(0.0f, -35.0f));

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                ButtonUIElement button = (ButtonUIElement) menu.addElement(
                        UIElement.create(
                                ButtonUIElement.class,
                                "slot_" + x + "_" + y,
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

                TextUIElement text = (TextUIElement) menu.addElement(
                        UIElement.create(
                                TextUIElement.class,
                                "slot_text_" + x + "_" + y,
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

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                ButtonUIElement button = (ButtonUIElement) menu.addElement(UIElement.create(ButtonUIElement.class, "crafting_slot_" + x + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(32.0f, 32.0f).mul(Settings.UI_SCALE.getValue() * ((float) 9 / 11))));

                button.setTransparent(true);
                button.setTexture(TEXTURE_TRANSPARENCY);

                setupCraftingSlotEvents(button, x, y);

                button.setOffset(new Vector2f(((36 * Settings.UI_SCALE.getValue() * ((float) 9 / 11)) * x) - 123, ((36 * Settings.UI_SCALE.getValue() * ((float) 9 / 11)) * y) - 178));

                craftingSlotElements[x][y] = button;

                TextUIElement text = (TextUIElement) menu.addElement(UIElement.create(TextUIElement.class, "crafting_slot_text_" + x + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())));

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

                text.setOffset(new Vector2f(((36 * Settings.UI_SCALE.getValue() * ((float) 9 / 11)) * x) - 123, ((36 * Settings.UI_SCALE.getValue() * ((float) 9 / 11)) * y) - 178));

                craftingSlotTexts[x][y] = text;
            }
        }

        grabbedItemElement = menu.addElement(
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

        grabbedItemText = (TextUIElement) menu.addElement(
                UIElement.create(
                        TextUIElement.class,
                        "selected_item_text",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())
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
        grabbedItemText.setFontSize(18);
        grabbedItemText.build();
        grabbedItemText.setAlignAndStretch(false);
    }

    @Override
    public void update()
    {
        if (grabbedItemText.isActive())
            grabbedItemText.setPosition(InputManager.getMousePosition().add(new Vector2f(7.5f, 7.5f)));

        if (grabbedItemElement.isActive())
            grabbedItemElement.setPosition(InputManager.getMousePosition().sub(new Vector2f(16.0f, 16.0f)));
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_crafting_table";
    }

    public void build()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
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

                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();

                    continue;
                }

                if (inventory.slotCounts[x][y] == 1)
                {
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

                if (inventory.slotCounts[x][y] > 1)
                {
                    slotElementTexts[x][y].setText(String.valueOf(inventory.slotCounts[x][y]));
                    slotElementTexts[x][y].build();
                }
            }
        }

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
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

        inventoryMenu.build();
    }

    public void setInventory(@NotNull Inventory inventory)
    {
        this.inventory = inventory;
    }

    public void setInventoryMenu(@NotNull InventoryMenu inventoryMenu)
    {
        this.inventoryMenu = inventoryMenu;
    }

    public void setActive(boolean active)
    {
        menu.setActive(active);
    }

    public boolean isActive()
    {
        return menu.isActive();
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
                x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, false
        ));

        button.addOnRightClickedEvent(() -> handleSlotRightClick(
                x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, false
        ));

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
}