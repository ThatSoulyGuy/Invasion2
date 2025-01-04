package com.thatsoulyguy.invasion2.render.advanced.ssao.renderpasses;

import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.Shader;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.advanced.core.Framebuffer;
import com.thatsoulyguy.invasion2.render.advanced.core.RenderPass;
import com.thatsoulyguy.invasion2.render.advanced.ssao.framebuffers.SSAOFramebuffer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SSAORenderPass implements RenderPass
{
    private SSAOFramebuffer ssaoBuffer;
    private Shader ssaoShader;

    private final int positionTex;
    private final int normalTex;

    private int noiseTexture;
    private List<Vector3f> ssaoKernel;

    public SSAORenderPass(int positionTex, int normalTex)
    {
        this.positionTex = positionTex;
        this.normalTex = normalTex;
    }

    @Override
    public void initialize()
    {
        ssaoBuffer = Framebuffer.create(SSAOFramebuffer.class);

        ssaoShader = ShaderManager.get("ssao.default");

        ssaoKernel = new ArrayList<>(64);
        Random random = new Random();

        for (int i = 0; i < 64; i++)
        {
            float x = random.nextFloat() * 2.0f - 1.0f;
            float y = random.nextFloat() * 2.0f - 1.0f;
            float z = random.nextFloat();

            Vector3f sample = new Vector3f(x, y, z).normalize();

            float scale = (float) i / 64.0f;

            float lerpVal = 0.1f + (1.0f - 0.1f) * (scale * scale);

            sample.mul(lerpVal);

            ssaoKernel.add(sample);
        }

        List<Vector3f> noiseData = new ArrayList<>(16);

        for (int i = 0; i < 16; i++)
        {
            float x = random.nextFloat() * 2.0f - 1.0f;
            float y = random.nextFloat() * 2.0f - 1.0f;
            noiseData.add(new Vector3f(x, y, 0.0f));
        }

        noiseTexture = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, noiseTexture);

        FloatBuffer noiseBuf = BufferUtils.createFloatBuffer(noiseData.size() * 3);

        for (Vector3f v : noiseData)
            noiseBuf.put(v.x).put(v.y).put(v.z);

        noiseBuf.flip();

        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGB32F, 4, 4, 0, GL41.GL_RGB, GL41.GL_FLOAT, noiseBuf);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, GL41.GL_REPEAT);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, GL41.GL_REPEAT);
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        ssaoBuffer.bind();

        Vector2i dimensions = Window.getDimensions();
        GL41.glViewport(0, 0, dimensions.x, dimensions.y);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);

        ssaoShader.bind();

        for (int i = 0; i < 64; i++)
            ssaoShader.setShaderUniform("samples[" + i + "]", ssaoKernel.get(i));

        ssaoShader.setShaderUniform("windowWidth", (float) Window.getDimensions().x);
        ssaoShader.setShaderUniform("windowHeight", (float) Window.getDimensions().y);

        ssaoShader.setShaderUniform("projection", camera.getProjectionMatrix());

        GL41.glActiveTexture(GL41.GL_TEXTURE0);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, positionTex);
        ssaoShader.setShaderUniform("gPosition", 0);

        GL41.glActiveTexture(GL41.GL_TEXTURE1);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, normalTex);
        ssaoShader.setShaderUniform("gNormal", 1);

        GL41.glActiveTexture(GL41.GL_TEXTURE2);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, noiseTexture);
        ssaoShader.setShaderUniform("texNoise", 2);

        Framebuffer.renderFullscreenQuadrilateral();

        ssaoShader.unbind();
    }

    @Override
    public void uninitialize()
    {
        ssaoBuffer.uninitialize();
        ssaoShader.uninitialize();
        GL41.glDeleteTextures(noiseTexture);
    }

    public int getSSAOColor()
    {
        return ssaoBuffer.getColorAttachments().get("ssaoColor");
    }
}