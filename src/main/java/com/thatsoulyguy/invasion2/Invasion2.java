package com.thatsoulyguy.invasion2;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.entity.entities.EntityPlayer;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.render.*;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.system.LevelManager;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.FileHelper;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import com.thatsoulyguy.invasion2.world.TextureAtlasManager;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Invasion2
{
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private @EffectivelyNotNull GameObject player;
    private @EffectivelyNotNull GameObject chunk;

    public void preInitialize()
    {
        InputManager.initialize();

        double dpi = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f;
        int screenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * dpi);
        int screenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * dpi);

        int windowWidth = screenWidth / 2;
        int windowHeight = screenHeight / 2;

        Vector2i windowSize = new Vector2i(windowWidth, windowHeight);

        Window.initialize("Invasion 2* (1.17.4r2)", windowSize);

        ShaderManager.register(Shader.create("default", AssetPath.create("invasion2", "shader/default")));
        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/debug.png")));
        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("invasion2", "texture/block/")));

        InputManager.update();
    }

    public void initialize()
    {
        //LevelManager.loadLevel(FileHelper.getPersistentDataPath("Invasion2") + "/overworld");

        ///*
        LevelManager.createLevel("overworld", true);

        player = GameObject.create("player");

        player.addComponent(Entity.create(EntityPlayer.class));

        chunk = GameObject.create("chunk");

        chunk.addComponent(Objects.requireNonNull(ShaderManager.get("default")));
        chunk.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        Objects.requireNonNull(chunk.getComponent(TextureAtlas.class)).onLoad();

        chunk.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        chunk.addComponent(Chunk.create());

        Objects.requireNonNull(chunk.getComponent(Chunk.class)).onLoad();
        //*/
    }

    public void update()
    {
        GameObjectManager.getAll().forEach(gameObject -> executor.submit(gameObject::update));

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.getAll().forEach((gameObject) -> gameObject.render(Objects.requireNonNull(Objects.requireNonNull(GameObjectManager.get("player")).getComponent(EntityPlayer.class)).getCamera()));

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