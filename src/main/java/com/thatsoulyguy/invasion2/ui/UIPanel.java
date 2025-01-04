package com.thatsoulyguy.invasion2.ui;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@CustomConstructor("create")
public class UIPanel implements Serializable
{
    private @EffectivelyNotNull String name;

    @EffectivelyNotNull GameObject object;

    private final @NotNull Map<String, UIElement> uiElementsMap = new LinkedHashMap<>();

    private boolean isActive = true;

    private UIPanel() { }

    public UIElement addElement(@NotNull UIElement element)
    {
        object.addChild(element.object);
        element.parent = this;

        uiElementsMap.putIfAbsent(element.getName(), element);

        return element;
    }

    public boolean hasElement(@NotNull String name)
    {
        return uiElementsMap.containsKey(name);
    }

    public @Nullable UIElement getElement(@NotNull String name)
    {
        return uiElementsMap.getOrDefault(name, null);
    }

    public void removeElement(@NotNull String name)
    {
        if (!uiElementsMap.containsKey(name))
        {
            System.err.println("UI element '" + name + "' not found!");
            return;
        }

        object.removeChild(object.getChild("ui." + name));
        GameObjectManager.unregister("ui." + name);

        uiElementsMap.remove(name);
    }

    public void update()
    {
        if (!isActive)
            return;

        uiElementsMap.values().parallelStream().forEach(UIElement::update);
    }

    public void save(@NotNull String directory)
    {
        File saveFile = new File(directory, "ui." + name + ".bin");

        File uiElementsDirectory = new File(directory, "ui." + name);

        if (!uiElementsDirectory.exists())
            uiElementsDirectory.mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(saveFile))
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeInt(uiElementsMap.size());

            uiElementsMap.values().forEach((object ->
            {
                try
                {
                    objectOutputStream.writeObject(object);

                    object.object.save(new File(uiElementsDirectory, "ui." + object.getName() + ".bin"), true);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }));

            System.out.println("Saved ui elements");

            objectOutputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to serialize ui! " + exception.getMessage());
        }
    }

    public static UIPanel load(@NotNull File file)
    {
        UIPanel result = new UIPanel();

        String name = file.getName().replace("ui.", "").replace(".bin", "");

        result.name = name;

        result.object = GameObject.create("ui." + name, Layer.UI);

        File uiElementsDirectory = new File(file.getAbsolutePath().replace(".bin", ""));

        try (FileInputStream fileInputStream = new FileInputStream(file))
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            int uiElementsMapSize = objectInputStream.readInt();

            for (int e = 0; e < uiElementsMapSize; e++)
            {
                UIElement element = (UIElement) objectInputStream.readObject();

                GameObject gameObject = GameObject.load(new File(uiElementsDirectory, "ui." + element.getName() + ".bin"));

                Field field = element.getClass().getDeclaredField("object");

                field.setAccessible(true);

                field.set(element, gameObject);

                result.object.addChild((GameObject) field.get(element));

                result.uiElementsMap.putIfAbsent(element.getName(), element);
            }

            System.out.println("Loaded ui elements");

            objectInputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to deserialize ui! " + exception.getMessage());
        }

        return result;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void setActive(boolean active)
    {
        uiElementsMap.values().forEach(element -> element.setActive(active));

        isActive = active;
    }

    public static @NotNull UIPanel create(@NotNull String name)
    {
        UIPanel result = new UIPanel();

        result.name = name;

        result.object = GameObject.create("ui." + name, Layer.UI);

        UIManager.register(result);

        return result;
    }
}