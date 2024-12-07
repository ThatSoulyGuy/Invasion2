package com.thatsoulyguy.invasion2.input;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public enum MouseCode
{
    MOUSE_LEFT(GLFW.GLFW_MOUSE_BUTTON_1),
    MOUSE_RIGHT(GLFW.GLFW_MOUSE_BUTTON_2),
    MOUSE_MIDDLE(GLFW.GLFW_MOUSE_BUTTON_3),
    UNKNOWN(-1);

    private final int value;

    MouseCode(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static @NotNull MouseCode fromGLFWKey(int glfwKey)
    {
        for (MouseCode mouseCode : MouseCode.values())
        {
            if (mouseCode.getValue() == glfwKey)
                return mouseCode;
        }

        return MouseCode.UNKNOWN;
    }
}