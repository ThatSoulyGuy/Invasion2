package com.thatsoulyguy.invasion2.entity.entities;

import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.input.*;
import com.thatsoulyguy.invasion2.math.Raycast;
import com.thatsoulyguy.invasion2.math.RaycastHit;
import com.thatsoulyguy.invasion2.math.Rigidbody;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Optional;

public class EntityPlayer extends Entity
{
    private Camera camera;

    private final float blockBreakCooldownTimerStart = 0.083f;
    private float blockBreakCooldownTimer;

    private final float jumpCooldownTimerStart = 0.18f;
    private float jumpCooldownTimer;

    @Override
    public void initialize()
    {
        super.initialize();

        jumpCooldownTimer = jumpCooldownTimerStart;
        blockBreakCooldownTimer = blockBreakCooldownTimerStart;

        getGameObject().addChild(GameObject.create("camera"));

        GameObject cameraObject = getGameObject().getChild("camera");

        cameraObject.addComponent(Camera.create(45.0f, 0.01f, 1000.0f));
        cameraObject.getTransform().setLocalPosition(new Vector3f(0.0f, 0.86f, 0.0f));

        camera = cameraObject.getComponent(Camera.class);

        InputManager.setMouseMode(MouseMode.LOCKED);
    }

    @Override
    public void update()
    {
        super.update();

        updateControls();
        updateMouselook();
        updateMovement();

        jumpCooldownTimer -= 0.01f;
        blockBreakCooldownTimer -= 0.01f;
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
        return 0.07f;
    }

    @Override
    public float getRunningSpeed()
    {
        return 0.2f;
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

    private void updateControls()
    {
        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED))
        {
            if (InputManager.getMouseMode() == MouseMode.LOCKED)
                InputManager.setMouseMode(MouseMode.FREE);
            else
                InputManager.setMouseMode(MouseMode.LOCKED);
        }

        if (InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.HELD) && blockBreakCooldownTimer <= 0)
        {
            Optional<RaycastHit> hit = Raycast.cast(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 10, getGameObject().getComponent(BoxCollider.class));

            if (hit.isPresent())
            {
                Vector3f point = hit.get().getPosition();
                Vector3f direction = camera.getGameObject().getTransform().getForward();
                Collider collider = hit.get().getCollider();

                point.add(direction.mul(0.5f, new Vector3f()));

                if (collider instanceof VoxelMeshCollider)
                {
                    World.getLocalWorld().setBlock(point, BlockRegistry.BLOCK_AIR.getID());
                    blockBreakCooldownTimer = blockBreakCooldownTimerStart;
                }
            }
        }
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

        float movementSpeed = getWalkingSpeed();

        Vector3f position = new Vector3f(getGameObject().getTransform().getLocalPosition());

        Vector3f forward = new Vector3f(camera.getGameObject().getTransform().getForward());
        Vector3f right = new Vector3f(camera.getGameObject().getTransform().getRight());

        forward.y = 0;
        right.y = 0;

        if (InputManager.getKeyState(KeyCode.W, KeyState.HELD))
            position.add(forward.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.S, KeyState.HELD))
            position.add(forward.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.A, KeyState.HELD))
            position.add(right.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.D, KeyState.HELD))
            position.add(right.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.SPACE, KeyState.HELD) && rigidbody.isGrounded() && jumpCooldownTimer <= 0)
        {
            rigidbody.addForce(new Vector3f(0.0f, 5.6f, 0.0f));
            jumpCooldownTimer = jumpCooldownTimerStart;
        }

        getGameObject().getTransform().setLocalPosition(position);
    }
}