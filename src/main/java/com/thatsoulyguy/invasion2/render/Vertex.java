package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

@CustomConstructor("create")
public class Vertex
{
    private @EffectivelyNotNull Vector3f position;
    private @EffectivelyNotNull Vector3f color;
    private @EffectivelyNotNull Vector3f normal;
    private @EffectivelyNotNull Vector2f uvs;

    private Vertex() { }

    public @NotNull Vector3f getPosition()
    {
        return position;
    }

    public @NotNull Vector3f getColor()
    {
        return color;
    }

    public @NotNull Vector3f getNormal()
    {
        return normal;
    }

    public @NotNull Vector2f getUVs()
    {
        return uvs;
    }

    public static @NotNull Vertex create(Vector3f position, Vector3f color, Vector3f normal, Vector2f uvs)
    {
        Vertex result = new Vertex();

        result.position = position;
        result.color = color;
        result.normal = normal;
        result.uvs = uvs;

        return result;
    }
}