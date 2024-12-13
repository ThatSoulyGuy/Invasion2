package com.thatsoulyguy.invasion2;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.entity.entities.EntityPlayer;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.math.Rigidbody;
import com.thatsoulyguy.invasion2.render.*;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.system.LevelManager;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.FileHelper;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import com.thatsoulyguy.invasion2.world.TextureAtlasManager;
import com.thatsoulyguy.invasion2.world.World;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.Objects;

public class Invasion2
{
    private @EffectivelyNotNull GameObject player;
    private @EffectivelyNotNull GameObject world;

    public void preInitialize()
    {
        InputManager.initialize();

        if(!GLFW.glfwInit())
            throw new IllegalStateException("Failed to initialize GLFW");

        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();

        if (primaryMonitor == 0L)
            throw new RuntimeException("Failed to get primary monitor");

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primaryMonitor);

        if (vidMode == null)
            throw new RuntimeException("Failed to get video mode");

        int windowWidth = vidMode.width();
        int windowHeight = vidMode.height();

        Vector2i windowSize = new Vector2i(windowWidth / 2, windowHeight / 2);

        MainThreadExecutor.initialize();

        Window.initialize("Invasion 2* (1.24.5)", windowSize);

        ShaderManager.register(Shader.create("default", AssetPath.create("invasion2", "shader/default")));
        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/debug.png")));
        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("invasion2", "texture/block/")));

        BlockRegistry.initialize();

        InputManager.update();
    }

    public void initialize()
    {
        //LevelManager.loadLevel(FileHelper.getPersistentDataPath("Invasion2") + "/overworld");

        ///*
        LevelManager.createLevel("overworld", true);

        player = GameObject.create("player");

        player.getTransform().setLocalPosition(new Vector3f(0.0f, 180.0f, 0.0f));

        player.addComponent(BoxCollider.create());
        player.addComponent(Rigidbody.create());
        player.addComponent(Entity.create(EntityPlayer.class));

        world = GameObject.create("world");

        world.addComponent(World.create("overworld"));
        //*/
    }

    public void update()
    {
        Objects.requireNonNull(Objects.requireNonNull(GameObjectManager.get("world")).getComponent(World.class)).chunkLoader = Objects.requireNonNull(GameObjectManager.get("player")).getTransform();

        GameObjectManager.update();
        GameObjectManager.updateSingleThread();

        MainThreadExecutor.execute();

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.render(Objects.requireNonNull(Objects.requireNonNull(GameObjectManager.get("player")).getComponent(EntityPlayer.class)).getCamera());

        Window.postRender();
    }

    public void uninitialize()
    {
        LevelManager.saveLevel("overworld", FileHelper.getPersistentDataPath("Invasion2"));

        GameObjectManager.uninitialize();

        InputManager.uninitialize();

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