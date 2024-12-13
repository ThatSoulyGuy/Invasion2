package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CustomConstructor("create")
public class VoxelMeshCollider extends Collider
{
    private @NotNull List<Vector3f> voxels = new ArrayList<>();
    private static final float EPSILON = 1e-4f;

    private VoxelMeshCollider() { }

    public void setVoxels(@NotNull List<Vector3f> voxels)
    {
        this.voxels = new ArrayList<>(voxels);
    }

    public @NotNull List<Vector3f> getVoxels()
    {
        return Collections.unmodifiableList(voxels);
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
    public void resolve(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            resolveWithBox(box);
        else if (other instanceof VoxelMeshCollider voxelMesh)
            resolveWithVoxelMesh(voxelMesh);
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    /**
     * Checks intersection with a BoxCollider.
     */
    private boolean intersectsBox(@NotNull BoxCollider box)
    {
        Vector3f boxCenter = box.getPosition();
        Vector3f boxHalf = new Vector3f(box.getSize()).mul(0.5f);

        Vector3f thisCenter = this.getPosition();

        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPos = new Vector3f(thisCenter).add(voxel);
            Vector3f voxelHalf = new Vector3f(0.5f, 0.5f, 0.5f);

            boolean overlapX = Math.abs(voxelWorldPos.x - boxCenter.x) < (voxelHalf.x + boxHalf.x);
            boolean overlapY = Math.abs(voxelWorldPos.y - boxCenter.y) < (voxelHalf.y + boxHalf.y);
            boolean overlapZ = Math.abs(voxelWorldPos.z - boxCenter.z) < (voxelHalf.z + boxHalf.z);

            if (overlapX && overlapY && overlapZ)
                return true;
        }

        return false;
    }

    /**
     * Checks intersection with another VoxelMeshCollider.
     */
    private boolean intersectsVoxelMesh(@NotNull VoxelMeshCollider other)
    {
        Vector3f thisCenter = this.getPosition();
        Vector3f otherCenter = other.getPosition();

        for (Vector3f voxelA : this.voxels)
        {
            Vector3f voxelAWorldPos = new Vector3f(thisCenter).add(voxelA);
            for (Vector3f voxelB : other.voxels)
            {
                Vector3f voxelBWorldPos = new Vector3f(otherCenter).add(voxelB);
                if (voxelAWorldPos.equals(voxelBWorldPos, 1e-6f))
                    return true;
            }
        }

        return false;
    }

    /**
     * Resolves collision with a BoxCollider by adjusting this VoxelMeshCollider's position.
     */
    private void resolveWithBox(@NotNull BoxCollider box)
    {
        Vector3f boxCenter = box.getPosition();
        Vector3f boxHalf = new Vector3f(box.getSize()).mul(0.5f);

        Vector3f thisCenter = this.getPosition();

        float smallestOverlap = Float.MAX_VALUE;
        Vector3f mtv = new Vector3f();

        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPos = new Vector3f(thisCenter).add(voxel);
            Vector3f voxelHalf = new Vector3f(0.5f, 0.5f, 0.5f);

            Vector3f delta = new Vector3f(voxelWorldPos).sub(boxCenter);

            float overlapX = (voxelHalf.x + boxHalf.x) - Math.abs(delta.x);
            float overlapY = (voxelHalf.y + boxHalf.y) - Math.abs(delta.y);
            float overlapZ = (voxelHalf.z + boxHalf.z) - Math.abs(delta.z);

            if (overlapX > 0 && overlapY > 0 && overlapZ > 0)
            {
                float currentOverlap;
                Vector3f currentMtv = new Vector3f();

                if (overlapX < overlapY && overlapX < overlapZ)
                {
                    currentOverlap = overlapX;
                    currentMtv.x = delta.x > 0 ? currentOverlap : -currentOverlap;
                }
                else if (overlapY < overlapX && overlapY < overlapZ)
                {
                    currentOverlap = overlapY;
                    currentMtv.y = delta.y > 0 ? currentOverlap : -currentOverlap;
                }
                else
                {
                    currentOverlap = overlapZ;
                    currentMtv.z = delta.z > 0 ? currentOverlap : -currentOverlap;
                }

                if (currentOverlap < smallestOverlap)
                {
                    smallestOverlap = currentOverlap;
                    mtv.set(currentMtv);
                }
            }
        }

        mtv.x = Math.max(-1.0f, Math.min(mtv.x, 1.0f));
        mtv.y = Math.max(-1.0f, Math.min(mtv.y, 1.0f));
        mtv.z = Math.max(-1.0f, Math.min(mtv.z, 1.0f));

        if (smallestOverlap < Float.MAX_VALUE && smallestOverlap > EPSILON)
            getGameObject().getTransform().translate(mtv);
    }

    /**
     * Resolves collision with another VoxelMeshCollider by adjusting this VoxelMeshCollider's position.
     */
    private void resolveWithVoxelMesh(@NotNull VoxelMeshCollider other)
    {
        Vector3f thisCenter = this.getPosition();
        Vector3f otherCenter = other.getPosition();

        float smallestOverlap = Float.MAX_VALUE;
        Vector3f mtv = new Vector3f();

        for (Vector3f voxelA : this.voxels)
        {
            Vector3f voxelAWorldPos = new Vector3f(thisCenter).add(voxelA);

            for (Vector3f voxelB : other.voxels)
            {
                Vector3f voxelBWorldPos = new Vector3f(otherCenter).add(voxelB);
                Vector3f delta = new Vector3f(voxelAWorldPos).sub(voxelBWorldPos);

                float overlapX = 1.0f - Math.abs(delta.x);
                float overlapY = 1.0f - Math.abs(delta.y);
                float overlapZ = 1.0f - Math.abs(delta.z);

                if (overlapX > 0 && overlapY > 0 && overlapZ > 0)
                {
                    float currentOverlap;
                    Vector3f currentMtv = new Vector3f();

                    if (overlapX < overlapY && overlapX < overlapZ)
                    {
                        currentOverlap = overlapX;
                        currentMtv.x = delta.x > 0 ? currentOverlap : -currentOverlap;
                    }
                    else if (overlapY < overlapX && overlapY < overlapZ)
                    {
                        currentOverlap = overlapY;
                        currentMtv.y = delta.y > 0 ? currentOverlap : -currentOverlap;
                    }
                    else
                    {
                        currentOverlap = overlapZ;
                        currentMtv.z = delta.z > 0 ? currentOverlap : -currentOverlap;
                    }

                    if (currentOverlap < smallestOverlap)
                    {
                        smallestOverlap = currentOverlap;
                        mtv.set(currentMtv);
                    }
                }
            }
        }

        mtv.x = Math.max(-1.0f, Math.min(mtv.x, 1.0f));
        mtv.y = Math.max(-1.0f, Math.min(mtv.y, 1.0f));
        mtv.z = Math.max(-1.0f, Math.min(mtv.z, 1.0f));

        if (smallestOverlap < Float.MAX_VALUE && smallestOverlap > EPSILON)
            getGameObject().getTransform().translate(mtv);
    }

    public static @NotNull VoxelMeshCollider create() {
        return new VoxelMeshCollider();
    }
}