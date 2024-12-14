package com.thatsoulyguy.invasion2.collider;

import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class Collider extends Component
{
    @Override
    public void initialize()
    {
        ColliderManager.register(this);
    }

    public abstract boolean intersects(@NotNull Collider other);

    /**
     * Resolves collisions by adjusting the position of this collider to prevent overlap.
     *
     * @param other The other collider to resolve against.
     * @return The penetration vector
     */
    public abstract @NotNull Vector3f resolve(@NotNull Collider other);

    public abstract @NotNull Optional<Vector3f> rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction);

    public abstract @NotNull Vector3f getPosition();

    public static Optional<Vector3f> resolveGeneric(Vector3f minA, Vector3f maxA, Vector3f minB, Vector3f maxB)
    {
        float overlapX = Math.min(maxA.x - minB.x, maxB.x - minA.x);
        float overlapY = Math.min(maxA.y - minB.y, maxB.y - minA.y);
        float overlapZ = Math.min(maxA.z - minB.z, maxB.z - minA.z);

        if (overlapX <= 0 || overlapY <= 0 || overlapZ <= 0)
            return Optional.empty();

        float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));

        Vector3f mtv = new Vector3f(0, 0, 0);

        if (minOverlap == overlapX)
        {
            float centerA = (minA.x + maxA.x) / 2.0f;
            float centerB = (minB.x + maxB.x) / 2.0f;

            mtv.x = centerA < centerB ? -overlapX : overlapX;
        }
        else if (minOverlap == overlapY)
        {
            float centerA = (minA.y + maxA.y) / 2.0f;
            float centerB = (minB.y + maxB.y) / 2.0f;

            mtv.y = centerA < centerB ? -overlapY : overlapY;
        }
        else
        {
            float centerA = (minA.z + maxA.z) / 2.0f;
            float centerB = (minB.z + maxB.z) / 2.0f;

            mtv.z = centerA < centerB ? -overlapZ : overlapZ;
        }

        return Optional.of(mtv);
    }

    public static @NotNull Optional<Vector3f> rayIntersectGeneric(@NotNull Vector3f min, @NotNull Vector3f max, @NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f invDir = new Vector3f(1.0f / direction.x, 1.0f / direction.y, 1.0f / direction.z);

        float t1 = (min.x - origin.x) * invDir.x;
        float t2 = (max.x - origin.x) * invDir.x;
        float t3 = (min.y - origin.y) * invDir.y;
        float t4 = (max.y - origin.y) * invDir.y;
        float t5 = (min.z - origin.z) * invDir.z;
        float t6 = (max.z - origin.z) * invDir.z;

        float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tMax < 0 || tMin > tMax)
            return Optional.empty();

        float t = tMin > 0 ? tMin : tMax;

        Vector3f hitPoint = new Vector3f(origin).add(new Vector3f(direction).mul(t));
        return Optional.of(hitPoint);
    }

    @Override
    public void uninitialize()
    {
        ColliderManager.unregister(getGameObject());
    }
}