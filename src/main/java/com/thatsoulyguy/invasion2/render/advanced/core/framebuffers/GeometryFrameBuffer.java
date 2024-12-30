package com.thatsoulyguy.invasion2.render.advanced.core.framebuffers;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public class GeometryFrameBuffer extends Framebuffer
{
    private int fboId;
    private int gPosition, gNormal, gAlbedo;
    private int rboDepth;

    @Override
    public void generate()
    {
        Vector2i dimensions = Window.getDimensions();

        int width = dimensions.x;
        int height = dimensions.y;

        fboId = GL41.glGenFramebuffers();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, fboId);

        gPosition = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gPosition);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA16F, width, height, 0, GL41.GL_RGBA, GL41.GL_FLOAT, (ByteBuffer)null);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, gPosition, 0);

        gNormal = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gNormal);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA16F, width, height, 0, GL41.GL_RGBA, GL41.GL_FLOAT, (ByteBuffer)null);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT1, GL41.GL_TEXTURE_2D, gNormal, 0);

        gAlbedo = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gAlbedo);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, width, height, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glFramebufferTexture2D(GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT2, GL41.GL_TEXTURE_2D, gAlbedo, 0);

        rboDepth = GL41.glGenRenderbuffers();

        GL41.glBindRenderbuffer(GL41.GL_RENDERBUFFER, rboDepth);
        GL41.glRenderbufferStorage(GL41.GL_RENDERBUFFER, GL41.GL_DEPTH_COMPONENT, width, height);
        GL41.glFramebufferRenderbuffer(GL41.GL_FRAMEBUFFER, GL41.GL_DEPTH_ATTACHMENT, GL41.GL_RENDERBUFFER, rboDepth);

        IntBuffer attachments = BufferUtils.createIntBuffer(3).put(new int[]
        {
            GL41.GL_COLOR_ATTACHMENT0,
            GL41.GL_COLOR_ATTACHMENT1,
            GL41.GL_COLOR_ATTACHMENT2
        });

        attachments.flip();

        GL41.glDrawBuffers(attachments);

        int status = GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER);

        if (status != GL41.GL_FRAMEBUFFER_COMPLETE)
            System.err.println("GBuffer not complete, status: " + status);

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
        return Map.of("gPosition", gPosition, "gNormal", gNormal, "gAlbedo", gAlbedo);
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
        GL41.glDeleteTextures(gPosition);
        GL41.glDeleteTextures(gNormal);
        GL41.glDeleteTextures(gAlbedo);
        GL41.glDeleteRenderbuffers(rboDepth);
    }
}