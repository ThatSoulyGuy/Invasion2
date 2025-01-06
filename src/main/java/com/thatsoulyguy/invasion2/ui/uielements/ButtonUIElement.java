package com.thatsoulyguy.invasion2.ui.uielements;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.input.MouseCode;
import com.thatsoulyguy.invasion2.input.MouseState;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.ui.UIElement;
import com.thatsoulyguy.invasion2.util.SerializableRunnable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ButtonUIElement extends UIElement
{
    private final @NotNull List<SerializableRunnable> onLeftClicked = new ArrayList<>();
    private final @NotNull List<SerializableRunnable> onRightClicked = new ArrayList<>();
    private final @NotNull List<SerializableRunnable> onReleased = new ArrayList<>();
    private final @NotNull List<SerializableRunnable> onHoveringBegin = new ArrayList<>();
    private final @NotNull List<SerializableRunnable> onHovering = new ArrayList<>();
    private final @NotNull List<SerializableRunnable> onHoveringEnd = new ArrayList<>();

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
        boolean isMouseLeftClicked = InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.PRESSED);
        boolean isMouseRightClicked = InputManager.getMouseState(MouseCode.MOUSE_RIGHT, MouseState.PRESSED);
        boolean isMouseReleased = InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.RELEASED) || InputManager.getMouseState(MouseCode.MOUSE_RIGHT, MouseState.RELEASED);

        if (isOver && !wasHovering)
        {
            onHoveringBegin.forEach(SerializableRunnable::run);
            wasHovering = true;
        }

        if (isOver)
            onHovering.forEach(SerializableRunnable::run);

        if (!isOver && wasHovering)
        {
            onHoveringEnd.forEach(SerializableRunnable::run);
            wasHovering = false;
        }

        if (isOver && isMouseLeftClicked)
        {
            onLeftClicked.forEach(SerializableRunnable::run);
            wasMouseHeld = true;
        }

        if (isOver && isMouseRightClicked)
        {
            onRightClicked.forEach(SerializableRunnable::run);
            wasMouseHeld = true;
        }

        if (isOver && isMouseReleased && wasMouseHeld)
        {
            onReleased.forEach(SerializableRunnable::run);
            wasMouseHeld = false;
        }

        if (!isMouseLeftClicked)
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

    public void addOnLeftClickedEvent(@NotNull SerializableRunnable runnable)
    {
        onLeftClicked.add(runnable);
    }

    public void removeOnLeftClickedEvent(@NotNull SerializableRunnable runnable)
    {
        onLeftClicked.remove(runnable);
    }

    public void addOnRightClickedEvent(@NotNull SerializableRunnable runnable)
    {
        onRightClicked.add(runnable);
    }

    public void removeOnRightClickedEvent(@NotNull SerializableRunnable runnable)
    {
        onRightClicked.remove(runnable);
    }

    public void addOnReleasedEvent(@NotNull SerializableRunnable runnable)
    {
        onReleased.add(runnable);
    }

    public void removeOnReleasedEvent(@NotNull SerializableRunnable runnable)
    {
        onReleased.remove(runnable);
    }

    public void addOnHoveringBeginEvent(@NotNull SerializableRunnable runnable)
    {
        onHoveringBegin.add(runnable);
    }

    public void removeOnHoveringBeginEvent(@NotNull SerializableRunnable runnable)
    {
        onHoveringBegin.remove(runnable);
    }

    public void addOnHoveringEvent(@NotNull SerializableRunnable runnable)
    {
        onHovering.add(runnable);
    }

    public void removeOnHoveringEvent(@NotNull SerializableRunnable runnable)
    {
        onHovering.remove(runnable);
    }

    public void addOnHoveringEndEvent(@NotNull SerializableRunnable runnable)
    {
        onHoveringEnd.add(runnable);
    }

    public void removeOnHoveringEndEvent(@NotNull SerializableRunnable runnable)
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