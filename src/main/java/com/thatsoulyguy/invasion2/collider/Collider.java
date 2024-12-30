package com.thatsoulyguy.invasion2.collider;

import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.collider.handler.CollisionHandlerManager;
import com.thatsoulyguy.invasion2.render.DebugRenderer;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * An interface for collisions and collider-related topics
 */
public abstract class Collider extends Component
{
    private static final float EPSILON = 1e-6f;

    private boolean isCollidable = true;

    protected Collider() { }

    @Override
    public void initialize()
    {
        if (isCollidable)
            ColliderManager.register(this);
    }

    @Override
    public void uninitialize()
    {
        if (ColliderManager.has(getGameObject()))
            ColliderManager.unregister(getGameObject());
    }

    public boolean isCollidable()
    {
        return isCollidable;
    }

    public void setCollidable(boolean collidable)
    {
        if (collidable && !ColliderManager.has(getGameObject()))
            ColliderManager.register(this);
        else if (!collidable && ColliderManager.has(getGameObject()))
            ColliderManager.unregister(getGameObject());

        isCollidable = collidable;
    }

    public abstract @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction);

    /**
     * Gets the world position of the collider.
     * @return The position as a Vector3f.
     */
    public abstract @NotNull Vector3f getPosition();

    /**
     * Returns the centered position of the collider.
     *
     * @return The center position as a Vector3f.
     */
    public Vector3f getCenteredPosition()
    {
        return new Vector3f(getPosition()).add(new Vector3f(getSize()).mul(0.5f));
    }

    /**
     * Determines if one AABB is on top of another AABB.
     * <p>
     * "On top" is defined as:
     * - The AABBs overlap on the X and Z axes.
     * - AABB 'a' is entirely above the other along the Y axis.
     *
     * @param aMin The minimum (x, y, z) coordinates of Box A.
     * @param aMax The maximum (x, y, z) coordinates of Box A.
     * @param bMin The minimum (x, y, z) coordinates of Box B.
     * @param bMax The maximum (x, y, z) coordinates of Box B.
     * @return True if one AABB is on top of the other; false otherwise.
     */
    public static boolean isOnTopOf(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull Vector3f bMin, @NotNull Vector3f bMax)
    {
        boolean overlapX = aMax.x >= bMin.x && aMin.x <= bMax.x;
        boolean overlapZ = aMax.z >= bMin.z && aMin.z <= bMax.z;

        if (!(overlapX && overlapZ))
            return false;

        final float EPSILON = 1e-3f;

        return aMin.y >= bMax.y - EPSILON && aMin.y <= bMax.y + EPSILON;
    }

    /**
     * Gets the size or dimensions of the collider.
     * @return The size as a Vector3f.
     */
    public abstract @NotNull Vector3f getSize();

    /**
     * Checks if this collider intersects with another collider.
     * @param other The other collider.
     * @return True if they intersect, false otherwise.
     */
    public boolean intersects(@NotNull Collider other)
    {
        return CollisionHandlerManager.intersects(this, other);
    }

    /**
     * Resolves the collision between this collider and another collider.
     * @param other The other collider.
     * @param selfIsMovable Indicates if this collider is movable.
     * @return A Vector3f representing the resolution vector.
     */
    public @NotNull Vector3f resolve(@NotNull Collider other, boolean selfIsMovable)
    {
        return CollisionHandlerManager.resolve(this, other, selfIsMovable);
    }

    /**
     * Determines if two Axis-Aligned Bounding Boxes (AABBs) intersect.
     *
     * @param aMin The minimum (x, y, z) coordinates of Box A.
     * @param aMax The maximum (x, y, z) coordinates of Box A.
     * @param bMin The minimum (x, y, z) coordinates of Box B.
     * @param bMax The maximum (x, y, z) coordinates of Box B.
     * @return True if the boxes intersect; false otherwise.
     */
    public static boolean intersectsGeneric(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull Vector3f bMin, @NotNull Vector3f bMax)
    {
        return (aMin.x <= bMax.x && aMax.x >= bMin.x) && (aMin.y <= bMax.y && aMax.y >= bMin.y) && (aMin.z <= bMax.z && aMax.z >= bMin.z);
    }

    /**
     * A generic function to resolve an intersection between two AABBs
     *
     * @param aMin The minimum bound of collider A
     * @param aMax The maximum bound of collider A
     * @param bMin The minimum bound of collider B
     * @param bMax The maximum bound of collider B
     *
     * @return The movement required to resolve the collision
     */
    public static @Nullable Vector3f resolveGeneric(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull Vector3f bMin, @NotNull Vector3f bMax)
    {
        float overlapX = Math.min(aMax.x - bMin.x, bMax.x - aMin.x);
        float overlapY = Math.min(aMax.y - bMin.y, bMax.y - aMin.y);
        float overlapZ = Math.min(aMax.z - bMin.z, bMax.z - aMin.z);

        final float EPSILON = 1e-5f;

        if (overlapX < EPSILON || overlapY < EPSILON || overlapZ < EPSILON)
            return null;

        Vector3f mtv = new Vector3f();

        if (overlapY <= overlapX && overlapY <= overlapZ)
        {
            float centerA = (aMin.y + aMax.y) / 2.0f;
            float centerB = (bMin.y + bMax.y) / 2.0f;

            mtv.y = centerA < centerB ? -overlapY : overlapY;
        }
        else if (overlapX <= overlapY && overlapX <= overlapZ)
        {
            float centerA = (aMin.x + aMax.x) / 2.0f;
            float centerB = (bMin.x + bMax.x) / 2.0f;

            mtv.x = centerA < centerB ? -overlapX : overlapX;
        }
        else
        {
            float centerA = (aMin.z + aMax.z) / 2.0f;
            float centerB = (bMin.z + bMax.z) / 2.0f;

            mtv.z = centerA < centerB ? -overlapZ : overlapZ;
        }

        return mtv;
    }

    public static Vector3f resolveAllCollisions(BoxCollider box, VoxelMeshCollider voxelMesh)
    {
        Vector3f boxPos = box.getPosition();
        Vector3f boxSize = box.getSize();

        Vector3f boxMin = new Vector3f(boxPos).sub(new Vector3f(boxSize).mul(0.5f));
        Vector3f boxMax = new Vector3f(boxPos).add(new Vector3f(boxSize).mul(0.5f));

        //DebugRenderer.addBox(boxMin, boxMax, new Vector3f(1.0f, 0.0f, 0.0f));

        Vector3f cumulativeResolution = new Vector3f();

        for (Vector3f voxel : voxelMesh.getVoxels())
        {
            Vector3f voxelWorldPos = new Vector3f(voxelMesh.getPosition()).add(voxel);

            Vector3f voxelMin = new Vector3f(voxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f voxelMax = new Vector3f(voxelWorldPos).add(0.5f, 0.5f, 0.5f);

            //DebugRenderer.addBox(voxelMin, voxelMax, new Vector3f(1.0f, 0.0f, 0.0f));

            if (Collider.intersectsGeneric(boxMin, boxMax, voxelMin, voxelMax))
            {
                Vector3f resolution = Collider.resolveGeneric(boxMin, boxMax, voxelMin, voxelMax);

                if (resolution != null)
                    cumulativeResolution.add(resolution);
            }
        }

        box.getGameObject().getTransform().translate(cumulativeResolution);
        return cumulativeResolution;
    }

    /**
     * A generic function to detect a ray intersection on an AABB
     *
     * @param min The minimum bound of the collider
     * @param max The maximum bound of the collider
     * @param origin The origin of the ray's caster
     * @param direction The direction of the ray's caster
     *
     * @return The point of intersection
     */
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

    public static <T extends Collider> @NotNull T create(Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Collider! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}