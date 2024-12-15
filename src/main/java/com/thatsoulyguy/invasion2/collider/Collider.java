package com.thatsoulyguy.invasion2.collider;

import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class Collider extends Component
{
    @Override
    public void initialize()
    {
        ColliderManager.register(this);
    }

    @Override
    public void uninitialize()
    {
        ColliderManager.unregister(getGameObject());
    }

    public abstract boolean intersects(@NotNull Collider other);

    public abstract @NotNull Vector3f resolve(@NotNull Collider other);

    public abstract @Nullable Float sweepTest(@NotNull Collider other, @NotNull Vector3f displacement);

    public abstract @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction);

    public abstract @NotNull Vector3f getPosition();

    public static boolean aabbIntersect(@NotNull Vector3f minA, @NotNull Vector3f maxA, @NotNull Vector3f minB, @NotNull Vector3f maxB)
    {
        return !(maxA.x < minB.x || minA.x > maxB.x ||
                maxA.y < minB.y || minA.y > maxB.y ||
                maxA.z < minB.z || minA.z > maxB.z);
    }

    public static @Nullable Vector3f resolveGeneric(Vector3f minA, Vector3f maxA, Vector3f minB, Vector3f maxB)
    {
        float overlapX = Math.min(maxA.x - minB.x, maxB.x - minA.x);
        float overlapY = Math.min(maxA.y - minB.y, maxB.y - minA.y);
        float overlapZ = Math.min(maxA.z - minB.z, maxB.z - minA.z);

        if (overlapX <= 0 || overlapY <= 0 || overlapZ <= 0)
            return null;

        float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));
        Vector3f mtv = new Vector3f();

        if (minOverlap == overlapX)
        {
            float centerA = (minA.x + maxA.x)*0.5f;
            float centerB = (minB.x + maxB.x)*0.5f;

            mtv.x = centerA < centerB ? -overlapX : overlapX;
        }
        else if (minOverlap == overlapY)
        {
            float centerA = (minA.y + maxA.y)*0.5f;
            float centerB = (minB.y + maxB.y)*0.5f;

            mtv.y = centerA < centerB ? -overlapY : overlapY;
        }
        else
        {
            float centerA = (minA.z + maxA.z)*0.5f;
            float centerB = (minB.z + maxB.z)*0.5f;

            mtv.z = centerA < centerB ? -overlapZ : overlapZ;
        }

        return mtv;
    }

    public static @Nullable Vector3f rayIntersectGeneric(@NotNull Vector3f min, @NotNull Vector3f max, @NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        final float EPSILON = 1e-8f;

        Vector3f invDir = new Vector3f(
                Math.abs(direction.x) > EPSILON ? 1.0f / direction.x : Float.POSITIVE_INFINITY,
                Math.abs(direction.y) > EPSILON ? 1.0f / direction.y : Float.POSITIVE_INFINITY,
                Math.abs(direction.z) > EPSILON ? 1.0f / direction.z : Float.POSITIVE_INFINITY
        );

        float t1 = (min.x - origin.x) * invDir.x;
        float t2 = (max.x - origin.x) * invDir.x;
        float t3 = (min.y - origin.y) * invDir.y;
        float t4 = (max.y - origin.y) * invDir.y;
        float t5 = (min.z - origin.z) * invDir.z;
        float t6 = (max.z - origin.z) * invDir.z;

        float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tMax < 0 || tMin > tMax)
            return null;

        float t = tMin > EPSILON ? tMin : tMax;

        if (t < EPSILON)
            return null;

        return new Vector3f(origin).add(new Vector3f(direction).mul(t));
    }
}