package com.thatsoulyguy.invasion2.render.advanced.core.renderpasses;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.Shader;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import com.thatsoulyguy.invasion2.render.advanced.core.RenderPass;
import com.thatsoulyguy.invasion2.render.advanced.core.framebuffers.GeometryFrameBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

public class GeometryRenderPass implements RenderPass
{
    private GeometryFrameBuffer gBuffer;
    private Shader geometryShader;

    @Override
    public void initialize()
    {
        gBuffer = Framebuffer.create(GeometryFrameBuffer.class);
        geometryShader = ShaderManager.get("pass.geometry");
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        gBuffer.bind();

        Vector2i dimensions = Window.getDimensions();
        GL41.glViewport(0, 0, dimensions.x, dimensions.y);

        GL41.glClearColor(0, 0, 0, 1);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);

        geometryShader.bind();
    }

    public void endRender()
    {
        geometryShader.unbind();
    }

    @Override
    public void uninitialize()
    {
        gBuffer.uninitialize();
        geometryShader.uninitialize();
    }

    public @NotNull Shader getGeometryShader()
    {
        return geometryShader;
    }

    public int getPositionTex()
    {
        return gBuffer.getColorAttachments().get("gPosition");
    }

    public int getNormalTex()
    {
        return gBuffer.getColorAttachments().get("gNormal");
    }

    public int getAlbedoTex()
    {
        return gBuffer.getColorAttachments().get("gAlbedo");
    }
}