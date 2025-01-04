package com.thatsoulyguy.invasion2.ui.menus;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Settings;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.ui.Menu;
import com.thatsoulyguy.invasion2.ui.UIElement;
import com.thatsoulyguy.invasion2.ui.UIPanel;
import com.thatsoulyguy.invasion2.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.invasion2.ui.uielements.ImageUIElement;
import com.thatsoulyguy.invasion2.ui.uielements.TextUIElement;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class InventoryMenu extends Menu
{
    public int currentSlotSelected = 0;

    private final Short[][] slots = new Short[4][9];
    private final Byte[][] slotCounts = new Byte[4][9];
    private final UIElement[] hotbarElements = new UIElement[9];
    private final TextUIElement[] hotbarElementTexts = new TextUIElement[9];
    private final UIElement[][] slotElements = new UIElement[4][9];
    private final TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private @EffectivelyNotNull UIPanel hud;
    private @EffectivelyNotNull UIPanel survivalMenu;
    private @EffectivelyNotNull UIPanel creativeMenu;
    private @EffectivelyNotNull UIElement hotbarSelector;

    private short grabbedItemId = 0;

    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    @Override
    public void initialize()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                slots[x][y] = 0;
                slotCounts[x][y] = 0;
            }
        }

        hud = UIPanel.create("hud");

        UIElement hotbar = hud.addElement(UIElement.create(ImageUIElement.class, "hotbar", new Vector2f(0.0f, 0.0f), new Vector2f(362.0f, 42.0f).mul(Settings.UI_SCALE.getValue())));

        hotbar.setTexture(Objects.requireNonNull(TextureManager.get("ui.hotbar")));
        hotbar.setTransparent(true);

        hotbar.setAlignment(UIElement.Alignment.BOTTOM);
        hotbar.setOffset(new Vector2f(0.0f, -8.0f));

        for (int y = 0; y < 9; y++)
        {
            UIElement element = hud.addElement(UIElement.create(ImageUIElement.class, "slot_" + 0 + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(28.0f, 28.0f).mul(Settings.UI_SCALE.getValue())));

            element.setTransparent(true);
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));

            element.setAlignment(UIElement.Alignment.BOTTOM);
            element.setOffset(new Vector2f(y * (40 * Settings.UI_SCALE.getValue()) - 240, -18.0f));

            hotbarElements[y] = element;
        }

        for (int y = 0; y < 9; y++)
        {
            TextUIElement text = (TextUIElement) hud.addElement(UIElement.create(TextUIElement.class, "slot_text_" + 0 + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())));

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

        hotbarSelector = hud.addElement(UIElement.create(ImageUIElement.class, "hotbar_selector", new Vector2f(0.0f, 0.0f), new Vector2f(46.0f, 46.0f).mul(Settings.UI_SCALE.getValue())));

        hotbarSelector.setTexture(Objects.requireNonNull(TextureManager.get("ui.hotbar_selector")));
        hotbarSelector.setTransparent(true);

        hotbarSelector.setAlignment(UIElement.Alignment.BOTTOM);
        hotbarSelector.setOffset(new Vector2f(0.0f, -5.0f));


        survivalMenu = UIPanel.create("survival_menu");

        {
            UIElement background = survivalMenu.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f)));

            background.setTransparent(true);
            background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
            background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));


            UIElement inventory = survivalMenu.addElement(UIElement.create(ImageUIElement.class, "inventory", new Vector2f(0.0f, 0.0f), new Vector2f(352.0f, 332.0f).mul(Settings.UI_SCALE.getValue() * ((float) 9/11))));

            inventory.setTransparent(true);
            inventory.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.survival_inventory")));
            inventory.setOffset(new Vector2f(0.0f, -35.0f));

            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 9; y++)
                {
                    ButtonUIElement button = (ButtonUIElement) survivalMenu.addElement(UIElement.create(ButtonUIElement.class, "survival_slot_" + x + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(32.0f, 32.0f).mul(Settings.UI_SCALE.getValue() * ((float) 9/11))));

                    button.setTransparent(true);
                    button.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));

                    int finalX = x;
                    int finalY = y;

                    button.addOnClickedEvent(() ->
                    {
                        if (slots[finalX][finalY] != ItemRegistry.ITEM_AIR.getId() && grabbedItemId == ItemRegistry.ITEM_AIR.getId())
                        {
                            grabbedItemId = slots[finalX][finalY];

                            grabbedItemElement.setTexture(slotElements[finalX][finalY].getTexture());
                            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(slots[finalX][finalY])).getTexture(), 90));
                            grabbedItemElement.setPosition(InputManager.getMousePosition());

                            grabbedItemElement.setActive(true);

                            if (slotCounts[finalX][finalY] > 1)
                            {
                                grabbedItemText.setText(String.valueOf(slotCounts[finalX][finalY]));
                                grabbedItemText.build();

                                grabbedItemText.setActive(true);
                            }

                            slotCounts[finalX][finalY] = 0;

                            build();
                        }
                        else if (slots[finalX][finalY] == ItemRegistry.ITEM_AIR.getId() && grabbedItemElement.isActive())
                        {
                            grabbedItemElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                            grabbedItemElement.setActive(false);

                            slotCounts[finalX][finalY] = Byte.valueOf(grabbedItemText.getText());

                            grabbedItemText.setText("");
                            grabbedItemText.build();

                            grabbedItemText.setActive(false);

                            slots[finalX][finalY] = grabbedItemId;

                            grabbedItemId = ItemRegistry.ITEM_AIR.getId();

                            build();
                        }
                        else if (slots[finalX][finalY] != grabbedItemId && grabbedItemElement.isActive())
                        {
                            short oldGrabbedItemId = grabbedItemId;
                            grabbedItemId = slots[finalX][finalY];

                            byte oldGrabbedItemCount = Byte.parseByte(grabbedItemText.getText());
                            grabbedItemText.setText(String.valueOf(slotCounts[finalX][finalY]));

                            grabbedItemElement.setTexture(slotElements[finalX][finalY].getTexture());
                            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(slots[finalX][finalY])).getTexture(), 90));
                            grabbedItemElement.setPosition(InputManager.getMousePosition());

                            grabbedItemElement.setActive(true);

                            if (slotCounts[finalX][finalY] > 1)
                            {
                                grabbedItemText.build();

                                grabbedItemText.setActive(true);
                            }

                            slots[finalX][finalY] = oldGrabbedItemId;
                            slotCounts[finalX][finalY] = oldGrabbedItemCount;

                            build();
                        }
                        else if (slots[finalX][finalY] == grabbedItemId)
                        {
                            grabbedItemElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                            grabbedItemElement.setActive(false);

                            slotCounts[finalX][finalY] = (byte) (slotCounts[finalX][finalY] + Byte.parseByte(grabbedItemText.getText()));

                            grabbedItemText.setText("");
                            grabbedItemText.build();

                            grabbedItemText.setActive(false);

                            grabbedItemId = ItemRegistry.ITEM_AIR.getId();

                            build();
                        }
                    });

                    button.addOnHoveringBeginEvent(() ->
                    {
                        if (button.getTexture() == Objects.requireNonNull(TextureManager.get("ui.transparency")))
                            button.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken")));
                        else
                            button.setColor(new Vector3f(0.82f));
                    });

                    button.addOnHoveringEndEvent(() ->
                    {
                        if (button.getTexture() == Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken")))
                            button.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                        else
                            button.setColor(new Vector3f(1.0f));
                    });

                    if (x == 0)
                        button.setOffset(new Vector2f(y * (36f * (Settings.UI_SCALE.getValue() * ((float) 9/11))) - 176.5f, 129.0f));
                    else
                        button.setOffset(new Vector2f(y * (36f * (Settings.UI_SCALE.getValue() * ((float) 9/11))) - 176.5f, 119.0f - (x  * (36f * (Settings.UI_SCALE.getValue() * ((float) 9/11))))));
                    
                    slotElements[x][y] = button;


                    TextUIElement text = (TextUIElement) survivalMenu.addElement(UIElement.create(TextUIElement.class, "survival_slot_text_" + x + "_" + y, new Vector2f(0.0f, 0.0f), new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())));

                    text.setTransparent(true);

                    text.setActive(false);

                    text.setText("");
                    text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
                    text.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
                    text.setFontSize(18);

                    text.build();

                    if (x == 0)
                        text.setOffset(new Vector2f(y * (29.5f * Settings.UI_SCALE.getValue()) - 161.0f, 129.0f + 12.45f));
                    else
                        text.setOffset(new Vector2f(y * (29.5f * Settings.UI_SCALE.getValue()) - 161.0f, (119.0f + 12.45f) - (x  * (36f * (Settings.UI_SCALE.getValue() * ((float) 9/11))))));

                    slotElementTexts[x][y] = text;
                }
            }

            grabbedItemElement = survivalMenu.addElement(UIElement.create(ImageUIElement.class, "selected_item", new Vector2f(0.0f, 0.0f), new Vector2f(32.0f, 32.0f).mul(Settings.UI_SCALE.getValue() * ((float) 9/11))));

            grabbedItemElement.setTransparent(true);

            grabbedItemElement.setActive(false);

            grabbedItemElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));

            grabbedItemElement.setAlignAndStretch(false);

            grabbedItemText = (TextUIElement) survivalMenu.addElement(UIElement.create(TextUIElement.class, "selected_item_text", new Vector2f(0.0f, 0.0f), new Vector2f(18.0f, 18.0f).mul(Settings.UI_SCALE.getValue())));

            grabbedItemText.setTransparent(true);

            grabbedItemText.setActive(false);

            grabbedItemText.setText("");
            grabbedItemText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
            grabbedItemText.setFontPath(AssetPath.create("invasion2", "font/Invasion2-Default.ttf"));
            grabbedItemText.setFontSize(18);

            grabbedItemText.build();

            grabbedItemText.setAlignAndStretch(false);
        }

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

        hotbarSelector.setOffset(new Vector2f((currentSlotSelected * (40 * Settings.UI_SCALE.getValue())) - 240, -5.0f));
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

    private void build()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                    continue;

                if (x == 0)
                {
                    Item item = ItemRegistry.get(slots[x][y]);

                    if (item == null)
                    {
                        System.err.println("Invalid item detected in menu!");
                        continue;
                    }

                    if (slotCounts[x][y] <= 0)
                    {
                        slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                        slotCounts[x][y] = 0;

                        hotbarElements[y].setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                        hotbarElementTexts[y].setActive(false);

                        slotElements[x][y].setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                        slotElementTexts[x][y].setText("");
                        slotElementTexts[x][y].build();
                        slotElementTexts[x][y].setActive(false);

                        continue;
                    }
                    else if (slotCounts[x][y] == 1)
                    {
                        hotbarElementTexts[y].setActive(false);

                        slotElementTexts[x][y].setText("");
                        slotElementTexts[x][y].build();
                        slotElementTexts[x][y].setActive(false);
                    }

                    hotbarElements[y].setTexture(Objects.requireNonNull(Objects.requireNonNull(TextureAtlasManager.get("items")).getOutputTexture()));
                    slotElements[x][y].setTexture(Objects.requireNonNull(Objects.requireNonNull(TextureAtlasManager.get("items")).getOutputTexture()));

                    Vector2f[] uvs = Objects.requireNonNull(TextureAtlasManager.get("items")).getSubTextureCoordinates(item.getTexture(), 90);

                    if (uvs == null)
                    {
                        System.err.println("Invalid uvs detected in menu!");
                        continue;
                    }

                    hotbarElements[y].setUVs(uvs);
                    slotElements[x][y].setUVs(uvs);

                    if (slotCounts[x][y] > 1)
                    {
                        hotbarElementTexts[y].setActive(true);

                        hotbarElementTexts[y].setText(String.valueOf(slotCounts[x][y]));
                        hotbarElementTexts[y].build();

                        slotElementTexts[x][y].setText(String.valueOf(slotCounts[x][y]));
                        slotElementTexts[x][y].build();
                    }
                }
                else
                {
                    Item item = ItemRegistry.get(slots[x][y]);

                    if (item == null)
                    {
                        System.err.println("Invalid item detected in menu!");
                        continue;
                    }

                    if (slotCounts[x][y] <= 0)
                    {
                        slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                        slotCounts[x][y] = 0;

                        slotElements[x][y].setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));

                        slotElementTexts[x][y].setText("");
                        slotElementTexts[x][y].build();
                        slotElementTexts[x][y].setActive(false);

                        continue;
                    }
                    else if (slotCounts[x][y] == 1)
                    {
                        slotElementTexts[x][y].setText("");
                        slotElementTexts[x][y].build();
                        slotElementTexts[x][y].setActive(false);
                    }

                    slotElements[x][y].setTexture(Objects.requireNonNull(Objects.requireNonNull(TextureAtlasManager.get("items")).getOutputTexture()));

                    Vector2f[] uvs = Objects.requireNonNull(TextureAtlasManager.get("items")).getSubTextureCoordinates(item.getTexture(), 90);

                    if (uvs == null)
                    {
                        System.err.println("Invalid uvs detected in menu!");
                        continue;
                    }

                    slotElements[x][y].setUVs(uvs);

                    if (slotCounts[x][y] > 1)
                    {
                        slotElementTexts[x][y].setText(String.valueOf(slotCounts[x][y]));
                        slotElementTexts[x][y].build();
                    }
                }
            }
        }
    }

    public void addItem(short item, byte count)
    {
        for (int x = 0; x < 1; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (slots[x][y] == item && slotCounts[x][y] <= 63)
                {
                    slotCounts[x][y] = (byte) (slotCounts[x][y] + count);

                    build();

                    return;
                }
                else if (slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    slots[x][y] = item;
                    slotCounts[x][y] = count;

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

        if (slots[position.x][position.y] == item && slotCounts[position.x][position.y] == count)
            return;

        slots[position.x][position.y] = item;
        slotCounts[position.x][position.y] = count;

        build();
    }

    public @Nullable SlotData getSlot(@NotNull Vector2i position)
    {
        if (!isPositionValid(position))
            return null;

        return new SlotData(slots[position.x][position.y], slotCounts[position.x][position.y]);
    }

    private boolean isPositionValid(@NotNull Vector2i position)
    {
        return position.x >= 0 && position.x < slots.length &&
                position.y >= 0 && position.y < slots[0].length;
    }

    public record SlotData(short id, byte count) { }
}