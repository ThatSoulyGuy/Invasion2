package com.thatsoulyguy.invasion2.render.advanced.core;

import com.thatsoulyguy.invasion2.core.Window;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class Framebuffer
{
    public static final float[] QUADRILATERAL_VERTICES =
    {
        -1f, -1f,    0f,  0f,
        +1f, -1f,    1f,  0f,
        +1f, +1f,    1f,  1f,
        -1f, +1f,    0f,  1f
    };

    public static final int[] QUADRILATERAL_INDICES =
    {
        0, 1, 2,
        2, 3, 0
    };

    protected Framebuffer()
    {
        Window.addOnResizeCompletedCallback(() ->
        {
            uninitialize();
            generate();
        });
    }

    public abstract void generate();

    public abstract int getBufferId();

    public abstract Map<String, Integer> getColorAttachments();

    public abstract void bind();

    public abstract void unbind();

    public void uninitialize() { }

    public static <T extends Framebuffer> @NotNull T create(@NotNull Class<T> clazz)
    {
        try
        {
            T result = clazz.getDeclaredConstructor().newInstance();

            result.generate();

            return result;
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Framebuffer! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}