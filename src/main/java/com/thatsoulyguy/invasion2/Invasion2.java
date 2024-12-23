package com.thatsoulyguy.invasion2;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.collider.handler.CollisionHandlerManager;
import com.thatsoulyguy.invasion2.collider.handler.CollisionResult;
import com.thatsoulyguy.invasion2.core.Time;
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
import java.util.concurrent.Callable;
import java.util.function.Function;

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

        Window.initialize("Invasion 2* (1.29.7)", windowSize);

        DebugRenderer.initialize();

        ShaderManager.register(Shader.create("default", AssetPath.create("invasion2", "shader/default")));
        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/debug.png")));
        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("invasion2", "texture/block/")));

        BlockRegistry.initialize();

        registerCollisionHandlers();

        InputManager.update();

        Time.reset();
    }

    public void initialize()
    {
        //LevelManager.loadLevel(FileHelper.getPersistentDataPath("Invasion2") + "/overworld", true);

        ///*
        LevelManager.createLevel("overworld", true);

        player = GameObject.create("player");

        player.getTransform().setLocalPosition(new Vector3f(0.0f, 180.0f, 0.0f));

        player.addComponent(BoxCollider.create(new Vector3f(0.65f, 1.89f, 0.65f)));
        player.addComponent(Rigidbody.create());
        player.addComponent(Entity.create(EntityPlayer.class));

        world = GameObject.create("world");

        world.addComponent(World.create("overworld"));
        //*/
    }

    public void update()
    {
        Time.update();

        World.getLocalWorld().chunkLoader = Objects.requireNonNull(GameObjectManager.get("player")).getTransform();

        GameObjectManager.update();

        MainThreadExecutor.execute();

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.render(Objects.requireNonNull(GameObjectManager.get("player")).getComponentNotNull(EntityPlayer.class).getCamera());
        DebugRenderer.render(Objects.requireNonNull(GameObjectManager.get("player")).getComponentNotNull(EntityPlayer.class).getCamera());

        Window.postRender();
    }

    private void registerCollisionHandlers()
    {
        CollisionHandlerManager.register(BoxCollider.class, BoxCollider.class, (a, b, selfIsMovable) ->
        {
            BoxCollider boxA = (BoxCollider) a;
            BoxCollider boxB = (BoxCollider) b;

            Vector3f posA = boxA.getPosition();
            Vector3f sizeA = boxA.getSize();
            Vector3f posB = boxB.getPosition();
            Vector3f sizeB = boxB.getSize();

            Vector3f minA = new Vector3f(posA).sub(new Vector3f(sizeA).mul(0.5f));
            Vector3f maxA = new Vector3f(posA).add(new Vector3f(sizeA).mul(0.5f));

            Vector3f minB = new Vector3f(posB).sub(new Vector3f(sizeB).mul(0.5f));
            Vector3f maxB = new Vector3f(posB).add(new Vector3f(sizeB).mul(0.5f));

            boolean intersects = Collider.intersectsGeneric(minA, maxA, minB, maxB);

            Vector3f resolution = new Vector3f();

            if (intersects)
            {
                float overlapX = Math.min(maxA.x - minB.x, maxB.x - minA.x);
                float overlapY = Math.min(maxA.y - minB.y, maxB.y - minA.y);
                float overlapZ = Math.min(maxA.z - minB.z, maxB.z - minA.z);

                float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));

                if (minOverlap == overlapX)
                    resolution.x = (posA.x < posB.x) ? -overlapX : overlapX;
                else if (minOverlap == overlapY)
                    resolution.y = (posA.y < posB.y) ? -overlapY : overlapY;
                else
                    resolution.z = (posA.z < posB.z) ? -overlapZ : overlapZ;

                if (!selfIsMovable)
                    resolution.negate();
            }

            return new CollisionResult(intersects, resolution);
        });

        CollisionHandlerManager.register(BoxCollider.class, VoxelMeshCollider.class, (a, b, selfIsMovable) ->
        {
            BoxCollider box = (BoxCollider) a;
            VoxelMeshCollider voxelMesh = (VoxelMeshCollider) b;

            Vector3f totalResolution = new Vector3f();

            final int MAX_ITERATIONS = 10;

            Vector3f resolutionX = Collider.resolveAxisCollisions(box, voxelMesh, "x", MAX_ITERATIONS);
            totalResolution.add(resolutionX);

            Vector3f resolutionY = Collider.resolveAxisCollisions(box, voxelMesh, "y", MAX_ITERATIONS);
            totalResolution.add(resolutionY);

            Vector3f resolutionZ = Collider.resolveAxisCollisions(box, voxelMesh, "z", MAX_ITERATIONS);
            totalResolution.add(resolutionZ);

            boolean collided = totalResolution.length() > 0.00001f;

            return new CollisionResult(collided, totalResolution);
        });

        CollisionHandlerManager.register(VoxelMeshCollider.class, VoxelMeshCollider.class, (a, b, selfIsMovable) -> //TODO: Performance-intensive; Optimize this
        {
            VoxelMeshCollider voxelMeshA = (VoxelMeshCollider) a;
            VoxelMeshCollider voxelMeshB = (VoxelMeshCollider) b;

            Vector3f posA = voxelMeshA.getPosition();
            Vector3f posB = voxelMeshB.getPosition();

            Vector3f totalResolution = new Vector3f();
            boolean intersects = false;

            for (Vector3f voxelA : voxelMeshA.getVoxels())
            {
                Vector3f voxelWorldPosA = new Vector3f(posA).add(voxelA);
                Vector3f voxelMinA = new Vector3f(voxelWorldPosA).sub(0.5f, 0.5f, 0.5f);
                Vector3f voxelMaxA = new Vector3f(voxelWorldPosA).add(0.5f, 0.5f, 0.5f);

                for (Vector3f voxelB : voxelMeshB.getVoxels())
                {
                    Vector3f voxelWorldPosB = new Vector3f(posB).add(voxelB);
                    Vector3f voxelMinB = new Vector3f(voxelWorldPosB).sub(0.5f, 0.5f, 0.5f);
                    Vector3f voxelMaxB = new Vector3f(voxelWorldPosB).add(0.5f, 0.5f, 0.5f);

                    boolean voxelIntersects = Collider.intersectsGeneric(voxelMinA, voxelMaxA, voxelMinB, voxelMaxB);

                    if (voxelIntersects)
                    {
                        intersects = true;

                        float overlapX = Math.min(voxelMaxA.x - voxelMinB.x, voxelMaxB.x - voxelMinA.x);
                        float overlapY = Math.min(voxelMaxA.y - voxelMinB.y, voxelMaxB.y - voxelMinA.y);
                        float overlapZ = Math.min(voxelMaxA.z - voxelMinB.z, voxelMaxB.z - voxelMinA.z);

                        float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));

                        Vector3f resolution = new Vector3f();

                        if (minOverlap == overlapX)
                            resolution.x = (posA.x < posB.x) ? -overlapX : overlapX;
                        else if (minOverlap == overlapY)
                            resolution.y = (posA.y < posB.y) ? -overlapY : overlapY;
                        else
                            resolution.z = (posA.z < posB.z) ? -overlapZ : overlapZ;

                        totalResolution.add(resolution);
                    }
                }
            }

            if (intersects)
            {
                int intersectCount = voxelMeshA.getVoxels().size() * voxelMeshB.getVoxels().size();
                totalResolution.mul(1.0f / intersectCount);

                if (!selfIsMovable)
                    totalResolution.negate();
            }

            return new CollisionResult(intersects, totalResolution);
        });
    }

    public void uninitialize()
    {
        LevelManager.saveLevel("overworld", FileHelper.getPersistentDataPath("Invasion2"));

        GameObjectManager.uninitialize();

        InputManager.uninitialize();

        ShaderManager.uninitialize();
        TextureManager.uninitialize();

        DebugRenderer.uninitialize();

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