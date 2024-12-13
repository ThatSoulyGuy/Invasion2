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
     */
    public abstract void resolve(@NotNull Collider other);

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

    @Override
    public void uninitialize()
    {
        ColliderManager.unregister(getGameObject());
    }
}