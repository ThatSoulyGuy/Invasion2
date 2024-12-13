package com.thatsoulyguy.invasion2.collider;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.system.GameObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Collider.class)
public class ColliderManager
{
    private static final ConcurrentMap<GameObject, Collider> colliderMap = new ConcurrentHashMap<>();

    private ColliderManager() { }

    public static void register(@NotNull Collider object)
    {
        colliderMap.putIfAbsent(object.getGameObject(), object);
    }

    public static void unregister(@NotNull GameObject object)
    {
        colliderMap.remove(object);
    }

    public static @Nullable Collider get(@NotNull Vector3f position)
    {
        return colliderMap.getOrDefault(position, null);
    }

    public static @NotNull List<Collider> getAll()
    {
        return List.copyOf(colliderMap.values());
    }
}