package com.thatsoulyguy.invasion2.entity.entities;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.core.Time;
import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.input.*;
import com.thatsoulyguy.invasion2.item.Item;
import com.thatsoulyguy.invasion2.item.ItemRegistry;
import com.thatsoulyguy.invasion2.math.Raycast;
import com.thatsoulyguy.invasion2.math.Rigidbody;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.DebugRenderer;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.Layer;
import com.thatsoulyguy.invasion2.ui.Menu;
import com.thatsoulyguy.invasion2.ui.menus.InventoryMenu;
import com.thatsoulyguy.invasion2.util.CoordinateHelper;
import com.thatsoulyguy.invasion2.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Objects;

public class EntityPlayer extends Entity
{
    private @EffectivelyNotNull Camera camera;

    private @EffectivelyNotNull Vector3i breakingBlockCoordinates;

    private @EffectivelyNotNull InventoryMenu inventoryMenu;

    private float breakingProgress;

    private final float blockBreakCooldownTimerStart = 0.083f;
    private float blockBreakCooldownTimer;

    private final float jumpCooldownTimerStart = 0.18f;
    private float jumpCooldownTimer;

    @Override
    public void initialize()
    {
        super.initialize();

        initializeCamera();
        initializeUI();
    }

    @Override
    public void updateMainThread()
    {
        super.updateMainThread();

        updateControls();
        updateMouselook();
        updateMovement();

        inventoryMenu.update();

        jumpCooldownTimer -= Time.getDeltaTime();
        blockBreakCooldownTimer -= Time.getDeltaTime();
    }

    private void initializeCamera()
    {
        jumpCooldownTimer = jumpCooldownTimerStart;
        blockBreakCooldownTimer = blockBreakCooldownTimerStart;

        getGameObject().addChild(GameObject.create("default.camera", Layer.DEFAULT));

        GameObject cameraObject = getGameObject().getChild("default.camera");

        cameraObject.addComponent(Camera.create(45.0f, 0.01f, 1000.0f));
        cameraObject.getTransform().setLocalPosition(new Vector3f(0.0f, 0.86f, 0.0f));

        camera = cameraObject.getComponent(Camera.class);

        InputManager.setMouseMode(MouseMode.LOCKED);
    }

    private void initializeUI()
    {
        inventoryMenu = Menu.create(InventoryMenu.class);
    }

