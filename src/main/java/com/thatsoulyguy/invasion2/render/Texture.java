package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@CustomConstructor("create")
public class Texture extends Component implements ManagerLinkedClass
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull AssetPath localPath;
    private @EffectivelyNotNull String fullPath;

    private @EffectivelyNotNull Filter filter;
    private @EffectivelyNotNull Wrapping wrapping;

    private transient int textureId;
    private @Nullable Vector2i dimensions = null;
    private boolean loadedFromMemory = false;

    private Texture() { }

    public void bind(int slot)
    {
        if (textureId == -1)
        {
            System.err.println("Texture '" + name + "' is invalid!");
            return;
        }

        GL41.glActiveTexture(GL41.GL_TEXTURE0 + slot);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, textureId);
    }

    public void unbind()
    {
        if (textureId == -1)
        {
            System.err.println("Texture '" + name + "' is invalid!");
            return;
        }

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
    }

    public void generate()
    {
        if (loadedFromMemory)
            return;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);

            ByteBuffer rawImage = loadImageAsByteBuffer(fullPath);

            if (rawImage == null)
                throw new RuntimeException("Failed to load texture file: " + fullPath);

            ByteBuffer image = STBImage.stbi_load_from_memory(rawImage, widthBuffer, heightBuffer, channelsBuffer, 4);

            if (image == null)
                throw new RuntimeException("Failed to decode texture file: " + fullPath + " " + STBImage.stbi_failure_reason());

            dimensions = new Vector2i(widthBuffer.get(), heightBuffer.get());

            textureId = GL41.glGenTextures();
            GL41.glBindTexture(GL41.GL_TEXTURE_2D, textureId);

            GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, dimensions.x, dimensions.y, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, image);

            GL41.glGenerateMipmap(GL41.GL_TEXTURE_2D);

            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, wrapping.getValue());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, wrapping.getValue());
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST_MIPMAP_NEAREST);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, filter.getValue());

            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_BASE_LEVEL, 0);
            GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAX_LEVEL, (int) Math.floor(Math.log(Math.max(dimensions.x, dimensions.y)) / Math.log(2)));
            GL41.glTexParameterf(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_LOD_BIAS, 0.0f);

            GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);

            STBImage.stbi_image_free(image);
        }
    }

    public @NotNull String getName()
    {
        return name;
    }

    public void setName(@NotNull String name)
    {
        this.name = name;
    }

    public @NotNull AssetPath getLocalPath()
    {
        return localPath;
    }

    public void setLocalPath(@NotNull AssetPath localPath)
    {
        this.localPath = localPath;
        this.fullPath = localPath.getFullPath();
    }

    public @NotNull String getFullPath()
    {
        return fullPath;
    }

    public @NotNull Filter getFilter()
    {
        return filter;
    }

    public void setFilter(@NotNull Filter filter)
    {
        this.filter = filter;
    }

    public @NotNull Wrapping getWrapping()
    {
        return wrapping;
    }

    public void setWrapping(@NotNull Wrapping wrapping)
    {
        this.wrapping = wrapping;
    }

    public @Nullable Vector2i getDimensions()
    {
        return dimensions;
    }

    public void setDimensions(@NotNull Vector2i dimensions)
    {
        this.dimensions = dimensions;
    }

    public int getTextureId()
    {
        return textureId;
    }

    public void setTextureId(int textureId)
    {
        this.textureId = textureId;
    }

    public void uploadRawData(@NotNull ByteBuffer pixelData, int width, int height)
    {
        this.dimensions = new Vector2i(width, height);
        this.textureId = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, textureId);

        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA, width, height, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, pixelData);

        GL41.glGenerateMipmap(GL41.GL_TEXTURE_2D);

        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, wrapping.getValue());
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, wrapping.getValue());

        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_NEAREST_MIPMAP_NEAREST);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, filter.getValue());

        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_BASE_LEVEL, 0);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAX_LEVEL, (int) Math.floor(Math.log(Math.max(dimensions.x, dimensions.y)) / Math.log(2)));
        GL41.glTexParameterf(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_LOD_BIAS, 0.0f);

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, 0);
    }

    private @Nullable ByteBuffer loadImageAsByteBuffer(@NotNull String resourcePath)
    {
        try (InputStream stream = Texture.class.getResourceAsStream(resourcePath))
        {
            if (stream == null)
                throw new IOException("Resource not found: " + resourcePath);

            byte[] bytes = stream.readAllBytes();

            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        }
        catch (Exception exception)
        {
            System.err.println("Failed to load image! " + exception.getMessage());
        }

        return null;
    }

    @Override
    public @NotNull Class<?> getManagingClass()
    {
        return TextureManager.class;
    }

    @Override
    public @NotNull String getManagedItem()
    {
        return name;
    }

    public void uninitialize_NoOverride()
    {
        if (textureId != 0)
        {
            GL41.glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    public static @NotNull Texture create(@NotNull String name, @NotNull Filter filter, @NotNull Wrapping wrapping, @NotNull AssetPath localPath)
    {
        Texture result = new Texture();

        result.setName(name);
        result.setLocalPath(localPath);
        result.setFilter(filter);
        result.setWrapping(wrapping);
        result.textureId = -1;

        result.generate();

        return result;
    }

    public static @NotNull Texture create(@NotNull String name, @NotNull Filter filter, @NotNull Wrapping wrapping, int width, int height, @NotNull ByteBuffer pixelData)
    {
        Texture result = new Texture();
        result.setName(name);
        result.filter = filter;
        result.wrapping = wrapping;
        result.loadedFromMemory = true;

        result.uploadRawData(pixelData, width, height);

        result.generate();

        return result;
    }

    public enum Filter
    {
        NEAREST(GL41.GL_NEAREST),
        LINEAR(GL41.GL_LINEAR);

        private final int value;

        Filter(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    public enum Wrapping
    {
        CLAMP_TO_EDGE(GL41.GL_CLAMP_TO_EDGE),
        CLAMP_TO_BORDER(GL41.GL_CLAMP_TO_BORDER),
        REPEAT(GL41.GL_REPEAT);

        private final int value;

        Wrapping(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }
}