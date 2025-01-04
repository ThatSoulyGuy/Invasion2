package com.thatsoulyguy.invasion2.ui;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.Texture;
import com.thatsoulyguy.invasion2.render.Vertex;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.Layer;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class UIElement implements Serializable
{
    public static final @NotNull List<Vertex> DEFAULT_VERTICES = List.of(new Vertex[]
    {
        Vertex.create(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(0.0f, 0.0f)),
        Vertex.create(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(1.0f, 0.0f)),
        Vertex.create(new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(1.0f, 1.0f)),
        Vertex.create(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), new Vector2f(0.0f, 1.0f)),
    });

    public static final @NotNull List<Integer> DEFAULT_INDICES = List.of(new Integer[]
    {
        0, 1, 2,
        2, 3, 0
    });

    @EffectivelyNotNull String name;

    protected transient @EffectivelyNotNull GameObject object;

    private @NotNull Alignment alignment = Alignment.CENTER;

    private @NotNull Vector2f offset = new Vector2f();

    private @NotNull List<Stretch> stretch = new ArrayList<>();

    @Nullable UIPanel parent;

    private boolean isActive = true;

    private boolean alignAndStretch = true;

    protected UIElement() { }

    public abstract void generate(@NotNull GameObject object);

    public void update()
    {
        if (!isActive)
            return;

        if (!alignAndStretch)
            return;

        applyStretch();

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

    private void applyStretch()
    {
        Vector2i windowDimensions = Window.getDimensions();
        Vector2f newDimensions = getDimensions();
        Vector2f newPosition = getPosition();

        if (stretch.contains(Stretch.LEFT) && stretch.contains(Stretch.RIGHT))
        {
            newDimensions.x = windowDimensions.x - offset.x * 2;
            newPosition.x = offset.x;
        }
        else if (stretch.contains(Stretch.LEFT))
        {
            float rightBoundary = newPosition.x + newDimensions.x;

            newDimensions.x = rightBoundary - offset.x;
            newPosition.x = offset.x;
        }
        else if (stretch.contains(Stretch.RIGHT))
            newDimensions.x = windowDimensions.x - newPosition.x - offset.x;

        if (stretch.contains(Stretch.TOP) && stretch.contains(Stretch.BOTTOM))
        {
            newDimensions.y = windowDimensions.y - offset.y * 2;
            newPosition.y = offset.y;
        }
        else if (stretch.contains(Stretch.TOP))
        {
            float bottomBoundary = newPosition.y + newDimensions.y;

            newDimensions.y = bottomBoundary - offset.y;
            newPosition.y = offset.y;
        }
        else if (stretch.contains(Stretch.BOTTOM))
            newDimensions.y = windowDimensions.y - newPosition.y - offset.y;

        setDimensions(newDimensions);

        if (!stretch.contains(Stretch.LEFT) && !stretch.contains(Stretch.RIGHT))
        {
            switch (alignment)
            {
                case LEFT:
                    newPosition.x = offset.x;
                    break;
                case RIGHT:
                    newPosition.x = windowDimensions.x - newDimensions.x - offset.x;
                    break;
                case CENTER:
                    newPosition.x = (windowDimensions.x - newDimensions.x) / 2.0f;
                    break;
                default:
                    break;
            }
        }

        if (!stretch.contains(Stretch.TOP) && !stretch.contains(Stretch.BOTTOM))
        {
            switch (alignment)
            {
                case TOP:
                    newPosition.y = offset.y;
                    break;
                case BOTTOM:
                    newPosition.y = windowDimensions.y - newDimensions.y - offset.y;
                    break;
                case CENTER:
                    newPosition.y = (windowDimensions.y - newDimensions.y) / 2.0f;
                    break;
                default:
                    break;
            }
        }

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

    public @NotNull List<Stretch> getStretch()
    {
        return stretch;
    }

    public void setStretch(@NotNull List<Stretch> stretch)
    {
        this.stretch = stretch;
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

    public boolean doesAlignAndStretch()
    {
        return alignAndStretch;
    }

    public void setAlignAndStretch(boolean alignAndStretch)
    {
        this.alignAndStretch = alignAndStretch;
    }

    public void setColor(@NotNull Vector3f color)
    {
        object.getComponentNotNull(Mesh.class).setVertices(object.getComponentNotNull(Mesh.class).getVertices().stream().map(vertex -> Vertex.create(vertex.getPosition(), color, vertex.getNormal(), vertex.getUVs())).collect(Collectors.toList()));
        object.getComponentNotNull(Mesh.class).onLoad();
    }

    public boolean getTransparent()
    {
        return object.getComponentNotNull(Mesh.class).isTransparent();
    }

    public void setTransparent(boolean transparent)
    {
        object.getComponentNotNull(Mesh.class).setTransparent(transparent);
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
        Vector3f position = object.getTransform().getLocalPosition();

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
        return object.getTransform().getLocalRotation().z;
    }

    public void setRotation(float rotation)
    {
        object.getTransform().setLocalRotation(new Vector3f(0.0f, 0.0f, rotation));
    }

    public @NotNull Vector2f getDimensions()
    {
        Vector3f dimensions = object.getTransform().getLocalScale();

        return new Vector2f(dimensions.x, dimensions.y);
    }

    public void setDimensions(@NotNull Vector2f dimensions)
    {
        object.getTransform().setLocalScale(new Vector3f(dimensions.x, dimensions.y, 0.0f));
    }

    public void setUVs(@NotNull Vector2f[] uvs)
    {
        List<Vertex> vertices = List.of(new Vertex[]
        {
            Vertex.create(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), uvs[0]),
            Vertex.create(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), uvs[1]),
            Vertex.create(new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), uvs[2]),
            Vertex.create(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f), new Vector3f(0.0f), uvs[3]),
        });

        object.getComponentNotNull(Mesh.class).setVertices(vertices);
        MainThreadExecutor.submit(() -> object.getComponentNotNull(Mesh.class).onLoad());
    }

    public static <T extends UIElement> @NotNull T create(@NotNull Class<T> clazz, @NotNull String name, @NotNull Vector2f position, @NotNull Vector2f dimensions)
    {
        T result;

        try
        {
            result = clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from UIElement! This shouldn't happen!");
            return clazz.cast(new Object());
        }

        result.name = name;

        result.object = GameObject.create("ui." + name, Layer.UI);
        result.object.getTransform().setLocalPosition(new Vector3f(position.x, position.y, 0.0f));
        result.object.getTransform().setLocalScale(new Vector3f(dimensions.x, dimensions.y, 1.0f));

        result.generate(result.object);

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

    public enum Stretch
    {
        MIDDLE,
        CENTER,
        TOP,
        BOTTOM,
        RIGHT,
        LEFT
    }
}