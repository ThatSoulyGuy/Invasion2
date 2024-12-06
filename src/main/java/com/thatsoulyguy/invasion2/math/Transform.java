package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@CustomConstructor("create")
public class Transform extends Component
{
    private @EffectivelyNotNull Vector3f position;
    private @EffectivelyNotNull Vector3f rotation;
    private @EffectivelyNotNull Vector3f scale;

    private @Nullable Transform parent;

    private Transform() { }

    public void translate(@NotNull Vector3f translation)
    {
        position.add(new Vector3f(translation));
    }

    public void rotate(@NotNull Vector3f rotation)
    {
        this.rotation.add(new Vector3f(rotation));
    }

    public void scale(@NotNull Vector3f scale)
    {
        this.scale.add(new Vector3f(scale));
    }

    public @NotNull Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(@NotNull Vector3f position)
    {
        this.position = position;
    }

    public @NotNull Vector3f getRotation()
    {
        return rotation;
    }

    public void setRotation(@NotNull Vector3f rotation)
    {
        this.rotation = rotation;
    }

    public @NotNull Vector3f getScale()
    {
        return scale;
    }

    public void setScale(@NotNull Vector3f scale)
    {
        this.scale = scale;
    }

    public @Nullable Transform getParent()
    {
        return parent;
    }

    public void setParent(@Nullable Transform parent)
    {
        this.parent = parent;
    }

    public @NotNull Matrix4f getModelMatrix()
    {
        Matrix4f localMatrix = new Matrix4f()
                .translation(position)
                .rotateXYZ(rotation.x, rotation.y, rotation.z)
                .scale(scale);

        if (parent != null)
        {
            Matrix4f parentMatrix = parent.getModelMatrix();

            parentMatrix.mulLocal(localMatrix);

            return parentMatrix;
        }
        else
            return localMatrix;
    }

    @Override
    public String toString()
    {
        return "\nPosition: [" + position.x + ", " + position.y + ", " + position.z + "]\n" + "Rotation: [" + rotation.x + ", " + rotation.y + ", " + rotation.z + "]\n" + "Scale: [" + scale.x + ", " + scale.y + ", " + scale.z + "]";
    }

    public static @NotNull Transform create(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale)
    {
        Transform result = new Transform();

        result.position = new Vector3f(position);
        result.rotation = new Vector3f(rotation);
        result.scale = new Vector3f(scale);

        return result;
    }
}