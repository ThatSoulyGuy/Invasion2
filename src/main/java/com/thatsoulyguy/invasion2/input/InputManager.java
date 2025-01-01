package com.thatsoulyguy.invasion2.input;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.lwjgl.glfw.*;

import java.util.Arrays;

@Static
public class InputManager
{
    private static final boolean[] currentKeyStates = new boolean[GLFW.GLFW_KEY_LAST];
    private static final boolean[] previousKeyStates = new boolean[GLFW.GLFW_KEY_LAST];

    private static final boolean[] currentMouseButtonStates = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private static final boolean[] previousMouseButtonStates = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];

    private static final @NotNull Vector2f mousePosition = new Vector2f();
    private static final @NotNull Vector2f lastMousePosition = new Vector2f();
    private static final @NotNull Vector2f mouseDelta = new Vector2f();

    private static @NotNull MouseMode mouseMode = MouseMode.UNKNOWN;

    private static float scrollOffset = 0.0f;

    private static final @NotNull Object keyStateLock = new Object();

    private static @EffectivelyNotNull GLFWKeyCallback keyCallback;
    private static @EffectivelyNotNull GLFWMouseButtonCallback mouseButtonCallback;
    private static @EffectivelyNotNull GLFWCursorPosCallback mousePositionCallback;
    private static @EffectivelyNotNull GLFWScrollCallback scrollCallback;

    private InputManager() { }

    public static void initialize()
    {
        keyCallback = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long windowHandle, int key, int scancode, int action, int mods)
            {
                if (key < 0 || key >= GLFW.GLFW_KEY_LAST)
                    return;
                synchronized (keyStateLock)
                {
                    switch (action)
                    {
                        case GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT:
                            currentKeyStates[key] = true;
                            break;
                        case GLFW.GLFW_RELEASE:
                            currentKeyStates[key] = false;
                            break;
                    }
                }
            }
        };

        mouseButtonCallback = new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long windowHandle, int button, int action, int mods)
            {
                if (button < 0 || button >= GLFW.GLFW_MOUSE_BUTTON_LAST)
                    return;

                switch (action)
                {
                    case GLFW.GLFW_PRESS:
                        currentMouseButtonStates[button] = true;
                        break;
                    case GLFW.GLFW_RELEASE:
                        currentMouseButtonStates[button] = false;
                        break;
                    default:
                        break;
                }
            }
        };

        mousePositionCallback = new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long windowHandle, double xpos, double ypos)
            {
                lastMousePosition.set(mousePosition);

                mousePosition.set((float) xpos, (float) ypos);
                mouseDelta.set((float) xpos, (float) ypos).sub(lastMousePosition);
            }
        };

        scrollCallback = new GLFWScrollCallback()
        {
            @Override
            public void invoke(long windowHandle, double xoffset, double yoffset)
            {
                scrollOffset += (float) yoffset;
            }
        };
    }

    public static void update()
    {
        System.arraycopy(currentKeyStates, 0, previousKeyStates, 0, currentKeyStates.length);
        System.arraycopy(currentMouseButtonStates, 0, previousMouseButtonStates, 0, currentMouseButtonStates.length);

        mouseDelta.set(mousePosition).sub(lastMousePosition);
        lastMousePosition.set(mousePosition);

        scrollOffset = 0.0f;
    }

    public static void setMouseMode(@NotNull MouseMode mode)
    {
        MainThreadExecutor.submit(() ->
        {
            switch (mode)
            {
                case FREE -> GLFW.glfwSetInputMode(Window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                case LOCKED -> GLFW.glfwSetInputMode(Window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            }
        });

        System.arraycopy(currentKeyStates, 0, previousKeyStates, 0, currentKeyStates.length);

        mouseMode = mode;
    }

    public static @NotNull MouseMode getMouseMode()
    {
        return mouseMode;
    }

    public static boolean getKeyState(KeyCode code, KeyState state)
    {
        int key = code.getValue();

        if (key < 0 || key >= GLFW.GLFW_KEY_LAST)
            return false;

        synchronized (keyStateLock)
        {
            return switch (state)
            {
                case PRESSED -> currentKeyStates[key] && !previousKeyStates[key];
                case HELD -> currentKeyStates[key] && previousKeyStates[key];
                case RELEASED -> !currentKeyStates[key] && previousKeyStates[key];
            };
        }
    }

    public static boolean getMouseState(MouseCode code, MouseState state)
    {
        int button = code.getValue();

        if (button < 0 || button >= GLFW.GLFW_MOUSE_BUTTON_LAST)
            return false;

        return switch (state)
        {
            case PRESSED -> currentMouseButtonStates[button] && !previousMouseButtonStates[button];
            case HELD -> currentMouseButtonStates[button] && previousMouseButtonStates[button];
            case RELEASED -> !currentMouseButtonStates[button] && previousMouseButtonStates[button];
        };
    }

    public static @NotNull Vector2f getMousePosition()
    {
        return new Vector2f(mousePosition);
    }

    public static @NotNull Vector2f getMouseDelta()
    {
        return new Vector2f(mouseDelta);
    }

    public static float getScrollDelta()
    {
        return scrollOffset;
    }

    public static @NotNull GLFWKeyCallback getKeyCallback()
    {
        return keyCallback;
    }

    public static @NotNull GLFWMouseButtonCallback getMouseButtonCallback()
    {
        return mouseButtonCallback;
    }

    public static @NotNull GLFWCursorPosCallback getMousePositionCallback()
    {
        return mousePositionCallback;
    }

    public static @NotNull GLFWScrollCallback getScrollCallback()
    {
        return scrollCallback;
    }

    public static void uninitialize()
    {
        if (keyCallback != null)
        {
            keyCallback.free();
            keyCallback = null;
        }

        if (mouseButtonCallback != null)
        {
            mouseButtonCallback.free();
            mouseButtonCallback = null;
        }

        if (mousePositionCallback != null)
        {
            mousePositionCallback.free();
            mousePositionCallback = null;
        }

        if (scrollCallback != null)
        {
            scrollCallback.free();
            scrollCallback = null;
        }
    }
}