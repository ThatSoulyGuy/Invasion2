package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@CustomConstructor("create")
public class VoxelMeshCollider extends Collider
{
    private @NotNull List<Vector3f> voxels = new ArrayList<>();

    private VoxelMeshCollider() { }

    @Override
    public boolean intersects(@NotNull Collider other)
    {
        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPos = new Vector3f(getPosition()).add(voxel);
            Vector3f voxelMin = new Vector3f(voxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f voxelMax = new Vector3f(voxelWorldPos).add(0.5f, 0.5f, 0.5f);

            if (other instanceof BoxCollider boxCollider)
            {
                if (intersectAABBs(voxelMin, voxelMax, boxCollider))
                    return true;
            }
            else if (other instanceof VoxelMeshCollider voxelMeshCollider)
            {
                if (intersectVoxelMesh(voxelMin, voxelMax, voxelMeshCollider))
                    return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable Vector3f resolve(@NotNull Collider other)
    {
        Vector3f smallestResolution = null;
        float smallestMagnitude = Float.MAX_VALUE;

        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPos = new Vector3f(getPosition()).add(voxel);
            Vector3f voxelMin = new Vector3f(voxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f voxelMax = new Vector3f(voxelWorldPos).add(0.5f, 0.5f, 0.5f);

            Vector3f resolution = null;

            if (other instanceof BoxCollider boxCollider)
                resolution = resolveAABBs(voxelMin, voxelMax, boxCollider);
            else if (other instanceof VoxelMeshCollider voxelMeshCollider)
                resolution = resolveVoxelMesh(voxelMin, voxelMax, voxelMeshCollider);

            if (resolution != null)
            {
                float mag = resolution.length();

                if (mag < smallestMagnitude)
                {
                    smallestMagnitude = mag;
                    smallestResolution = resolution;
                }
            }
        }

        if (smallestResolution != null)
        {
            applyResolution(other, smallestResolution);
        }

        return smallestResolution;
    }

    @Override
    public @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f closestIntersection = null;
        float closestDistance = Float.MAX_VALUE;

        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPos = new Vector3f(getPosition()).add(voxel);
            Vector3f voxelMin = new Vector3f(voxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f voxelMax = new Vector3f(voxelWorldPos).add(0.5f, 0.5f, 0.5f);

            Vector3f intersection = Collider.rayIntersectGeneric(voxelMin, voxelMax, origin, direction);

            if (intersection != null)
            {
                float distance = new Vector3f(intersection).sub(origin).length();

                if (distance < closestDistance)
                {
                    closestDistance = distance;
                    closestIntersection = intersection;
                }
            }
        }

        return closestIntersection;
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    @Override
    public @NotNull Vector3f getSize()
    {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f voxel : voxels)
        {
            minX = Math.min(minX, voxel.x);
            minY = Math.min(minY, voxel.y);
            minZ = Math.min(minZ, voxel.z);
            maxX = Math.max(maxX, voxel.x);
            maxY = Math.max(maxY, voxel.y);
            maxZ = Math.max(maxZ, voxel.z);
        }

        return new Vector3f(maxX - minX, maxY - minY, maxZ - minZ);
    }

    public void setVoxels(@NotNull List<Vector3f> voxels)
    {
        this.voxels = new ArrayList<>(voxels);
    }

    public @NotNull List<Vector3f> getVoxels()
    {
        return new ArrayList<>(voxels);
    }

    private boolean intersectAABBs(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull BoxCollider box)
    {
        Vector3f bMin = new Vector3f(box.getPosition()).sub(0.5f, 0.5f, 0.5f);
        Vector3f bMax = new Vector3f(box.getPosition()).add(0.5f, 0.5f, 0.5f);

        return Collider.intersectGeneric(aMin, aMax, bMin, bMax);
    }

    private @Nullable Vector3f resolveAABBs(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull BoxCollider box)
    {
        Vector3f bMin = new Vector3f(box.getPosition()).sub(0.5f, 0.5f, 0.5f);
        Vector3f bMax = new Vector3f(box.getPosition()).add(0.5f, 0.5f, 0.5f);

        return Collider.resolveGeneric(aMin, aMax, bMin, bMax);
    }

    private boolean intersectVoxelMesh(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull VoxelMeshCollider voxelMesh)
    {
        for (Vector3f otherVoxel : voxelMesh.getVoxels())
        {
            Vector3f otherVoxelWorldPos = new Vector3f(voxelMesh.getPosition()).add(otherVoxel);
            Vector3f otherMin = new Vector3f(otherVoxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f otherMax = new Vector3f(otherVoxelWorldPos).add(0.5f, 0.5f, 0.5f);

            if (Collider.intersectGeneric(aMin, aMax, otherMin, otherMax))
                return true;
        }

        return false;
    }

    private @Nullable Vector3f resolveVoxelMesh(@NotNull Vector3f aMin, @NotNull Vector3f aMax, @NotNull VoxelMeshCollider voxelMesh)
    {
        Vector3f resolution = null;

        for (Vector3f otherVoxel : voxelMesh.getVoxels())
        {
            Vector3f otherVoxelWorldPos = new Vector3f(voxelMesh.getPosition()).add(otherVoxel);
            Vector3f otherMin = new Vector3f(otherVoxelWorldPos).sub(0.5f, 0.5f, 0.5f);
            Vector3f otherMax = new Vector3f(otherVoxelWorldPos).add(0.5f, 0.5f, 0.5f);
            Vector3f res = Collider.resolveGeneric(aMin, aMax, otherMin, otherMax);

            if (res != null)
            {
                if (resolution == null || res.length() < resolution.length())
                    resolution = res;
            }
        }

        return resolution;
    }

    private void applyResolution(Collider other, Vector3f resolution)
    {
        boolean vmcDynamic = isDynamic();
        boolean otherDynamic = other.isDynamic();

        if (vmcDynamic && otherDynamic)
        {
            Vector3f vmcSize = getSize();
            Vector3f otherSize = other.getSize();
            float totalSize = vmcSize.length() + otherSize.length();

            if (totalSize == 0) totalSize = 1;

            Vector3f vmcMove = new Vector3f(resolution).mul(vmcSize.length() / totalSize);
            Vector3f otherMove = new Vector3f(resolution).mul(-otherSize.length() / totalSize);

            getGameObject().getTransform().translate(vmcMove);
            other.getGameObject().getTransform().translate(otherMove);
        }
        else if (vmcDynamic)
            getGameObject().getTransform().translate(resolution);
        else if (other.isDynamic())
            other.getGameObject().getTransform().translate(new Vector3f(resolution).negate());
    }

    public static @NotNull VoxelMeshCollider create()
    {
        return new VoxelMeshCollider();
    }
}