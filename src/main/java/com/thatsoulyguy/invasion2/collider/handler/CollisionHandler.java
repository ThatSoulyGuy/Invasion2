package com.thatsoulyguy.invasion2.collider.handler;

import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CollisionHandler
{
    /**
     * Handles collision detection and resolution between two colliders.
     * @param a The first collider.
     * @param b The second collider.
     * @param selfIsMovable Indicates if the first collider is movable.
     * @return A CollisionResult containing intersection status and resolution vector.
     */
    CollisionResult handle(@NotNull Collider a, @NotNull Collider b, boolean selfIsMovable);
}