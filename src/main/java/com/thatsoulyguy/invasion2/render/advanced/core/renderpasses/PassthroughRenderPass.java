package com.thatsoulyguy.invasion2.render.advanced.core.renderpasses;

import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.Shader;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import com.thatsoulyguy.invasion2.render.advanced.core.RenderPass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

public class PassthroughRenderPass implements RenderPass
{
    private int quadVAO = 0;
    private int quadVBO = 0;
    private int quadEBO = 0;

    private final LevelRenderPass scenePass;

    public PassthroughRenderPass(@NotNull LevelRenderPass scenePass)
    {
        this.scenePass = scenePass;
    }

    @Override
    public void initialize()
    {
        quadVAO = GL41.glGenVertexArrays();
        quadVBO = GL41.glGenBuffers();
        quadEBO = GL41.glGenBuffers();

        GL41.glBindVertexArray(quadVAO);

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(4 * 4);

        vertexData.put(new float[]
        {
            -1f, -1f,  0f, 0f,
             1f, -1f,  1f, 0f,
             1f,  1f,  1f, 1f,
            -1f,  1f,  0f, 1f
        });

        vertexData.flip();

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, quadVBO);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertexData, GL41.GL_STATIC_DRAW);

        IntBuffer indexData = BufferUtils.createIntBuffer(6);
        indexData.put(new int[]{0,1,2, 2,3,0}).flip();

        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, quadEBO);
        GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, indexData, GL41.GL_STATIC_DRAW);

        GL41.glVertexAttribPointer(0, 2, GL41.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL41.glEnableVertexAttribArray(0);

        GL41.glVertexAttribPointer(1, 2, GL41.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL41.glEnableVertexAttribArray(1);

        GL41.glBindVertexArray(0);
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        Shader shader = Objects.requireNonNull(ShaderManager.get("pass.passthrough"));

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);

        GL41.glClearColor(0, 0, 0, 1);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);

        shader.bind();

        int sceneColorTex = scenePass.getColorTexture();

        GL41.glActiveTexture(GL41.GL_TEXTURE0);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, sceneColorTex);

        shader.setShaderUniform("sceneTexture", 0);

        Framebuffer.renderFullscreenQuadrilateral();

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);

        shader.unbind();
    }

    @Override
    public void uninitialize()
    {
        GL41.glDeleteVertexArrays(quadVAO);
        GL41.glDeleteBuffers(quadVBO);
        GL41.glDeleteBuffers(quadEBO);
    }
}