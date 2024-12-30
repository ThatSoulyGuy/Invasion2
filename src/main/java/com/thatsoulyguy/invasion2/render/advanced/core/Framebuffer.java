package com.thatsoulyguy.invasion2.render.advanced.core;

import com.thatsoulyguy.invasion2.core.Window;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public abstract class Framebuffer
{
    private static int quadrilateralVao = -1;

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

    public static void renderFullscreenQuadrilateral()
    {
        if (quadrilateralVao == -1)
        {
            quadrilateralVao = GL41.glGenVertexArrays();
            int quadVBO = GL41.glGenBuffers();
            int quadEBO = GL41.glGenBuffers();

            GL41.glBindVertexArray(quadrilateralVao);

            FloatBuffer vertexData = BufferUtils.createFloatBuffer(4 * 4);

            vertexData.put(QUADRILATERAL_VERTICES);

            vertexData.flip();

            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, quadVBO);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertexData, GL41.GL_STATIC_DRAW);

            IntBuffer indexData = BufferUtils.createIntBuffer(6);
            indexData.put(QUADRILATERAL_INDICES).flip();

            GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, quadEBO);
            GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, indexData, GL41.GL_STATIC_DRAW);

            GL41.glVertexAttribPointer(0, 2, GL41.GL_FLOAT, false, 4 * Float.BYTES, 0);
            GL41.glEnableVertexAttribArray(0);

            GL41.glVertexAttribPointer(1, 2, GL41.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
            GL41.glEnableVertexAttribArray(1);

            GL41.glBindVertexArray(0);
        }

        GL41.glDisable(GL41.GL_DEPTH_TEST);

        GL41.glBindVertexArray(quadrilateralVao);

        GL41.glDrawElements(GL41.GL_TRIANGLES, 6, GL41.GL_UNSIGNED_INT, 0);

        GL41.glBindVertexArray(0);

        GL41.glEnable(GL41.GL_DEPTH_TEST);
    }

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