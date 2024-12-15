package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Contains a method(s) for ray casting.
 * <p>
 * Annotates: {@code @Static}
 */
@Static
public class Raycast
{
    private Raycast() { }

    /**
     * Casts a ray asynchronously from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}.
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @return Returns a {@code CompletableFuture<Optional<RaycastHit>>} that will contain the hit point, hit normal, hit collider, and distance traveled.
     */
    public static @NotNull CompletableFuture<@Nullable RaycastHit> castAsync(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance)
    {
        return castAsync(origin, normalizedDirection, maxDistance, null);
    }

    /**
     * Casts a ray asynchronously from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}.
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @param colliderToIgnore A {@code @Nullable} collider to ignore when casting.
     * @return Returns a {@code CompletableFuture<Optional<RaycastHit>>} that will contain the hit point, hit normal, hit collider, and distance traveled.
     */
    public static @NotNull CompletableFuture<@Nullable RaycastHit> castAsync(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance, @Nullable Collider colliderToIgnore)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            List<Collider> colliders = ColliderManager.getAll();

            if (colliderToIgnore != null)
            {
                colliders = colliders.stream()
                        .filter(collider -> colliderToIgnore != collider)
                        .toList();
            }

            RaycastHit closestHit = null;

            for (Collider collider : colliders)
            {
                Vector3f intersection = collider.rayIntersect(origin, normalizedDirection);

                if (intersection != null)
                {
                    float distance = intersection.distance(origin);

                    if (distance <= maxDistance && (closestHit == null || distance < closestHit.getDistance()))
                        closestHit = RaycastHit.create(intersection, new Vector3f(0.0f), collider, distance);
                }
            }

            return closestHit;
        });
    }

    /**
     * Casts a ray from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @return Returns an {@code Optional<RaycastHit>} that contains the hit point, hit normal, hit collider, and distance traveled to achieve the hit.
     */
    public static @Nullable RaycastHit cast(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance)
    {
        return cast(origin, normalizedDirection, maxDistance, null);
    }

    /**
     * Casts a ray from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @param colliderToIgnore A {@code @Nullable} collider to ignore when casting.
     * @return Returns an {@code Optional<RaycastHit>} that contains the hit point, hit normal, hit collider, and distance traveled to achieve the hit.
     */
    public static @Nullable RaycastHit cast(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance, @Nullable Collider colliderToIgnore)
    {
        List<Collider> colliders = ColliderManager.getAll();

        if (colliderToIgnore != null)
            colliders = colliders.stream()
                    .filter(collider -> colliderToIgnore != collider)
                    .toList();

        RaycastHit closestHit = null;

        for (Collider collider : colliders)
        {
            Vector3f intersection = collider.rayIntersect(origin, normalizedDirection);

            if (intersection != null)
            {
                float distance = intersection.distance(origin);

                if (distance <= maxDistance && (closestHit == null || distance < closestHit.getDistance()))
                    closestHit = RaycastHit.create(intersection, new Vector3f(0.0f), collider, distance); //TODO: Calculate normal
            }
        }

        return closestHit;
    }
}