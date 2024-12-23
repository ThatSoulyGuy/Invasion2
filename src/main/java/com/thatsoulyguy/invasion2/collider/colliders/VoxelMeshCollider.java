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
        if (voxels.isEmpty())
            return new Vector3f(0, 0, 0);

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f voxel : voxels)
        {
            minX = Math.min(minX, voxel.x - 0.5f);
            minY = Math.min(minY, voxel.y - 0.5f);
            minZ = Math.min(minZ, voxel.z - 0.5f);

            maxX = Math.max(maxX, voxel.x + 0.5f);
            maxY = Math.max(maxY, voxel.y + 0.5f);
            maxZ = Math.max(maxZ, voxel.z + 0.5f);
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

    public static @NotNull VoxelMeshCollider create()
    {
        return new VoxelMeshCollider();
    }
}