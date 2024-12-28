package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.core.Time;
import com.thatsoulyguy.invasion2.ui.UIManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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

    public static void saveLevel(@NotNull String name, @NotNull String rawPath)
    {
        String path = rawPath + "/" + name + "/";

        File saveDirectory = new File(path);

        if (!saveDirectory.exists())
            saveDirectory.mkdirs();

        if (!saveDirectory.isDirectory())
        {
            System.err.println("Save level path must be a directory!");
            return;
        }

        if (!levels.containsKey(name))
        {
            System.err.println("Level: '" + name + "' does not exist!");
            return;
        }

        System.out.println("Saving level '" + name + "'...");

        UIManager.serialize(path);

        File gameObjectsDirectory = new File(path, "gameObject/");

        if (!gameObjectsDirectory.exists())
            gameObjectsDirectory.mkdirs();

        File saveFile = new File(path, "level.bin");

        Level level = levels.get(name);
        List<GameObject> gameObjects = GameObjectManager.getAll();

        try (FileOutputStream fileOutputStream = new FileOutputStream(saveFile))
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeUTF(level.getName());

            objectOutputStream.writeInt(level.getGameObjectNames().size());

            level.getGameObjectNames().forEach((object ->
            {
                try
                {
                    objectOutputStream.writeUTF(object);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }));

            System.out.println("Saved game object names");

            gameObjects.forEach((gameObject -> gameObject.save(new File(gameObjectsDirectory, gameObject.getName() + ".bin"))));

            objectOutputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to serialize level! " + exception.getMessage());
        }

        System.out.println("Level '" + name + "' saved!");
    }

    public static void loadLevel(@NotNull String path, boolean setCurrent)
    {
        File saveDirectory = new File(path);

        if (!saveDirectory.isDirectory() || !saveDirectory.exists())
        {
            System.err.println("Save level path must be a directory and must exist!");
            return;
        }

        UIManager.deserialize(path);

        File saveFile = new File(path, "level.bin");

        System.out.println("Loading level '" + saveDirectory.toPath().getName(saveDirectory.toPath().getNameCount() - 1) + "'...");

        File gameObjectsDirectory = new File(path, "gameObject/");

        if (!gameObjectsDirectory.exists())
        {
            System.err.println("Game objects directory does not exist!");
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(saveFile))
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            Level level = Level.create(objectInputStream.readUTF());

            int gameObjectNamesCount = objectInputStream.readInt();

            for (int n = 0; n < gameObjectNamesCount; n++)
                level.addGameObject(objectInputStream.readUTF());

            for (int n = 0; n < gameObjectNamesCount; n++)
                GameObjectManager.register(GameObject.load(new File(gameObjectsDirectory, level.getGameObjectNames().get(n) + ".bin")));

            objectInputStream.close();

            levels.putIfAbsent(level.getName(), level);

            if (setCurrent)
                currentLevel = level;
        }
        catch (Exception exception)
        {
            System.err.println("Failed to deserialize level! " + exception.getMessage());
        }

        Time.reset();
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

    public static @NotNull List<String> getAllLevelNames()
    {
        return List.copyOf(levels.keySet());
    }

    public static @Nullable Level getCurrentLevel()
    {
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