package com.thatsoulyguy.invasion2.ui.uielements;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.Texture;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.ui.UIElement;
import com.thatsoulyguy.invasion2.util.AssetPath;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class TextUIElement extends UIElement
{
    private @EffectivelyNotNull String text;
    private @EffectivelyNotNull AssetPath fontPath;
    private int alignmentFlags;
    private int fontSize;

    public int getAlignmentFlags()
    {
        return alignmentFlags;
    }

    public void setAlignment(TextAlignment... alignments)
    {
        int combined = 0;

        for (TextAlignment alignment : alignments)
            combined |= alignment.getMask();

        this.alignmentFlags = combined;
    }

    public void setText(@NotNull String text)
    {
        this.text = text;
    }

    public @NotNull String getText()
    {
        return text;
    }

    public @NotNull AssetPath getFontPath()
    {
        return fontPath;
    }

    public void setFontPath(@NotNull AssetPath fontPath)
    {
        this.fontPath = fontPath;
    }

    public int getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    public void build()
    {
        Vector2i dimensions = new Vector2i((int) getDimensions().x, (int) getDimensions().y);

        Font customFont;

        try (InputStream fontStream = TextUIElement.class.getResourceAsStream(fontPath.getFullPath()))
        {
            if (fontStream == null)
                throw new IllegalArgumentException("Font file not found in classpath: " + fontPath.getFullPath());

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            customFont = baseFont.deriveFont((float) fontSize);
        }
        catch (FontFormatException | IOException e)
        {
            throw new RuntimeException("Could not load font from classpath: " + fontPath.getFullPath(), e);
        }

        BufferedImage textImage = new BufferedImage(dimensions.x, dimensions.y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = textImage.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dimensions.x, dimensions.y);

        graphics.setFont(customFont);
        FontMetrics fm = graphics.getFontMetrics();

        String[] lines = text.split("\n");

        int lineHeight = fm.getHeight();
        int totalTextHeight = lines.length * lineHeight;

        int startY;

        if ((alignmentFlags & TextAlignment.VERTICAL_CENTER.getMask()) != 0)
            startY = (dimensions.y - totalTextHeight) / 2 + fm.getAscent();
        else if ((alignmentFlags & TextAlignment.VERTICAL_BOTTOM.getMask()) != 0)
            startY = dimensions.y - totalTextHeight + fm.getAscent();
        else
            startY = fm.getAscent();


        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            int lineWidth = fm.stringWidth(line);

            int xPos;
            if ((alignmentFlags & TextAlignment.HORIZONTAL_CENTER.getMask()) != 0)
                xPos = (dimensions.x - lineWidth) / 2;
            else if ((alignmentFlags & TextAlignment.HORIZONTAL_RIGHT.getMask()) != 0)
                xPos = dimensions.x - lineWidth;
            else
                xPos = 0;

            int yPos = startY + i * lineHeight;

            graphics.setColor(Color.WHITE);
            graphics.drawString(line, xPos, yPos);
        }

        graphics.dispose();

        int[] pixels = new int[dimensions.x * dimensions.y];
        textImage.getRGB(0, 0, dimensions.x, dimensions.y, pixels, 0, dimensions.x);

        ByteBuffer buffer = ByteBuffer.allocateDirect(dimensions.x * dimensions.y * 4);

        for (int y = 0; y < dimensions.y; y++)
        {
            for (int x = 0; x < dimensions.x; x++)
            {
                int pixel = pixels[y * dimensions.x + x];

                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                buffer.put((byte) red);
                buffer.put((byte) green);
                buffer.put((byte) blue);
                buffer.put((byte) alpha);
            }
        }

        buffer.flip();

        Texture rawTexture = Texture.create(text + "_texture", Texture.Filter.LINEAR, Texture.Wrapping.REPEAT, dimensions.x, dimensions.y, buffer);

        object.setComponent(rawTexture);
    }


    @Override
    public void generate(@NotNull GameObject object)
    {
        object.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        object.addComponent(Objects.requireNonNull(TextureManager.get("white")));

        object.addComponent(Mesh.create(DEFAULT_VERTICES, DEFAULT_INDICES));

        object.getComponentNotNull(Mesh.class).setTransparent(true);
        object.getComponentNotNull(Mesh.class).onLoad();
    }

    public enum TextAlignment
    {
        HORIZONTAL_LEFT(1),
        HORIZONTAL_CENTER(1 << 1),
        HORIZONTAL_RIGHT(1 << 2),

        VERTICAL_TOP(1 << 3),
        VERTICAL_CENTER(1 << 4),
        VERTICAL_BOTTOM(1 << 5);

        private final int mask;

        TextAlignment(int mask)
        {
            this.mask = mask;
        }

        public int getMask()
        {
            return mask;
        }
    }
}
