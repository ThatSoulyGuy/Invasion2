package com.thatsoulyguy.invasion2.collider.colliders;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.SweptAABBTester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CustomConstructor("create")
public class VoxelMeshCollider extends Collider
{
    private final List<Vector3f> voxels = new ArrayList<>();

    private VoxelMeshCollider() { }

    @Override
    public boolean intersects(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            return intersectsBox(box);
        else if (other instanceof VoxelMeshCollider vm)
            return intersectsVoxelMesh(vm);
        return false;
    }

    @Override
    public @NotNull Vector3f resolve(@NotNull Collider other)
    {
        if (other instanceof BoxCollider box)
            return resolveWithBox(box);
        else if (other instanceof VoxelMeshCollider vm)
            return resolveWithVoxelMesh(vm);

        return new Vector3f();
    }

    @Override
    public @Nullable Float sweepTest(@NotNull Collider other, @NotNull Vector3f displacement)
    {
        if (other instanceof BoxCollider box)
            return sweepTestBox(box, displacement);
        else if (other instanceof VoxelMeshCollider vm)
            return sweepTestVoxelMesh(vm, displacement);

        return null;
    }

    @Override
    public @Nullable Vector3f rayIntersect(@NotNull Vector3f origin, @NotNull Vector3f direction)
    {
        Vector3f closestHit = null;

        float closestDistance = Float.MAX_VALUE;

        for (Vector3f voxel : voxels)
        {
            Vector3f voxelWorldPosition = new Vector3f(getPosition()).add(voxel);
            Vector3f voxelHalf = new Vector3f(0.5f, 0.5f, 0.5f);

            Vector3f voxelMin = voxelWorldPosition.sub(voxelHalf, new Vector3f());
            Vector3f voxelMax = voxelWorldPosition.add(voxelHalf, new Vector3f());

            Vector3f hit = Collider.rayIntersectGeneric(voxelMin, voxelMax, origin, direction);

            if (hit != null)
            {
                float distance = hit.distance(origin);

                if (distance < closestDistance)
                {
                    closestDistance = distance;
                    closestHit = hit;
                }
            }
        }

        return closestHit;
    }

