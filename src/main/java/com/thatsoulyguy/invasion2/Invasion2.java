package com.thatsoulyguy.invasion2;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.render.*;
import com.thatsoulyguy.invasion2.util.AssetPath;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class Invasion2
{
    private @EffectivelyNotNull Mesh mesh;

    public void preInitialize()
    {
        Window.initialize("Invasion 2* (1.3.1)", new Vector2i(750, 450));

        ShaderManager.register(Shader.create("default", AssetPath.create("invasion2", "shader/default")));
        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/grass.png")));
    }

    public void initialize()
    {
        mesh = Mesh.create(Objects.requireNonNull(ShaderManager.get("default")), Objects.requireNonNull(TextureManager.get("debug")),
        List.of
        (
            Vertex.create(new Vector3f(-0.5f,  0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(0.0f, 0.0f)),
            Vertex.create(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(0.0f, 1.0f)),
            Vertex.create(new Vector3f( 0.5f, -0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(1.0f, 1.0f)),
            Vertex.create(new Vector3f( 0.5f,  0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(1.0f, 0.0f))
        ),
        List.of
        (
            0, 1, 2,
            2, 3, 0
        ));

        mesh.generate();
    }

    public void update()
    {

    }

    public void render()
    {
        Window.preRender();

        mesh.render();

        Window.postRender();
    }

    public void uninitialize()
    {
        mesh.uninitialize();

        ShaderManager.uninitialize();
        TextureManager.uninitialize();

        Window.uninitialize();
    }

    public static void main(String[] args)
    {
        Invasion2 instantiation = new Invasion2();

        instantiation.preInitialize();
        instantiation.initialize();

        while (!Window.shouldClose())
        {
            instantiation.update();
            instantiation.render();
        }

        instantiation.uninitialize();
    }
}