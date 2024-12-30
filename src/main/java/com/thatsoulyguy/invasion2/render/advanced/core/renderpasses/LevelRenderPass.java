package com.thatsoulyguy.invasion2.render.advanced.core.renderpasses;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import com.thatsoulyguy.invasion2.render.advanced.core.RenderPass;
import com.thatsoulyguy.invasion2.render.advanced.core.framebuffers.LevelFramebuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL41;

public class LevelRenderPass implements RenderPass
{
    private LevelFramebuffer sceneFBO;
    private boolean active = true;

    @Override
    public void initialize()
    {
        sceneFBO = Framebuffer.create(LevelFramebuffer.class);
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        if (!active)
            return;

        sceneFBO.bind();

        GL41.glViewport(0, 0, Window.getDimensions().x, Window.getDimensions().y);

        GL41.glClearColor(0, 0, 0, 1);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void uninitialize()
    {
        if (sceneFBO != null)
        {
            sceneFBO.uninitialize();
            sceneFBO = null;
        }
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public int getColorTexture()
    {
        if (sceneFBO == null)
            return 0;

        return sceneFBO.getColorAttachments().getOrDefault("mainColor", 0);
    }

    public void unbindFBO()
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }
}