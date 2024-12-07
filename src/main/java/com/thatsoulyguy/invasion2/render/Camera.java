package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@CustomConstructor("create")
public class Camera extends Component
{
    private float fieldOfView;
    private float nearPlane;
    private float farPlane;

    private Camera() { }

    public @NotNull Matrix4f getProjectionMatrix()
    {
        return new Matrix4f().perspective((float) Math.toRadians(fieldOfView), Window.getDimensions().x / (float)Window.getDimensions().y, nearPlane, farPlane);
    }

    public @NotNull Matrix4f getViewMatrix()
    {
        return new Matrix4f().lookAt(getGameObject().getTransform().getWorldPosition(), getGameObject().getTransform().getWorldPosition().add(getGameObject().getTransform().getForward(), new Vector3f()), new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public static @NotNull Camera create(float fieldOfView, float nearPlane, float farPlane)
    {
        Camera result = new Camera();

        result.fieldOfView = fieldOfView;
        result.nearPlane = nearPlane;
        result.farPlane = farPlane;

        return result;
    }
}