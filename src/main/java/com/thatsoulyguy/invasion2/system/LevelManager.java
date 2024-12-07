package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Static
public class LevelManager
{
    private static final ConcurrentMap<String, Level> levels = new ConcurrentHashMap<>();
    private static @Nullable Level currentLevel = null;
    private static final AtomicBoolean isLoading = new AtomicBoolean(false);
    private static final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();

    private LevelManager() { }

    public static void createLevel(@NotNull String name, boolean setCurrent)
    {
        if (levels.containsKey(name))
        {
            System.err.println("Level '" + name + "' already exists.");
            return;
        }

        Level level = Level.create(name);
        levels.put(name, level);

        if (setCurrent)
            currentLevel = level;

        System.out.println("Level '" + name + "' created.");
    }

    public static boolean loadLevel(@NotNull String name, @NotNull String path)
    {
        if (isLoading.get())
        {
            System.err.println("Another level is currently loading. Cannot load level '" + name + "' now.");
            return false;
        }

        Level level = levels.get(name);

        if (level == null)
        {
            System.err.println("Level '" + name + "' does not exist.");
            return false;
        }

        isLoading.set(true);

        try {
            if (currentLevel != null)
                unloadCurrentLevel();

            String sanitizedPath = path.replace(".bin", "");

            for (String gameObjectName : level.getGameObjectNames())
            {
                File gameObjectFile = new File(sanitizedPath, gameObjectName + ".bin");

                try
                {
                    GameObject gameObject = GameObject.loadFromFile(gameObjectFile);

                    if (gameObject == null)
                        System.err.println("Failed to load GameObject '" + gameObjectName + "'.");
                }
                catch (IOException | ClassNotFoundException e)
                {
                    System.err.println("Error loading GameObject '" + gameObjectName + "': " + e.getMessage());
                }
            }

            currentLevel = level;

            System.out.println("Level '" + name + "' loaded.");

            return true;
        }
        finally {

            isLoading.set(false);
            processQueuedActions();
        }
    }

    public static void unloadCurrentLevel()
    {
        if (currentLevel == null)
        {
            System.err.println("No level is currently loaded.");
            return;
        }

        for (String gameObjectName : currentLevel.getGameObjectNames())
            GameObjectManager.unregister(gameObjectName);

        System.out.println("Level '" + currentLevel.getName() + "' unloaded.");
        currentLevel = null;
    }

    public static boolean saveLevel(@NotNull String name, @NotNull String path)
    {
        Level level = levels.get(name);

        if (level == null)
        {
            System.err.println("Level '" + name + "' does not exist.");
            return false;
        }

        File levelFile = new File(path, name + ".bin");
        level.saveToFile(levelFile);

        for (String gameObjectName : level.getGameObjectNames())
        {
            GameObject gameObject = GameObjectManager.get(gameObjectName);

            if (gameObject != null)
            {
                File levelDir = new File(path, name);

                if (!levelDir.exists() && !levelDir.mkdirs())
                {
                    System.err.println("Failed to create directory: " + levelDir.getAbsolutePath());
                    continue;
                }

                File gameObjectFile = new File(levelDir, gameObjectName + ".bin");

                try
                {
                    gameObject.saveToFile(gameObjectFile);
                }
                catch (IOException e)
                {
                    System.err.println("Failed to save GameObject '" + gameObjectName + "': " + e.getMessage());
                }
            }
            else
                System.err.println("GameObject '" + gameObjectName + "' does not exist in GameObjectManager.");
        }

        System.out.println("Level '" + name + "' saved to " + levelFile.getAbsolutePath());
        return true;
    }

    public static boolean loadLevelFromFile(@NotNull String path)
    {
        if (isLoading.get())
        {
            System.err.println("Another level is currently loading. Cannot load level from file now.");
            return false;
        }

        File levelFile = new File(path);

        Level level = Level.loadFromFile(levelFile);

        if (level == null)
        {
            System.err.println("Failed to load level from file: " + levelFile.getAbsolutePath());
            return false;
        }

        levels.put(level.getName(), level);

        return loadLevel(level.getName(), path);
    }

    public static boolean addGameObjectToCurrentLevel(@NotNull GameObject gameObject)
    {
        if (isLoading.get())
        {
            System.out.println("Level is loading. Queuing addGameObjectToCurrentLevel for GameObject '" + gameObject.getName() + "'.");
            actionQueue.add(() -> addGameObjectToCurrentLevel(gameObject));
            return true;
        }

        if (currentLevel == null)
        {
            System.err.println("No level is currently loaded.");
            return false;
        }

        if (!GameObjectManager.has(gameObject.getName()))
        {
            System.err.println("GameObject '" + gameObject.getName() + "' is not registered in GameObjectManager.");
            return false;
        }

        currentLevel.addGameObject(gameObject.getName());
        System.out.println("GameObject '" + gameObject.getName() + "' added to level '" + currentLevel.getName() + "'.");
        return true;
    }

    public static boolean removeGameObjectFromCurrentLevel(@NotNull GameObject gameObject)
    {
        if (isLoading.get())
        {
            System.out.println("Level is loading. Queuing removeGameObjectFromCurrentLevel for GameObject '" + gameObject.getName() + "'.");

            actionQueue.add(() -> removeGameObjectFromCurrentLevel(gameObject));

            return true;
        }

        if (currentLevel == null) {
            System.err.println("No level is currently loaded.");
            return false;
        }

        currentLevel.removeGameObject(gameObject.getName());

        System.out.println("GameObject '" + gameObject.getName() + "' removed from level '" + currentLevel.getName() + "'.");
        return true;
    }

    public static @NotNull List<String> getAllLevelNames()
    {
        return List.copyOf(levels.keySet());
    }

    public static boolean deleteLevel(@NotNull String name, @NotNull String path)
    {
        Level removed = levels.remove(name);

        if (removed == null)
        {
            System.err.println("Level '" + name + "' does not exist.");
            return false;
        }

        File levelFile = new File(path, name + ".bin");

        if (levelFile.exists())
        {
            if (!levelFile.delete())
            {
                System.err.println("Failed to delete level file: " + levelFile.getAbsolutePath());
                return false;
            }
        }

        for (String gameObjectName : removed.getGameObjectNames())
        {
            File gameObjectFile = new File(path, gameObjectName + ".bin");

            if (gameObjectFile.exists())
            {
                if (!gameObjectFile.delete())
                {
                    System.err.println("Failed to delete GameObject file: " + gameObjectFile.getAbsolutePath());
                    return false;
                }
            }
        }

        System.out.println("Level '" + name + "' deleted.");
        return true;
    }

    public static @Nullable Level getCurrentLevel() {
        return currentLevel;
    }

    private static void processQueuedActions()
    {
        while (!actionQueue.isEmpty())
        {
            Runnable action = actionQueue.poll();

            if (action != null)
                action.run();
        }
    }
}