package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.render.Camera;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Static
@Manager(GameObject.class)
public class GameObjectManager
{
    private static final @NotNull ConcurrentMap<String, GameObject> gameObjectMap = new ConcurrentHashMap<>();
    private static final @NotNull ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final @NotNull BlockingQueue<GameObject> uninitializeQueue = new LinkedBlockingQueue<>();
    private static final @NotNull AtomicBoolean isUpdating = new AtomicBoolean(false);

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
        GameObject gameObject = gameObjectMap.remove(name);

        if (gameObject == null)
            return;

        if (LevelManager.getCurrentLevel() != null)
            LevelManager.getCurrentLevel().removeGameObject(name);

        if (uninitialize)
            uninitializeQueue.add(gameObject);
    }

    public static @Nullable GameObject get(@NotNull String name)
    {
        return gameObjectMap.getOrDefault(name, null);
    }

    public static @NotNull List<GameObject> getAll()
    {
        return List.copyOf(gameObjectMap.values());
    }

    public static void update()
    {
        isUpdating.set(true);

        List<GameObject> allGameObjects = collectAllGameObjects();
        List<Future<?>> updateTasks = new ArrayList<>();

        for (GameObject gameObject : allGameObjects)
        {
            updateTasks.add(executor.submit(() ->
            {
                if (gameObject.isActive())
                    gameObject.update();
            }));
        }

        for (Future<?> task : updateTasks)
        {
            try
            {
                task.get(10, TimeUnit.SECONDS);
            }
            catch (TimeoutException e)
            {
                System.err.println("Update task timed out and may be hanging.");
                task.cancel(true);
            }
            catch (Exception e)
            {
                System.err.println("Error in GameObject update task: " + e.getMessage());
            }
        }

        isUpdating.set(false);

        processUninitializeQueue();
    }

    private static List<GameObject> collectAllGameObjects()
    {
        List<GameObject> list = new ArrayList<>();
        Set<GameObject> visited = ConcurrentHashMap.newKeySet();

        for (GameObject gameObject : gameObjectMap.values())
            traverse(gameObject, list, visited);

        return list;
    }

    private static void traverse(GameObject gameObject, List<GameObject> list, Set<GameObject> visited)
    {
        if (gameObject == null || !visited.add(gameObject))
            return;
        list.add(gameObject);

        for (GameObject child : gameObject.getChildren())
            traverse(child, list, visited);
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

    private static void processUninitializeQueue()
    {
        while (!uninitializeQueue.isEmpty())
        {
            GameObject gameObject = uninitializeQueue.poll();

            if (gameObject != null)
                gameObject.uninitialize();
        }
    }

    public static void uninitialize()
    {
        executor.shutdown();

        try
        {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES))
            {
                executor.shutdownNow();

                if (!executor.awaitTermination(1, TimeUnit.MINUTES))
                    System.err.println("Executor did not terminate.");
            }
        }
        catch (InterruptedException e)
        {
            executor.shutdownNow();

            Thread.currentThread().interrupt();
            System.err.println("GameObject update tasks were interrupted.");
        }

        for (GameObject gameObject : gameObjectMap.values())
            gameObject.uninitialize();

        gameObjectMap.clear();
    }
}