    private void updateControls()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED))
        {
            if (InputManager.getMouseMode() == MouseMode.LOCKED)
                InputManager.setMouseMode(MouseMode.FREE);
            else
                InputManager.setMouseMode(MouseMode.LOCKED);
        }

        Raycast.VoxelHit hit = Raycast.castVoxel(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 4);

        if (hit != null)
        {
            {
                Vector3f point = hit.center();

                short block = World.getLocalWorld().getBlock(point);

                if (block != BlockRegistry.BLOCK_AIR.getId() && block != -1)
                {
                    Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(point);
                    Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(point);

                    Vector3f selectorSize = new Vector3f(1.0f);
                    Vector3f selectorPosition = CoordinateHelper.blockToWorldCoordinates(blockCoordinates, chunkCoordinates).add(new Vector3f(0.5f));

                    Vector3f selectorMin = selectorPosition.sub(selectorSize.mul(0.5f, new Vector3f()), new Vector3f());
                    Vector3f selectorMax = selectorPosition.add(selectorSize.mul(0.5f, new Vector3f()), new Vector3f());

                    DebugRenderer.addBox(selectorMin, selectorMax, new Vector3f(0.0f, 0.0f, 0.0f));
                }
            }

            if (InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.HELD))
            {
                Vector3f point = hit.center();

                short blockID = World.getLocalWorld().getBlock(point);

                if (blockID != BlockRegistry.BLOCK_AIR.getId() && blockID != -1)
                {
                    Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(point);

                    if (breakingBlockCoordinates == null || !breakingBlockCoordinates.equals(blockCoordinates))
                    {
                        breakingBlockCoordinates = blockCoordinates;
                        breakingProgress = 0;
                    }

                    float blockHardness = Objects.requireNonNull(BlockRegistry.get(blockID)).getHardness();
                    breakingProgress += Time.getDeltaTime();

                    if (breakingProgress >= blockHardness)
                    {
                        inventoryMenu.addItem(Objects.requireNonNull(BlockRegistry.get(World.getLocalWorld().getBlock(point))).getAssociatedItem().getId(), (byte) 1);

                        World.getLocalWorld().setBlock(point, BlockRegistry.BLOCK_AIR.getId());

                        breakingProgress = 0;
                        breakingBlockCoordinates = null;
                        blockBreakCooldownTimer = blockBreakCooldownTimerStart;
                    }
                }
                else
                {
                    breakingBlockCoordinates = null;
                    breakingProgress = 0;
                }
            }

            if (InputManager.getMouseState(MouseCode.MOUSE_RIGHT, MouseState.PRESSED))
            {
                InventoryMenu.SlotData slot = inventoryMenu.getSlot(new Vector2i(0, inventoryMenu.currentSlotSelected));

                if (slot == null)
                    return;

                Item item = ItemRegistry.get(slot.id());

                if (item == null)
                {
                    System.err.println("Invalid item detected! (This shouldn't happen!)");
                    return;
                }

                if (slot.count() <= 0 || !item.isBlockItem())
                    return;

                Vector3f point = hit.center();
                Vector3f normal = hit.normal();

                point.add(normal.mul(1f, new Vector3f()));

                short currentBlock = World.getLocalWorld().getBlock(point);

                if (currentBlock == -1 || currentBlock == BlockRegistry.BLOCK_AIR.getId())
                {
                    World.getLocalWorld().setBlock(point, item.getAssociatedBlock().getId());

                    inventoryMenu.setSlot(new Vector2i(0, inventoryMenu.currentSlotSelected), item.getAssociatedBlock().getId(), (byte) (slot.count() - 1));
                }
            }
        }
        else
        {
            breakingBlockCoordinates = null;
            breakingProgress = 0;
        }

        if (InputManager.getScrollDelta() > 0)
            inventoryMenu.currentSlotSelected--;

        if (InputManager.getScrollDelta() < 0)
            inventoryMenu.currentSlotSelected++;
    }

    private void updateMouselook()
    {
        Vector2f mouseDelta = InputManager.getMouseDelta();

        Vector3f rotation = new Vector3f(camera.getGameObject().getTransform().getLocalRotation());

        float mouseSensitivity = 0.1f;

        rotation.y += -mouseDelta.x * mouseSensitivity;
        rotation.x += -mouseDelta.y * mouseSensitivity;

        float maxPitch = 89.9f;

        if (rotation.x > maxPitch)
            rotation.x = maxPitch;
        else if (rotation.x < -maxPitch)
            rotation.x = -maxPitch;

        camera.getGameObject().getTransform().setLocalRotation(rotation);
    }

    private void updateMovement()
    {
        Rigidbody rigidbody = getGameObject().getComponent(Rigidbody.class);

        if (rigidbody == null)
        {
            System.err.println("No RigidBody component found on game object: '" + getGameObject().getName() + "'!");
            return;
        }

        float movementSpeed = getWalkingSpeed() * Time.getDeltaTime();

        if (InputManager.getKeyState(KeyCode.LEFT_SHIFT, KeyState.HELD))
            movementSpeed = getRunningSpeed() * Time.getDeltaTime();

        Vector3f movement = new Vector3f();

        Vector3f forward = new Vector3f(camera.getGameObject().getTransform().getForward());
        Vector3f right = new Vector3f(camera.getGameObject().getTransform().getRight());

        forward.y = 0;
        right.y = 0;

        forward.normalize();
        right.normalize();

        if (InputManager.getKeyState(KeyCode.W, KeyState.HELD))
            movement.add(forward.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.S, KeyState.HELD))
            movement.add(forward.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.A, KeyState.HELD))
            movement.add(right.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.D, KeyState.HELD))
            movement.add(right.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.SPACE, KeyState.HELD) && rigidbody.isGrounded() && jumpCooldownTimer <= 0)
        {
            rigidbody.addForce(new Vector3f(0.0f, 5.5f, 0.0f));
            jumpCooldownTimer = jumpCooldownTimerStart;
        }

        rigidbody.addForce(movement);
    }

    @Override
    public String getDisplayName()
    {
        return "Player**";
    }

    @Override
    public String getRegistryName()
    {
        return "entity_player";
    }

    @Override
    public float getWalkingSpeed()
    {
        return 50.0f;
    }

    @Override
    public float getRunningSpeed()
    {
        return 65.0f;
    }

    @Override
    public float getMaximumHealth()
    {
        return 100;
    }

    public @NotNull Camera getCamera()
    {
        return camera;
    }
}