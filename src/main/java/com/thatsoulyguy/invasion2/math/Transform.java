package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CustomConstructor("create")
public class Transform extends Component
{
    private @EffectivelyNotNull Vector3f localPosition;
    private @EffectivelyNotNull Vector3f localRotation;
    private @EffectivelyNotNull Vector3f localScale;

    private @Nullable Transform parent;
    private final List<Transform> children = Collections.synchronizedList(new ArrayList<>());

    private Transform() { }

    public void translate(@NotNull Vector3f translation)
    {
        localPosition.add(translation);
    }

    public void rotate(@NotNull Vector3f rotation)
    {
        this.localRotation.add(rotation);
    }

    public void scale(@NotNull Vector3f scale)
    {
        this.localScale.add(scale);
    }

    public @NotNull Vector3f getLocalPosition()
    {
        return new Vector3f(localPosition);
    }

    public void setLocalPosition(@NotNull Vector3f localPosition)
    {
        this.localPosition = new Vector3f(localPosition);
    }

    public @NotNull Vector3f getLocalRotation()
    {
        return new Vector3f(localRotation);
    }

    public void setLocalRotation(@NotNull Vector3f localRotation)
    {
        this.localRotation = new Vector3f(localRotation);
    }

    public @NotNull Vector3f getLocalScale()
    {
        return new Vector3f(localScale);
    }

    public void setLocalScale(@NotNull Vector3f localScale)
    {
        this.localScale = new Vector3f(localScale);
    }

    public @Nullable Transform getParent()
    {
        return parent;
    }

    public void setParent(@Nullable Transform parent)
    {
        if (this.parent != null)
            this.parent.children.remove(this);

        this.parent = parent;

        if (parent != null && !parent.children.contains(this))
            parent.children.add(this);
    }

    public @NotNull List<Transform> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public @NotNull Vector3f getForward()
    {
        return getModelMatrix().getColumn(2, new Vector3f()).negate().normalize();
    }

    public @NotNull Vector3f getRight()
    {
        return getModelMatrix().getColumn(0, new Vector3f()).normalize();
    }

    public @NotNull Vector3f getUp()
    {
        return getModelMatrix().getColumn(1, new Vector3f()).normalize();
    }

    public @NotNull Vector3f getWorldPosition()
    {
        return getModelMatrix().getTranslation(new Vector3f());
    }

    public @NotNull Vector3f getWorldRotation()
    {
        Quaternionf rotation = getModelMatrix().getNormalizedRotation(new Quaternionf());

        Vector3f eulerAngles = new Vector3f();

        rotation.getEulerAnglesXYZ(eulerAngles);

        return eulerAngles.mul((float) Math.toDegrees(1.0));
    }

    public @NotNull Vector3f getWorldScale()
    {
        return getModelMatrix().getScale(new Vector3f());
    }

    public @NotNull Matrix4f getModelMatrix()
    {
        float rx = (float) Math.toRadians(localRotation.x);
        float ry = (float) Math.toRadians(localRotation.y);
        float rz = (float) Math.toRadians(localRotation.z);

        Matrix4f localMatrix = new Matrix4f()
                .identity()
                .scale(localScale)
                .rotateY(ry)
                .rotateX(rx)
                .rotateZ(rz)
                .translate(localPosition);

        if (parent != null)
            return parent.getModelMatrix().mul(localMatrix, new Matrix4f());
        else
            return localMatrix;
    }

    @Override
    public String toString()
    {
        return "\nPosition: [" + localPosition.x + ", " + localPosition.y + ", " + localPosition.z + "]\n" +
                "Rotation: [" + localRotation.x + ", " + localRotation.y + ", " + localRotation.z + "]\n" +
                "Scale: [" + localScale.x + ", " + localScale.y + ", " + localScale.z + "]";
    }

    public static @NotNull Transform create(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale)
    {
        Transform result = new Transform();

        result.localPosition = new Vector3f(position);
        result.localRotation = new Vector3f(rotation);
        result.localScale = new Vector3f(scale);

        return result;
    }
}