package com.thatsoulyguy.invasion2.render.advanced.ssao.framebuffers;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public class SSAOBlurFramebuffer extends Framebuffer
{
    private int fboId;
    private int ssaoBlurColor;

    @Override
    public void generate()
    {
        Vector2i dimensions = Window.getDimensions();

        int width = dimensions.x;
        int height = dimensions.y;

        fboId = GL41.glGenFramebuffers();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);

        ssaoBlurColor = GL41.glGenTextures();

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, ssaoBlurColor);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RED, width, height, 0, GL41.GL_RED, GL41.GL_FLOAT, (ByteBuffer)null);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, ssaoBlurColor, 0);

        int status = GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER);

        if (status != GL41.GL_FRAMEBUFFER_COMPLETE)
            System.err.println("SSAOBlurBuffer not complete, status: " + status);

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    @Override
    public int getBufferId()
    {
        return fboId;
    }

    @Override
    public Map<String, Integer> getColorAttachments()
    {
        return Collections.singletonMap("ssaoBlurColor", ssaoBlurColor);
    }

    @Override
    public void bind()
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);
    }

    @Override
    public void unbind()
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void uninitialize()
    {
        GL41.glDeleteFramebuffers(fboId);
        GL41.glDeleteTextures(ssaoBlurColor);
    }
}