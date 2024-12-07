package com.thatsoulyguy.invasion2.entity.entities;

import com.thatsoulyguy.invasion2.entity.Entity;
import com.thatsoulyguy.invasion2.input.InputManager;
import com.thatsoulyguy.invasion2.input.KeyCode;
import com.thatsoulyguy.invasion2.input.KeyState;
import com.thatsoulyguy.invasion2.input.MouseMode;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.system.GameObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class EntityPlayer extends Entity
{
    private Camera camera;

    @Override
    public void initialize()
    {
        super.initialize();

        getGameObject().addChild(GameObject.create("camera"));

        GameObject cameraObject = getGameObject().getChild("camera");

        cameraObject.addComponent(Camera.create(45.0f, 0.01f, 1000.0f));

        camera = cameraObject.getComponent(Camera.class);

        InputManager.setMouseMode(MouseMode.LOCKED);
    }

    @Override
    public void update()
    {
        super.update();

        updateMouselook();
        updateMovement();
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
        return 0.36f;
    }

    @Override
    public float getRunningSpeed()
    {
        return 0.446f;
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
        float movementSpeed = getWalkingSpeed();

        Vector3f position = new Vector3f(getGameObject().getTransform().getLocalPosition());

        Vector3f forward = new Vector3f(camera.getGameObject().getTransform().getForward());
        Vector3f right = new Vector3f(camera.getGameObject().getTransform().getRight());

        if (InputManager.getKeyState(KeyCode.W, KeyState.HELD))
            position.add(forward.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.S, KeyState.HELD))
            position.add(forward.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.A, KeyState.HELD))
            position.add(right.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.D, KeyState.HELD))
            position.add(right.mul(movementSpeed, new Vector3f()));

        getGameObject().getTransform().setLocalPosition(position);
    }
}