    @Override
    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getWorldPosition();
    }

    public void setVoxels(@NotNull List<Vector3f> positions)
    {
        voxels.clear();
        voxels.addAll(positions);
    }

    public @NotNull List<Vector3f> getVoxels()
    {
        return voxels;
    }

    private boolean intersectsBox(BoxCollider box)
    {
        Vector3f boxPos = box.getPosition();
        Vector3f boxHalf = new Vector3f(box.getSize()).mul(0.5f);

        Vector3f thisPos = getPosition();

        for (Vector3f v : voxels)
        {
            Vector3f voxelPos = new Vector3f(thisPos).add(v);

            Vector3f minV = new Vector3f(voxelPos).sub(0.5f,0.5f,0.5f);
            Vector3f maxV = new Vector3f(voxelPos).add(0.5f,0.5f,0.5f);

            Vector3f minB = new Vector3f(boxPos).sub(boxHalf);
            Vector3f maxB = new Vector3f(boxPos).add(boxHalf);

            if (aabbIntersect(minV, maxV, minB, maxB))
                return true;
        }

        return false;
    }

    private boolean intersectsVoxelMesh(VoxelMeshCollider other)
    {
        Vector3f thisPos = getPosition();
        Vector3f otherPos = other.getPosition();

        for (Vector3f va : voxels)
        {
            Vector3f voxelA = new Vector3f(thisPos).add(va);
            Vector3f minA = new Vector3f(voxelA).sub(new Vector3f(0.5f));
            Vector3f maxA = new Vector3f(voxelA).add(new Vector3f(0.5f));

            for (Vector3f vb : other.voxels)
            {
                Vector3f voxelB = new Vector3f(otherPos).add(vb);
                Vector3f minB = new Vector3f(voxelB).sub(new Vector3f(0.5f));
                Vector3f maxB = new Vector3f(voxelB).add(new Vector3f(0.5f));

                if (aabbIntersect(minA, maxA, minB, maxB))
                    return true;
            }
        }

        return false;
    }

    private @NotNull Vector3f resolveWithBox(BoxCollider box)
    {
        Vector3f thisPos = getPosition();
        Vector3f boxPos = box.getPosition();
        Vector3f boxHalf = new Vector3f(box.getSize()).mul(0.5f);

        float smallestOverlap = Float.MAX_VALUE;
        Vector3f finalMtv = new Vector3f();

        for (Vector3f v : voxels)
        {
            Vector3f voxelPos = new Vector3f(thisPos).add(v);
            Vector3f minV = new Vector3f(voxelPos).sub(new Vector3f(0.5f));
            Vector3f maxV = new Vector3f(voxelPos).add(new Vector3f(0.5f));

            Vector3f minB = new Vector3f(boxPos).sub(boxHalf);
            Vector3f maxB = new Vector3f(boxPos).add(boxHalf);

            Vector3f mtv = resolveGeneric(minB, maxB, minV, maxV);
            if (mtv != null)
            {
                float mag = mtv.lengthSquared();
                if (mag < smallestOverlap)
                {
                    smallestOverlap = mag;
                    finalMtv.set(mtv);
                }
            }
        }

        if (smallestOverlap < Float.MAX_VALUE)
        {
            box.getGameObject().getTransform().translate(finalMtv);

            return finalMtv;
        }

        return new Vector3f();
    }

    private @NotNull Vector3f resolveWithVoxelMesh(VoxelMeshCollider other)
    {
        Vector3f thisPos = getPosition();
        Vector3f otherPos = other.getPosition();

        float smallestOverlap = Float.MAX_VALUE;
        Vector3f finalMtv = new Vector3f();

        for (Vector3f va : voxels)
        {
            Vector3f voxelA = new Vector3f(thisPos).add(va);
            Vector3f minA = new Vector3f(voxelA).sub(new Vector3f(0.5f));
            Vector3f maxA = new Vector3f(voxelA).add(new Vector3f(0.5f));

            for (Vector3f vb : other.voxels)
            {
                Vector3f voxelB = new Vector3f(otherPos).add(vb);
                Vector3f minB = new Vector3f(voxelB).sub(new Vector3f(0.5f));
                Vector3f maxB = new Vector3f(voxelB).add(new Vector3f(0.5f));

                Vector3f mtv = resolveGeneric(minA, maxA, minB, maxB);
                if (mtv != null)
                {
                    float mag = mtv.lengthSquared();
                    if (mag < smallestOverlap)
                    {
                        smallestOverlap = mag;
                        finalMtv.set(mtv);
                    }
                }
            }
        }

        if (smallestOverlap < Float.MAX_VALUE)
        {
            getGameObject().getTransform().translate(finalMtv);
            return finalMtv;
        }

        return new Vector3f();
    }

    private @Nullable Float sweepTestBox(BoxCollider box, Vector3f displacement)
    {
        Vector3f thisPos = getPosition();

        Float earliest = null;

        List<Vector3f> voxelSnapshot = new ArrayList<>(voxels);
        for (Vector3f v : voxelSnapshot)
        {
            Vector3f voxelPos = new Vector3f(thisPos).add(v);
            Vector3f minV = new Vector3f(voxelPos).sub(new Vector3f(0.5f));
            Vector3f maxV = new Vector3f(voxelPos).add(new Vector3f(0.5f));

            Vector3f boxPos = box.getPosition();
            Vector3f boxHalf = new Vector3f(box.getSize()).mul(0.5f);
            Vector3f minB = new Vector3f(boxPos).sub(boxHalf);
            Vector3f maxB = new Vector3f(boxPos).add(boxHalf);

            Vector3f negDisplacement = new Vector3f(displacement).negate();

            Float t = SweptAABBTester.sweptAABB(minB, maxB, minV, maxV, negDisplacement);

            if (t != null && (earliest == null || t < earliest))
                earliest = t;
        }

        return earliest;
    }

    private @Nullable Float sweepTestVoxelMesh(VoxelMeshCollider other, Vector3f displacement)
    {
        Float earliest = null;

        Vector3f thisPos = getPosition();
        Vector3f otherPos = other.getPosition();

        for (Vector3f va : voxels)
        {
            Vector3f voxelA = new Vector3f(thisPos).add(va);
            Vector3f minA = new Vector3f(voxelA).sub(new Vector3f(0.5f));
            Vector3f maxA = new Vector3f(voxelA).add(new Vector3f(0.5f));

            for (Vector3f vb : other.voxels)
            {
                Vector3f voxelB = new Vector3f(otherPos).add(vb);
                Vector3f minB = new Vector3f(voxelB).sub(new Vector3f(0.5f));
                Vector3f maxB = new Vector3f(voxelB).add(new Vector3f(0.5f));

                Float t = SweptAABBTester.sweptAABB(minA, maxA, minB, maxB, displacement);

                if (t != null && (earliest == null || t < earliest))
                    earliest = t;
            }
        }

        return earliest;
    }

    public static @NotNull VoxelMeshCollider create()
    {
        return new VoxelMeshCollider();
    }
}