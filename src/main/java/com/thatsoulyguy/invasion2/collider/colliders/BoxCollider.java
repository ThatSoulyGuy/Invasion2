package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@CustomConstructor("create")
public class BoxCollider extends Collider
{
    private @EffectivelyNotNull Vector3f size;

    private BoxCollider() { }

    @Override
    public boolean intersects(@NotNull Collider other)
    {
        Vector3f selfHalfPosition = getPosition().div(2.0f);
        Vector3f selfMin = selfHalfPosition.sub(new Vector3f(size).div(2.0f), new Vector3f());
        Vector3f selfMax = selfHalfPosition.add(new Vector3f(size).div(2.0f), new Vector3f());

        if (other instanceof BoxCollider boxCollider)
        {
            Vector3f otherHalfPosition = boxCollider.getPosition().div(2.0f);
            Vector3f otherMin = otherHalfPosition.sub(new Vector3f(boxCollider.size).div(2.0f), new Vector3f());
            Vector3f otherMax = otherHalfPosition.add(new Vector3f(boxCollider.size).div(2.0f), new Vector3f());

            return Collider.intersectGeneric(selfMin, selfMax, otherMin, otherMax);
        }

        return false;
    }

    @Override
    public @Nullable Vector3f resolve(@NotNull Collider other)
    {
        Vector3f selfHalfPosition = getPosition().div(2.0f);
        Vector3f selfMin = selfHalfPosition.sub(new Vector3f(size).div(2.0f), new Vector3f());
        Vector3f selfMax = selfHalfPosition.add(new Vector3f(size).div(2.0f), new Vector3f());

        if (other instanceof BoxCollider boxCollider)
        {
            Vector3f otherHalfPosition = boxCollider.getPosition().div(2.0f);
            Vector3f otherMin = otherHalfPosition.sub(new Vector3f(boxCollider.size).div(2.0f), new Vector3f());
            Vector3f otherMax = otherHalfPosition.add(new Vector3f(boxCollider.size).div(2.0f), new Vector3f());

            return Collider.resolveGeneric(selfMin, selfMax, otherMin, otherMax);
        }

        return new Vector3f();
    }

    @Override
    public @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f selfHalfPosition = getPosition().div(2.0f);
        Vector3f selfMin = selfHalfPosition.sub(new Vector3f(size).div(2.0f), new Vector3f());
        Vector3f selfMax = selfHalfPosition.add(new Vector3f(size).div(2.0f), new Vector3f());

        return Collider.rayIntersectGeneric(selfMin, selfMax, origin, direction);
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    @Override
    public @NotNull Vector3f getSize()
    {
        return size;
    }

    public static @NotNull BoxCollider create(@NotNull Vector3f size)
    {
        BoxCollider result = new BoxCollider();

        result.size = size;

        return result;
    }
}