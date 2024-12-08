package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.render.Texture;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CustomConstructor("create")
public class TextureAtlas extends Component implements ManagerLinkedClass
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull AssetPath localDirectory;
    private @EffectivelyNotNull String directory;

    private final @NotNull ConcurrentMap<String, Vector2f[]> subTextureMap = new ConcurrentHashMap<>();
    private @Nullable transient Texture outputTexture = null;

    private TextureAtlas() { }

    @Override
    public void initialize()
    {
        try
        {
            List<String> imageFiles = listImageFiles(directory);
            if (imageFiles.isEmpty())
            {
                System.err.println("No images found in directory: " + directory);
                return;
            }

            List<ImageData> images = new ArrayList<>();
            long totalArea = 0;

            for (String imagePath : imageFiles)
            {
                ImageData data = loadImageData(imagePath);

                if (data == null)
                {
                    System.err.println("Failed to load image: " + imagePath);
                    continue;
                }

                images.add(data);
                totalArea += (long)data.width * (long)data.height;
            }

            if (images.isEmpty())
            {
                System.err.println("No valid images loaded.");
                return;
            }

            int atlasSize = (int)Math.ceil(Math.sqrt(totalArea));
            images.sort((a, b) -> Integer.compare(b.height, a.height));

            ByteBuffer atlasBuffer = createAtlasBuffer(images, atlasSize);

            if (atlasBuffer == null)
            {
                System.err.println("Could not pack textures into atlas.");
                return;
            }

            this.outputTexture = Texture.create(name + "_atlas", Texture.Filter.NEAREST, Texture.Wrapping.CLAMP_TO_EDGE, atlasSize, atlasSize, atlasBuffer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    public @Nullable Texture getOutputTexture()
    {
        return outputTexture;
    }

    public @NotNull ConcurrentMap<String, Vector2f[]> getSubTextureMap()
    {
        return subTextureMap;
    }

    public @NotNull AssetPath getLocalDirectory()
    {
        return localDirectory;
    }

    public void setLocalDirectory(@NotNull AssetPath localDirectory)
    {
        this.localDirectory = localDirectory;
        this.directory = localDirectory.getFullPath();
    }

    public @NotNull String getDirectory()
    {
        return directory;
    }

    public void setDirectory(@NotNull String directory)
    {
        this.directory = directory;
    }

    private ByteBuffer createAtlasBuffer(List<ImageData> images, int atlasSize)
    {
        ByteBuffer atlasBuffer = ByteBuffer.allocateDirect(atlasSize * atlasSize * 4);
        atlasBuffer.order(ByteOrder.nativeOrder());

        for (int i = 0; i < atlasSize * atlasSize * 4; i++)
            atlasBuffer.put((byte)0x00);

        atlasBuffer.flip();

        int currentX = 0;
        int currentY = 0;
        int rowHeight = 0;

        atlasBuffer = atlasBuffer.duplicate();

        for (ImageData img : images)
        {
            if (currentX + img.width > atlasSize)
            {
                currentX = 0;
                currentY += rowHeight;
                rowHeight = 0;
            }

            if (currentY + img.height > atlasSize)
            {
                System.err.println("Cannot fit image: " + img.name + " in atlas.");
                return null;
            }

            for (int row = 0; row < img.height; row++)
            {
                int srcPos = row * img.width * 4;
                int dstPos = ((currentY + row) * atlasSize + currentX) * 4;

                atlasBuffer.position(dstPos);
                atlasBuffer.put(img.pixels, srcPos, img.width * 4);
            }

            float u0 = (float)currentX / (float)atlasSize;
            float v0 = (float)currentY / (float)atlasSize;
            float u1 = (float)(currentX + img.width) / (float)atlasSize;
            float v1 = (float)(currentY + img.height) / (float)atlasSize;

            Vector2f[] uvs = new Vector2f[]
            {
                new Vector2f(u0, v0),
                new Vector2f(u0, v1),
                new Vector2f(u1, v1),
                new Vector2f(u1, v0)
            };

            subTextureMap.put(img.name.replace(".png", ""), uvs);

            currentX += img.width;

            if (img.height > rowHeight)
                rowHeight = img.height;
        }

        atlasBuffer.position(0);
        return atlasBuffer;
    }

    private List<String> listImageFiles(String directoryPath)
    {
        List<String> result = new ArrayList<>();

        try
        {
            URL dirURL = TextureAtlas.class.getResource(directoryPath);

            if (dirURL == null)
            {
                System.err.println("Directory not found: " + directoryPath);
                return result;
            }

            File dir = new File(dirURL.toURI());

            File[] files = dir.listFiles((d, name) ->
            {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
            });

            if (files != null)
            {
                for (File f : files)
                {
                    String relPath = directoryPath + "/" + f.getName();
                    result.add(relPath);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private ImageData loadImageData(String resourcePath)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);

            ByteBuffer rawImage = loadResourceAsByteBuffer(resourcePath);

            if (rawImage == null)
                return null;

            ByteBuffer image = STBImage.stbi_load_from_memory(rawImage, widthBuffer, heightBuffer, channelsBuffer, 4);

            if (image == null)
            {
                System.err.println("Failed to load image: " + resourcePath + " Reason: " + STBImage.stbi_failure_reason());
                return null;
            }

            int w = widthBuffer.get(0);
            int h = heightBuffer.get(0);

            byte[] pixels = new byte[w * h * 4];
            image.get(pixels);

            //STBImage.stbi_image_free(image); TODO: For some reason this causes a silent crash.

            String fileName = new File(resourcePath).getName();

            ImageData data = new ImageData();

            data.name = fileName;
            data.width = w;
            data.height = h;
            data.pixels = pixels;

            return data;
        }
    }

    private ByteBuffer loadResourceAsByteBuffer(@NotNull String resourcePath)
    {
        try (InputStream stream = Texture.class.getResourceAsStream(resourcePath))
        {
            if (stream == null)
            {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }

            byte[] bytes = stream.readAllBytes();

            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        }
        catch (Exception exception)
        {
            System.err.println("Failed to load resource: " + exception.getMessage());
        }

        return null;
    }

    @Override
    public @NotNull Class<?> getManagingClass()
    {
        return TextureAtlasManager.class;
    }

    @Override
    public @NotNull String getManagedItem()
    {
        return name;
    }

    public void uninitialize_NoOverride()
    {
        if (outputTexture != null)
            outputTexture.uninitialize_NoOverride();
    }

    public static @NotNull TextureAtlas create(@NotNull String name, @NotNull AssetPath localPath)
    {
        TextureAtlas result = new TextureAtlas();
        result.setName(name);
        result.setLocalDirectory(localPath);
        return result;
    }

    private static class ImageData
    {
        String name;
        int width;
        int height;
        byte[] pixels;
    }
}