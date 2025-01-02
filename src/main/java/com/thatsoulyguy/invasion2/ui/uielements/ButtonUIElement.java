package com.thatsoulyguy.invasion2.ui.uielements;

import com.thatsoulyguy.invasion2.input.*;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.ui.UIElement;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ButtonUIElement extends UIElement
{
    private final @NotNull List<Runnable> onClicked = new ArrayList<>();
    private final @NotNull List<Runnable> onReleased = new ArrayList<>();
    private final @NotNull List<Runnable> onHoveringBegin = new ArrayList<>();
    private final @NotNull List<Runnable> onHovering = new ArrayList<>();
    private final @NotNull List<Runnable> onHoveringEnd = new ArrayList<>();

    private boolean wasMouseHeld;
    private boolean wasHovering;

    @Override
    public void update()
    {
        super.update();

        Vector2f dimensions = getDimensions();
        Vector2f position = getPosition();
        Vector2f mousePosition = InputManager.getMousePosition();

        boolean isOver = isMouseOver(position, dimensions, mousePosition);
        boolean isMousePressed = InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.PRESSED) ||
                InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.HELD);
        boolean isMouseReleased = InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.RELEASED);

        if (isOver && !wasHovering)
        {
            onHoveringBegin.forEach(Runnable::run);
            wasHovering = true;
        }

        if (isOver)
            onHovering.forEach(Runnable::run);

        if (!isOver && wasHovering)
        {
            onHoveringEnd.forEach(Runnable::run);
            wasHovering = false;
        }

        if (isOver && isMousePressed)
        {
            onClicked.forEach(Runnable::run);
            wasMouseHeld = true;
        }

        if (isOver && isMouseReleased && wasMouseHeld)
        {
            onReleased.forEach(Runnable::run);
            wasMouseHeld = false;
        }

        if (!isMousePressed)
            wasMouseHeld = false;
    }

    @Override
    public void generate(@NotNull GameObject object)
    {
        object.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        object.addComponent(Objects.requireNonNull(TextureManager.get("error")));

        object.addComponent(Mesh.create(DEFAULT_VERTICES, DEFAULT_INDICES));

        object.getComponentNotNull(Mesh.class).setTransparent(true);
        object.getComponentNotNull(Mesh.class).onLoad();
    }

    public void addOnClickedEvent(@NotNull Runnable runnable)
    {
        onClicked.add(runnable);
    }

    public void removeOnClickedEvent(@NotNull Runnable runnable)
    {
        onClicked.remove(runnable);
    }

    public void addOnReleasedEvent(@NotNull Runnable runnable)
    {
        onReleased.add(runnable);
    }

    public void removeOnReleasedEvent(@NotNull Runnable runnable)
    {
        onReleased.remove(runnable);
    }

    public void addOnHoveringBeginEvent(@NotNull Runnable runnable)
    {
        onHoveringBegin.add(runnable);
    }

    public void removeOnHoveringBeginEvent(@NotNull Runnable runnable)
    {
        onHoveringBegin.remove(runnable);
    }

    public void addOnHoveringEvent(@NotNull Runnable runnable)
    {
        onHovering.add(runnable);
    }

    public void removeOnHoveringEvent(@NotNull Runnable runnable)
    {
        onHovering.remove(runnable);
    }

    public void addOnHoveringEndEvent(@NotNull Runnable runnable)
    {
        onHoveringEnd.add(runnable);
    }

    public void removeOnHoveringEndEvent(@NotNull Runnable runnable)
    {
        onHoveringEnd.remove(runnable);
    }

    private boolean isMouseOver(Vector2f position, Vector2f dimensions, Vector2f mousePosition)
    {
        float xMin = position.x;
        float xMax = position.x + dimensions.x;
        float yMin = position.y;
        float yMax = position.y + dimensions.y;

        return (mousePosition.x >= xMin && mousePosition.x <= xMax) && (mousePosition.y >= yMin && mousePosition.y <= yMax);
    }
}