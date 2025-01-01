package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.block.Block;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.util.CoordinateHelper;
import com.thatsoulyguy.invasion2.world.Chunk;
import com.thatsoulyguy.invasion2.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.*;

/**
 * Contains a method(s) for ray casting.
 * <p>
 * Annotates: {@code @Static}
 */
@Static
public class Raycast
{
    private static final @NotNull ExecutorService threadPool = Executors.newFixedThreadPool(3);

    private Raycast() { }

    /**
     * Casts a raycast from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}.
     * This function only picks up on voxels.
     * <p>
     * NOTE: {@code RaycastHit#collider} is invalid, as this function picks up on voxels, not on colliders.
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @return Returns a {@code @Nullable RaycastHit} that will contain the center of the voxel hit, the hit normal, and distance traveled.
     */
    public static @Nullable VoxelHit castVoxel(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance)
    {
        if (normalizedDirection.length() == 0)
            return null;

        Vector3i currentVoxel = CoordinateHelper.worldToGlobalBlockCoordinates(origin);

        Vector3i step = new Vector3i(
                Math.signum(normalizedDirection.x) > 0 ? 1 : (Math.signum(normalizedDirection.x) < 0 ? -1 : 0),
                Math.signum(normalizedDirection.y) > 0 ? 1 : (Math.signum(normalizedDirection.y) < 0 ? -1 : 0),
                Math.signum(normalizedDirection.z) > 0 ? 1 : (Math.signum(normalizedDirection.z) < 0 ? -1 : 0)
        );

        Vector3f tDelta = new Vector3f();
        tDelta.x = (normalizedDirection.x != 0) ? Math.abs(1.0f / normalizedDirection.x) : Float.MAX_VALUE;
        tDelta.y = (normalizedDirection.y != 0) ? Math.abs(1.0f / normalizedDirection.y) : Float.MAX_VALUE;
        tDelta.z = (normalizedDirection.z != 0) ? Math.abs(1.0f / normalizedDirection.z) : Float.MAX_VALUE;

        Vector3f voxelBoundary = new Vector3f(
                step.x > 0 ? currentVoxel.x + 1.0f : currentVoxel.x,
                step.y > 0 ? currentVoxel.y + 1.0f : currentVoxel.y,
                step.z > 0 ? currentVoxel.z + 1.0f : currentVoxel.z
        );

        Vector3f tMax = new Vector3f(
                normalizedDirection.x != 0 ? (voxelBoundary.x - origin.x) / normalizedDirection.x : Float.MAX_VALUE,
                normalizedDirection.y != 0 ? (voxelBoundary.y - origin.y) / normalizedDirection.y : Float.MAX_VALUE,
                normalizedDirection.z != 0 ? (voxelBoundary.z - origin.z) / normalizedDirection.z : Float.MAX_VALUE
        );

        tMax.x = tMax.x < 0 ? Float.MAX_VALUE : tMax.x;
        tMax.y = tMax.y < 0 ? Float.MAX_VALUE : tMax.y;
        tMax.z = tMax.z < 0 ? Float.MAX_VALUE : tMax.z;

        while (true)
        {
            Chunk chunk = World.getLocalWorld().getChunk(CoordinateHelper.worldToChunkCoordinates(new Vector3f(currentVoxel.x, currentVoxel.y, currentVoxel.z)));

            if (chunk != null)
            {
                Vector3i blockPosition = new Vector3i(currentVoxel.x, currentVoxel.y, currentVoxel.z);

                short blockId = chunk.getBlock(CoordinateHelper.worldToBlockCoordinates(new Vector3f(blockPosition)));

                if (blockId != -1 && blockId != BlockRegistry.BLOCK_AIR.getId())
                {
                    VoxelHit hitResult = doRaycast(origin, normalizedDirection, maxDistance, currentVoxel);

                    if (hitResult != null)
                        return hitResult;
                }
            }

            if (tMax.x < tMax.y)
            {
                if (tMax.x < tMax.z)
                {
                    currentVoxel.x += step.x;

                    if (tMax.x > maxDistance)
                        break;

                    tMax.x += tDelta.x;
                }
                else
                {
                    currentVoxel.z += step.z;

                    if (tMax.z > maxDistance)
                        break;

                    tMax.z += tDelta.z;
                }
            }
            else
            {
                if (tMax.y < tMax.z)
                {
                    currentVoxel.y += step.y;

                    if (tMax.y > maxDistance)
                        break;

                    tMax.y += tDelta.y;
                }
                else
                {
                    currentVoxel.z += step.z;

                    if (tMax.z > maxDistance)
                        break;

                    tMax.z += tDelta.z;
                }
            }
        }

        return null;
    }

