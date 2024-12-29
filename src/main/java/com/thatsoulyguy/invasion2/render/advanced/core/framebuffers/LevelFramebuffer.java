package com.thatsoulyguy.invasion2.render.advanced.core.framebuffers;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import org.lwjgl.opengl.GL41;

import java.util.Map;

public class LevelFramebuffer extends Framebuffer
{
    private int framebufferId;
    private int colorTextureId;
    private int depthRenderbufferId;

    @Override
    public void generate()
    {
        framebufferId = GL41.glGenFramebuffers();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, framebufferId);

        colorTextureId = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, colorTextureId);

        int width = Window.getDimensions().x;
        int height = Window.getDimensions().y;

        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA8, width, height, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_LINEAR);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_LINEAR);

        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, colorTextureId, 0);

        depthRenderbufferId = GL41.glGenRenderbuffers();

        GL41.glBindRenderbuffer(GL41.GL_RENDERBUFFER, depthRenderbufferId);
        GL41.glRenderbufferStorage(GL41.GL_RENDERBUFFER, GL41.GL_DEPTH_COMPONENT24, width, height);
        GL41.glFramebufferRenderbuffer(GL41.GL_FRAMEBUFFER, GL41.GL_DEPTH_ATTACHMENT, GL41.GL_RENDERBUFFER, depthRenderbufferId);

        int status = GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER);

        if (status != GL41.GL_FRAMEBUFFER_COMPLETE)
            System.err.println("SceneFramebuffer incomplete! Status = " + status);

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void bind()
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, framebufferId);
    }

    @Override
    public void unbind()
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    @Override
    public int getBufferId()
    {
        return framebufferId;
    }

    @Override
    public Map<String, Integer> getColorAttachments()
    {
        return Map.of("mainColor", colorTextureId);
    }

    @Override
    public void uninitialize()
    {
        GL41.glDeleteRenderbuffers(depthRenderbufferId);
        GL41.glDeleteTextures(colorTextureId);
        GL41.glDeleteFramebuffers(framebufferId);
    }
}