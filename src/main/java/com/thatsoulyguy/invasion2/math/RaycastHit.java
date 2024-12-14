package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@CustomConstructor("create")
public class RaycastHit
{
    private @EffectivelyNotNull Vector3f position;
    private @EffectivelyNotNull Vector3f normal;
    private @EffectivelyNotNull Collider collider;
    private float distance;

    private RaycastHit() { }

    public @NotNull Vector3f getPosition()
    {
        return position;
    }

    public @NotNull Vector3f getNormal()
    {
        return normal;
    }

    public @NotNull Collider getCollider()
    {
        return collider;
    }

    public float getDistance()
    {
        return distance;
    }

    public static @NotNull RaycastHit create(@NotNull Vector3f position, @NotNull Vector3f normal, @NotNull Collider collider, float distance)
    {
        RaycastHit result = new RaycastHit();

        result.position = position;
        result.normal = normal;
        result.collider = collider;
        result.distance = distance;

        return result;
    }
}