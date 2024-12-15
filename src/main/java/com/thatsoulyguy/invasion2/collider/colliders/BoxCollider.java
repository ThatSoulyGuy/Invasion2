package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.SweptAABBTester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@CustomConstructor("create")
public class BoxCollider extends Collider
{
    private final Vector3f size = new Vector3f(1,1,1);

    private BoxCollider() { }

    @Override
    public boolean intersects(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            return intersectsBox(box);
        else if (other instanceof VoxelMeshCollider voxel)
            return voxel.intersects(this);
        return false;
    }

    @Override
    public @NotNull Vector3f resolve(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            return resolveWithBox(box);
        else if (other instanceof VoxelMeshCollider voxel)
            return resolveWithVoxelMesh(voxel);

        return new Vector3f();
    }

    @Override
    public @Nullable Float sweepTest(@NotNull Collider other, @NotNull Vector3f displacement)
    {
        if (other instanceof BoxCollider box)
            return sweepTestBox(box, displacement);
        else if (other instanceof VoxelMeshCollider voxel)
            return voxel.sweepTest(this, displacement);
        return null;
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    public Vector3f getSize()
    {
        return new Vector3f(size);
    }

    private boolean intersectsBox(BoxCollider other)
    {
        Vector3f posA = getPosition(); Vector3f halfA = new Vector3f(size).mul(0.5f);
        Vector3f minA = new Vector3f(posA).sub(halfA);
        Vector3f maxA = new Vector3f(posA).add(halfA);

        Vector3f posB = other.getPosition(); Vector3f halfB = new Vector3f(other.size).mul(0.5f);
        Vector3f minB = new Vector3f(posB).sub(halfB);
        Vector3f maxB = new Vector3f(posB).add(halfB);

        return aabbIntersect(minA, maxA, minB, maxB);
    }

    private @NotNull Vector3f resolveWithBox(BoxCollider other)
    {
        Vector3f posA = getPosition(); Vector3f halfA = new Vector3f(size).mul(0.5f);
        Vector3f minA = new Vector3f(posA).sub(halfA);
        Vector3f maxA = new Vector3f(posA).add(halfA);

        Vector3f posB = other.getPosition(); Vector3f halfB = new Vector3f(other.size).mul(0.5f);
        Vector3f minB = new Vector3f(posB).sub(halfB);
        Vector3f maxB = new Vector3f(posB).add(halfB);

        Vector3f mtv = resolveGeneric(minA, maxA, minB, maxB);

        if (mtv != null)
        {
            getGameObject().getTransform().translate(mtv);
            return mtv;
        }

        return new Vector3f();
    }

    private @NotNull Vector3f resolveWithVoxelMesh(VoxelMeshCollider voxelMesh)
    {
        return voxelMesh.resolve(this);
    }

    private @Nullable Float sweepTestBox(BoxCollider other, Vector3f displacement)
    {
        Vector3f posA = getPosition(); Vector3f halfA = new Vector3f(size).mul(0.5f);
        Vector3f minA0 = new Vector3f(posA).sub(halfA);
        Vector3f maxA0 = new Vector3f(posA).add(halfA);

        Vector3f posB = other.getPosition(); Vector3f halfB = new Vector3f(other.size).mul(0.5f);
        Vector3f minB = new Vector3f(posB).sub(halfB);
        Vector3f maxB = new Vector3f(posB).add(halfB);

        return SweptAABBTester.sweptAABB(minA0, maxA0, minB, maxB, displacement);
    }

    @Override
    public @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f worldPosition = getPosition();
        Vector3f half = new Vector3f(this.size).mul(0.5f);

        Vector3f min = worldPosition.sub(half, new Vector3f());
        Vector3f max = worldPosition.add(half, new Vector3f());

        return Collider.rayIntersectGeneric(min, max, origin, direction);
    }

    public static @NotNull BoxCollider create(Vector3f size)
    {
        BoxCollider result = new BoxCollider();

        result.size.set(size);

        return result;
    }
}