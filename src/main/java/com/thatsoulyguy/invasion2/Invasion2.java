package com.thatsoulyguy.invasion2;

import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.collider.handler.CollisionHandlerManager;
import com.thatsoulyguy.invasion2.collider.handler.CollisionResult;
import com.thatsoulyguy.invasion2.core.Settings;
import com.thatsoulyguy.invasion2.core.Time;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.entity.entities.EntityPlayer;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import com.thatsoulyguy.invasion2.math.Rigidbody;
import com.thatsoulyguy.invasion2.render.*;
import com.thatsoulyguy.invasion2.render.advanced.RenderPassManager;
import com.thatsoulyguy.invasion2.render.advanced.core.renderpasses.GeometryRenderPass;
import com.thatsoulyguy.invasion2.render.advanced.core.renderpasses.LevelRenderPass;
import com.thatsoulyguy.invasion2.render.advanced.ssao.renderpasses.SSAOBlurRenderPass;
import com.thatsoulyguy.invasion2.render.advanced.ssao.renderpasses.SSAOConcludingRenderPass;
import com.thatsoulyguy.invasion2.render.advanced.ssao.renderpasses.SSAORenderPass;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.system.Layer;
import com.thatsoulyguy.invasion2.system.LevelManager;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import com.thatsoulyguy.invasion2.ui.Menu;
import com.thatsoulyguy.invasion2.ui.MenuManager;
import com.thatsoulyguy.invasion2.ui.UIManager;
import com.thatsoulyguy.invasion2.ui.menus.CraftingTableMenu;
import com.thatsoulyguy.invasion2.ui.menus.InventoryMenu;
import com.thatsoulyguy.invasion2.ui.menus.PauseMenu;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.FileHelper;
import com.thatsoulyguy.invasion2.world.TerrainGenerator;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import com.thatsoulyguy.invasion2.world.TextureAtlasManager;
import com.thatsoulyguy.invasion2.world.World;
import com.thatsoulyguy.invasion2.world.terraingenerators.CaveTerrainGenerator;
import com.thatsoulyguy.invasion2.world.terraingenerators.GroundTerrainGenerator;
import com.thatsoulyguy.invasion2.world.terraingenerators.TreeTerrainGenerator;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Invasion2
{
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

        Window.initialize("Invasion 2* (1.38.10)", windowSize);

        DebugRenderer.initialize();

        ShaderManager.register(Shader.create("legacy.default", AssetPath.create("invasion2", "shader/legacy/default")));
        ShaderManager.register(Shader.create("ui", AssetPath.create("invasion2", "shader/ui")));
        ShaderManager.register(Shader.create("pass.passthrough", AssetPath.create("invasion2", "shader/pass/passthrough")));
        ShaderManager.register(Shader.create("pass.geometry", AssetPath.create("invasion2", "shader/pass/geometry")));
        ShaderManager.register(Shader.create("ssao.default", AssetPath.create("invasion2", "shader/ssao/default")));
        ShaderManager.register(Shader.create("ssao.blur", AssetPath.create("invasion2", "shader/ssao/blur")));
        ShaderManager.register(Shader.create("ssao.conclusion", AssetPath.create("invasion2", "shader/ssao/conclusion")));

        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/debug.png")));
        TextureManager.register(Texture.create("error", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/error.png")));
        TextureManager.register(Texture.create("ui.hotbar", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/hotbar.png")));
        TextureManager.register(Texture.create("ui.hotbar_selector", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/hotbar_selector.png")));
        TextureManager.register(Texture.create("ui.transparency", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/transparency.png")));
        TextureManager.register(Texture.create("ui.background", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/background.png")));
        TextureManager.register(Texture.create("ui.button_default", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/button_default.png")));
        TextureManager.register(Texture.create("ui.button_disabled", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/button_disabled.png")));
        TextureManager.register(Texture.create("ui.button_selected", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("invasion2", "texture/ui/button_selected.png")));
        TextureManager.register(Texture.create("ui.menu.survival_inventory", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("invasion2", "texture/ui/menu/inventory_survival.png")));
        TextureManager.register(Texture.create("ui.menu.crafting_table", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("invasion2", "texture/ui/menu/crafting_table.png")));
        TextureManager.register(Texture.create("ui.menu.slot_darken", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("invasion2", "texture/ui/menu/slot_darken.png")));

        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("invasion2", "texture/block/")));
        TextureAtlasManager.register(TextureAtlas.create("items", AssetPath.create("invasion2", "texture/item/")));

        MenuManager.register(Menu.create(CraftingTableMenu.class));
        MenuManager.register(Menu.create(InventoryMenu.class));
        MenuManager.register(Menu.create(PauseMenu.class));


        LevelRenderPass levelRenderPass = new LevelRenderPass();

        RenderPassManager.register(levelRenderPass);

        GeometryRenderPass geometryPass = new GeometryRenderPass();

        RenderPassManager.register(geometryPass);


        SSAORenderPass ssaoPass = new SSAORenderPass(
                geometryPass.getPositionTex(),
                geometryPass.getNormalTex()
        );

        RenderPassManager.register(ssaoPass);


        SSAOBlurRenderPass ssaoBlurPass = new SSAOBlurRenderPass(ssaoPass.getSSAOColor());

        RenderPassManager.register(ssaoBlurPass);


        SSAOConcludingRenderPass concludingPass = new SSAOConcludingRenderPass(
                geometryPass.getPositionTex(),
                geometryPass.getNormalTex(),
                geometryPass.getAlbedoTex(),
                ssaoBlurPass.getBlurredSSAO()
        );

        RenderPassManager.register(concludingPass);


        Settings.initialize();

        BlockRegistry.initialize();
        ItemRegistry.initialize();
        CraftingRecipeRegistry.initialize();

        registerCollisionHandlers();

        InputManager.update();

        UIManager.initialize();

        Time.reset();
    }

    public void initialize()
    {
        //LevelManager.loadLevel(FileHelper.getPersistentDataPath("Invasion2") + "/overworld", true);

        //*
        LevelManager.createLevel("overworld", true);

        GameObject player = GameObject.create("default.player", Layer.DEFAULT);

        player.getTransform().setLocalPosition(new Vector3f(0.0f, 180.0f, 0.0f));

        player.addComponent(Collider.create(BoxCollider.class).setSize(new Vector3f(0.65f, 1.89f, 0.65f)));
        player.addComponent(Rigidbody.create());
        player.addComponent(Entity.create(EntityPlayer.class));

        GameObject overworld = GameObject.create("default.world", Layer.DEFAULT);

        overworld.addComponent(World.create("overworld"));

        World world = overworld.getComponentNotNull(World.class);

        world.addTerrainGenerator(TerrainGenerator.create(GroundTerrainGenerator.class));
        world.addTerrainGenerator(TerrainGenerator.create(CaveTerrainGenerator.class));
        world.addTerrainGenerator(TerrainGenerator.create(TreeTerrainGenerator.class));
        //*/
    }

    public void update()
    {
        Time.update();

        World.getLocalWorld().chunkLoader = Objects.requireNonNull(GameObjectManager.get("default.player")).getTransform();

        GameObjectManager.updateMainThread();
        GameObjectManager.update();
        UIManager.update();

        MainThreadExecutor.execute();

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.renderDefault(Objects.requireNonNull(GameObjectManager.get("default.player")).getComponentNotNull(EntityPlayer.class).getCamera());

        GameObjectManager.renderUI();

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

            for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++)
            {
                Vector3f resolution = Collider.resolveAllCollisions(box, voxelMesh);

                if (resolution.length() < 0.00001f)
                    break;

                totalResolution.add(resolution);
            }

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

        RenderPassManager.uninitialize();

        InputManager.uninitialize();

        ShaderManager.uninitialize();
        TextureManager.uninitialize();

        DebugRenderer.uninitialize();

        Window.uninitialize();
    }

    public static void main(String[] args)
    {
        try
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
        catch (Exception exception)
        {
            String stackTrace = Arrays.stream(exception.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));

            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage() + "\n\n" + stackTrace,
                    "Exception!",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(-1);
        }
    }
}