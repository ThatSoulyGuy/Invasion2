package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.render.Camera;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(GameObject.class)
public class GameObjectManager
{
    private static final ConcurrentMap<String, GameObject> gameObjectMap = new ConcurrentHashMap<>();

    private GameObjectManager() { }

    public static void register(@NotNull GameObject object)
    {
        gameObjectMap.putIfAbsent(object.getName(), object);

        if (LevelManager.getCurrentLevel() != null)
            LevelManager.getCurrentLevel().addGameObject(object.getName());
    }

    public static void unregister(@NotNull String name)
    {
        unregister(name, false);
    }

    public static void unregister(@NotNull String name, boolean uninitialize)
    {
        if (gameObjectMap.containsKey(name) && LevelManager.getCurrentLevel() != null)
            LevelManager.getCurrentLevel().removeGameObject(name);

        if (uninitialize && gameObjectMap.containsKey(name))
            gameObjectMap.get(name).uninitialize();

        gameObjectMap.remove(name);
    }

    public static @Nullable GameObject get(@NotNull String name)
    {
        return gameObjectMap.getOrDefault(name, null);
    }

    public static @NotNull List<GameObject> getAll()
    {
        return List.copyOf(gameObjectMap.values());
    }

    public static void render(@Nullable Camera camera)
    {
        for (GameObject gameObject : gameObjectMap.values())
            gameObject.render(camera);
    }

    public static boolean has(@NotNull String name)
    {
        return gameObjectMap.containsKey(name);
    }

    public static void uninitialize()
    {
        gameObjectMap.values().forEach(GameObject::uninitialize);

        gameObjectMap.clear();
    }
}