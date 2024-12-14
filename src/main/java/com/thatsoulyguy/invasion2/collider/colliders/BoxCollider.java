package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

@CustomConstructor("create")
public class BoxCollider extends Collider
{
    private @NotNull Vector3f size = new Vector3f(1.0f);
    private static final float EPSILON = 1e-4f;
    private static final float MAX_TRANSLATION = 1.0f;

    private BoxCollider() { }

    public @NotNull Vector3f getSize()
    {
        return new Vector3f(size);
    }

    @Override
    public boolean intersects(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            return intersectsBox(box);
        else if (other instanceof VoxelMeshCollider voxelMesh)
            return intersectsVoxelMesh(voxelMesh);

        return false;
    }

    @Override
    public @NotNull Vector3f resolve(@NotNull Collider other)
    {
        Vector3f movement = new Vector3f();

        if (other instanceof BoxCollider box)
            movement = resolveWithBox(box);
        else if (other instanceof VoxelMeshCollider voxelMesh)
            movement = resolveWithVoxelMesh(voxelMesh);

        return movement;
    }

    @Override
    public @NotNull Optional<Vector3f> rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f worldPosition = getPosition();
        Vector3f half = new Vector3f(this.size).mul(0.5f);

        Vector3f min = worldPosition.sub(half, new Vector3f());
        Vector3f max = worldPosition.add(half, new Vector3f());

        return Collider.rayIntersectGeneric(min, max, origin, direction);
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    private boolean intersectsBox(@NotNull BoxCollider box)
    {
        Vector3f aCenter = this.getPosition();
        Vector3f aHalf = new Vector3f(this.size).mul(0.5f);

        Vector3f bCenter = box.getPosition();
        Vector3f bHalf = new Vector3f(box.size).mul(0.5f);

        return Math.abs(aCenter.x - bCenter.x) < (aHalf.x + bHalf.x) &&
                Math.abs(aCenter.y - bCenter.y) < (aHalf.y + bHalf.y) &&
                Math.abs(aCenter.z - bCenter.z) < (aHalf.z + bHalf.z);
    }

    private boolean intersectsVoxelMesh(@NotNull VoxelMeshCollider voxelMesh)
    {
        return voxelMesh.intersects(this);
    }

    private @NotNull Vector3f resolveWithBox(@NotNull BoxCollider box)
    {
        Vector3f aCenter = this.getPosition();
        Vector3f bCenter = box.getPosition();

        Vector3f aHalf = new Vector3f(this.size).mul(0.5f);
        Vector3f bHalf = new Vector3f(box.size).mul(0.5f);

        Vector3f delta = new Vector3f(aCenter).sub(bCenter);

        float overlapX = (aHalf.x + bHalf.x) - Math.abs(delta.x);
        float overlapY = (aHalf.y + bHalf.y) - Math.abs(delta.y);
        float overlapZ = (aHalf.z + bHalf.z) - Math.abs(delta.z);

        if (overlapX <= 0 || overlapY <= 0 || overlapZ <= 0)
            return new Vector3f();

        float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));
        Vector3f mtv = new Vector3f();

        if (minOverlap == overlapX)
            mtv.x = delta.x > 0 ? minOverlap : -minOverlap;
        else if (minOverlap == overlapY)
            mtv.y = delta.y > 0 ? minOverlap : -minOverlap;
        else
            mtv.z = delta.z > 0 ? minOverlap : -minOverlap;

        mtv.x = Math.max(-MAX_TRANSLATION, Math.min(mtv.x, MAX_TRANSLATION));
        mtv.y = Math.max(-MAX_TRANSLATION, Math.min(mtv.y, MAX_TRANSLATION));
        mtv.z = Math.max(-MAX_TRANSLATION, Math.min(mtv.z, MAX_TRANSLATION));

        if (minOverlap > EPSILON)
            getGameObject().getTransform().translate(mtv);

        return mtv;
    }

    private @NotNull Vector3f resolveWithVoxelMesh(@NotNull VoxelMeshCollider voxelMesh)
    {
        Vector3f selfWorldPosition = getPosition();
        Vector3f selfHalf = new Vector3f(this.size).mul(0.5f);

        Vector3f selfMin = selfWorldPosition.sub(selfHalf, new Vector3f());
        Vector3f selfMax = selfWorldPosition.add(selfHalf, new Vector3f());

        Vector3f movement = new Vector3f(0);

        for (Vector3f voxel : voxelMesh.getVoxels())
        {
            Vector3f voxelWorldPosition = new Vector3f(voxelMesh.getPosition()).add(voxel);
            Vector3f voxelHalf = new Vector3f(0.5f, 0.5f, 0.5f);

            Vector3f voxelMin = voxelWorldPosition.sub(voxelHalf, new Vector3f());
            Vector3f voxelMax = voxelWorldPosition.add(voxelHalf, new Vector3f());

            Optional<Vector3f> optionalMovement = Collider.resolveGeneric(selfMin, selfMax, voxelMin, voxelMax);

            if (optionalMovement.isEmpty())
                continue;

            movement.add(optionalMovement.get());
        }

        if (movement.lengthSquared() > 0)
            getGameObject().getTransform().translate(movement);

        return movement;
    }

    public static @NotNull BoxCollider create(@NotNull Vector3f size)
    {
        BoxCollider result = new BoxCollider();

        result.size = size;

        return result;
    }
}