package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BoxCollider extends Collider
{
    private @EffectivelyNotNull Vector3f size;

    @Override
    public @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f center = getPosition();
        Vector3f halfSize = new Vector3f(size).div(2.0f);
        Vector3f selfMin = center.sub(halfSize, new Vector3f());
        Vector3f selfMax = center.add(halfSize, new Vector3f());

        return Collider.rayIntersectGeneric(selfMin, selfMax, origin, direction);
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    public @NotNull BoxCollider setSize(@NotNull Vector3f size)
    {
        this.size = size;

        return this;
    }

    @Override
    public @NotNull Vector3f getSize()
    {
        return new Vector3f(size);
    }
}