package com.thatsoulyguy.invasion2.collider;

import org.joml.Vector3f;

public class SweptAABBTester
{
    public static Float sweptAABB(Vector3f AMin, Vector3f AMax, Vector3f BMin, Vector3f BMax, Vector3f velocity)
    {
        if (velocity.lengthSquared() < 1e-12f)
            return Collider.aabbIntersect(AMin, AMax, BMin, BMax) ? 0.0f : null;

        FloatRef tEnter = new FloatRef(0.0f);
        FloatRef tExit = new FloatRef(1.0f);

        if (!sweptAABB1D(AMin.x, AMax.x, BMin.x, BMax.x, velocity.x, tEnter, tExit))
            return null;

        if (!sweptAABB1D(AMin.y, AMax.y, BMin.y, BMax.y, velocity.y, tEnter, tExit))
            return null;

        if (!sweptAABB1D(AMin.z, AMax.z, BMin.z, BMax.z, velocity.z, tEnter, tExit))
            return null;

        if (tEnter.value > tExit.value || tEnter.value > 1.0f || tEnter.value < 0.0f)
            return null;

        return tEnter.value;
    }

    public static Float sweptAABBRef(Vector3f AMin, Vector3f AMax, Vector3f BMin, Vector3f BMax, Vector3f velocity)
    {
        if (velocity.lengthSquared() < 1e-12f)
            return Collider.aabbIntersect(AMin, AMax, BMin, BMax) ? 0.0f : null;

        FloatRef tEnter = new FloatRef(0.0f);
        FloatRef tExit = new FloatRef(1.0f);

        if (!sweptAABB1DRef(AMin.x, AMax.x, BMin.x, BMax.x, velocity.x, tEnter, tExit))
            return null;

        if (!sweptAABB1DRef(AMin.y, AMax.y, BMin.y, BMax.y, velocity.y, tEnter, tExit))
            return null;

        if (!sweptAABB1DRef(AMin.z, AMax.z, BMin.z, BMax.z, velocity.z, tEnter, tExit))
            return null;

        if (tEnter.value > tExit.value || tEnter.value > 1.0f || tEnter.value < 0.0f)
            return null;

        return tEnter.value;
    }

    private static boolean sweptAABB1D(float AMin, float AMax, float BMin, float BMax, float v, FloatRef tEnter, FloatRef tExit)
    {
        return sweptAABB1DRef(AMin, AMax, BMin, BMax, v, tEnter, tExit);
    }

    private static boolean sweptAABB1DRef(float AMin, float AMax, float BMin, float BMax, float v, FloatRef tEnter, FloatRef tExit)
    {
        if (Math.abs(v) < 1e-12f)
        {
            return !(AMax < BMin) && !(AMin > BMax);
        }

        float t1 = (BMin - AMax) / v;
        float t2 = (BMax - AMin) / v;

        float ent = tEnter.value;
        float ex = tExit.value;

        if (t1 > t2)
        {
            float temp = t1;
            t1 = t2;
            t2 = temp;
        }

        if (t1 > ent)
            ent = t1;

        if (t2 < ex)
            ex = t2;

        if (ent > ex)
            return false;

        tEnter.value = ent;
        tExit.value = ex;

        return true;
    }

    static class FloatRef
    {
        float value;
        FloatRef(float v) {value=v;}
    }
}