    /**
     * Performs raycast against a specific voxel to determine hit details.
     *
     * @param origin            The origin of the ray.
     * @param direction         The normalized direction of the ray.
     * @param maxDistance       The maximum distance to check.
     * @param blockPosition     The position of the block being checked.
     * @return                  RaycastStaticResult containing hit details if a hit is detected.
     */
    private static @Nullable VoxelHit doRaycast(Vector3f origin, Vector3f direction, float maxDistance, Vector3i blockPosition)
    {
        Vector3f min = new Vector3f(blockPosition.x, blockPosition.y, blockPosition.z);
        Vector3f max = new Vector3f(blockPosition.x + 1.0f, blockPosition.y + 1.0f, blockPosition.z + 1.0f);

        float t1 = (min.x - origin.x) / (direction.x != 0 ? direction.x : 1e-10f);
        float t2 = (max.x - origin.x) / (direction.x != 0 ? direction.x : 1e-10f);
        float t3 = (min.y - origin.y) / (direction.y != 0 ? direction.y : 1e-10f);
        float t4 = (max.y - origin.y) / (direction.y != 0 ? direction.y : 1e-10f);
        float t5 = (min.z - origin.z) / (direction.z != 0 ? direction.z : 1e-10f);
        float t6 = (max.z - origin.z) / (direction.z != 0 ? direction.z : 1e-10f);

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax)
            return null;

        float depth = (tmin < 0) ? tmax : tmin;

        if (depth > maxDistance)
            return null;

        Vector3f hitPoint = new Vector3f(origin).add(new Vector3f(direction).mul(depth));

        Vector3f hitNormal = new Vector3f(hitPoint).sub(new Vector3f(blockPosition.x + 0.5f, blockPosition.y + 0.5f, blockPosition.z + 0.5f));

        float absX = Math.abs(hitNormal.x);
        float absY = Math.abs(hitNormal.y);
        float absZ = Math.abs(hitNormal.z);

        float maxComponent = Math.max(absX, Math.max(absY, absZ));

        if (maxComponent == absX)
            hitNormal = new Vector3f(Math.signum(hitNormal.x), 0, 0);
        else if (maxComponent == absY)
            hitNormal = new Vector3f(0, Math.signum(hitNormal.y), 0);
        else
            hitNormal = new Vector3f(0, 0, Math.signum(hitNormal.z));

        return new VoxelHit(hitPoint, new Vector3f(blockPosition.x + 0.5f, blockPosition.y + 0.5f, blockPosition.z + 0.5f), hitNormal, depth);
    }

    /**
     * Casts a ray asynchronously from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}.
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @return Returns a {@code CompletableFuture<Optional<RaycastHit>>} that will contain the hit point, hit normal, hit collider, and distance traveled.
     */
    public static @NotNull CompletableFuture<@Nullable Hit> castAsync(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance)
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
    public static @NotNull CompletableFuture<@Nullable Hit> castAsync(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance, @Nullable Collider colliderToIgnore)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            List<Collider> colliders = ColliderManager.getAll().stream()
                    .filter(Collider::isCollidable)
                    .filter(collider -> collider.getCenteredPosition().distance(origin) < maxDistance * 10)
                    .toList();

            if (colliderToIgnore != null)
            {
                colliders = colliders.stream()
                        .filter(collider -> colliderToIgnore != collider)
                        .toList();
            }

            Hit closestHit = null;

            for (Collider collider : colliders)
            {
                Vector3f intersection = collider.rayIntersect(origin, normalizedDirection);

                if (intersection != null)
                {
                    float distance = intersection.distance(origin);

                    if (distance <= maxDistance && (closestHit == null || distance < closestHit.distance()))
                        closestHit = new Hit(intersection, new Vector3f(0.0f), collider, distance);
                }
            }

            return closestHit;
        }, threadPool);
    }

    /**
     * Casts a ray from {@code origin} at direction {@code normalizedDirection} until {@code maxDistance}
     *
     * @param origin The origin of the ray
     * @param normalizedDirection The direction of the ray (normalized)
     * @param maxDistance The maximum distance which the ray can travel
     * @return Returns an {@code Optional<RaycastHit>} that contains the hit point, hit normal, hit collider, and distance traveled to achieve the hit.
     */
    public static @Nullable Hit cast(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance)
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
    public static @Nullable Hit cast(@NotNull Vector3f origin, @NotNull Vector3f normalizedDirection, float maxDistance, @Nullable Collider colliderToIgnore)
    {
        List<Collider> colliders = ColliderManager.getAll();

        if (colliderToIgnore != null)
            colliders = colliders.stream()
                    .filter(collider -> colliderToIgnore != collider)
                    .toList();

        Hit closestHit = null;

        for (Collider collider : colliders)
        {
            Vector3f intersection = collider.rayIntersect(origin, normalizedDirection);

            if (intersection != null)
            {
                float distance = intersection.distance(origin);

                if (distance <= maxDistance && (closestHit == null || distance < closestHit.distance()))
                    closestHit = new Hit(intersection, new Vector3f(0.0f), collider, distance); //TODO: Calculate normal
            }
        }

        return closestHit;
    }

    private static Vector3f calculateNormal(Vector3f direction)
    {
        float absX = Math.abs(direction.x);
        float absY = Math.abs(direction.y);
        float absZ = Math.abs(direction.z);

        Vector3f normal = new Vector3f(0.0f, 0.0f, 0.0f);

        if (absX > absY && absX > absZ)
            normal.x = Math.signum(direction.x);
        else if (absY > absX && absY > absZ)
            normal.y = Math.signum(direction.y);
        else
            normal.z = Math.signum(direction.z);

        return normal;
    }

    public record Hit(@NotNull Vector3f point, @NotNull Vector3f normal, @NotNull Collider collider, float distance) { }
    public record VoxelHit(@NotNull Vector3f point, @NotNull Vector3f center, @NotNull Vector3f normal, float distance) { }
}