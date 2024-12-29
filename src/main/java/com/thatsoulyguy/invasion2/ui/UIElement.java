package com.thatsoulyguy.invasion2.ui;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.*;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@CustomConstructor("create")
public class UIElement implements Serializable
{
    private static final @NotNull List<Vertex> DEFAULT_VERTICES = List.of(new Vertex[]
    {
        Vertex.create(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(0.0f, 0.0f)),
        Vertex.create(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(1.0f, 0.0f)),
        Vertex.create(new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(1.0f, 1.0f)),
        Vertex.create(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(0.0f, 1.0f)),
    });

    private static final @NotNull List<Integer> DEFAULT_INDICES = List.of(new Integer[]
    {
        0, 1, 2,
        2, 3, 0
    });

    private @EffectivelyNotNull String name;

    transient @EffectivelyNotNull GameObject object;

    private @NotNull Alignment alignment = Alignment.CENTER;
    private @NotNull Vector2f offset = new Vector2f();

    @Nullable UIPanel parent;

    private boolean isActive = true;

    private long frameCounter = 0;

    private UIElement() { }

    public void update()
    {
        if (!isActive)
            return;

        frameCounter++;

        if (frameCounter < 2)
            return;

        frameCounter = 0;

        Vector2i windowDimensions = Window.getDimensions();
        Vector2f elementDimensions = getDimensions();
        Vector2f newPosition = new Vector2f();

        switch (alignment)
        {
            case TOP:
                newPosition.x = (windowDimensions.x - elementDimensions.x) / 2.0f;
                newPosition.y = 0;
                break;

            case BOTTOM:
                newPosition.x = (windowDimensions.x - elementDimensions.x) / 2.0f;
                newPosition.y = windowDimensions.y - elementDimensions.y;
                break;

            case CENTER:
                newPosition.x = (windowDimensions.x - elementDimensions.x) / 2.0f;
                newPosition.y = (windowDimensions.y - elementDimensions.y) / 2.0f;
                break;

            case RIGHT:
                newPosition.x = windowDimensions.x - elementDimensions.x;
                newPosition.y = (windowDimensions.y - elementDimensions.y) / 2.0f;
                break;

            case LEFT:
                newPosition.x = 0;
                newPosition.y = (windowDimensions.y - elementDimensions.y) / 2.0f;
                break;
        }

        newPosition.add(offset);

        setPosition(newPosition);
    }

    public @NotNull Alignment getAlignment()
    {
        return alignment;
    }

    public void setAlignment(@NotNull Alignment alignment)
    {
        this.alignment = alignment;
    }

    public @NotNull Vector2f getOffset()
    {
        return offset;
    }

    public void setOffset(@NotNull Vector2f offset)
    {
        this.offset = offset;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull Texture getTexture()
    {
        return object.getComponentNotNull(Texture.class);
    }

    public void setTexture(@NotNull Texture texture)
    {
        object.setComponent(texture);
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void setActive(boolean active)
    {
        object.setActive(active);

        isActive = active;
    }

    public @NotNull Vector2f getPosition()
    {
        Vector3f position = object.getTransform().getWorldPosition();

        return new Vector2f(position.x, position.y);
    }

    public void translate(@NotNull Vector2f translation)
    {
        object.getTransform().translate(new Vector3f(translation.x, translation.y, 0.0f));
    }

    public void setPosition(@NotNull Vector2f position)
    {
        object.getTransform().setLocalPosition(new Vector3f(position.x, position.y, 0.0f));
    }

    public void rotate(@NotNull Vector2f rotation)
    {
        object.getTransform().rotate(new Vector3f(rotation.x, rotation.y, 0.0f));
    }

    public float getRotation()
    {
        return object.getTransform().getWorldRotation().z;
    }

    public void setRotation(float rotation)
    {
        object.getTransform().setLocalRotation(new Vector3f(0.0f, 0.0f, rotation));
    }

    public @NotNull Vector2f getDimensions()
    {
        Vector3f dimensions = object.getTransform().getWorldScale();

        return new Vector2f(dimensions.x, dimensions.y);
    }

    public void setDimensions(@NotNull Vector2f dimensions)
    {
        object.getTransform().setLocalScale(new Vector3f(dimensions.x, dimensions.y, 0.0f));
    }

    public static @NotNull UIElement create(@NotNull String name, @NotNull Vector2f position, @NotNull Vector2f dimensions)
    {
        UIElement result = new UIElement();

        result.name = name;

        result.object = GameObject.create("ui." + name, Layer.UI);

        result.object.getTransform().setLocalPosition(new Vector3f(position.x, position.y, 0.0f));
        result.object.getTransform().setLocalScale(new Vector3f(dimensions.x, dimensions.y, 1.0f));

        result.object.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        result.object.addComponent(Objects.requireNonNull(TextureManager.get("white")));

        result.object.addComponent(Mesh.create(DEFAULT_VERTICES, DEFAULT_INDICES));

        result.object.getComponentNotNull(Mesh.class).onLoad();

        result.object.setTransient(true);

        return result;
    }

    public enum Alignment
    {
        TOP,
        BOTTOM,
        CENTER,
        RIGHT,
        LEFT
    }
}