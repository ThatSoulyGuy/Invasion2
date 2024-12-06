package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CustomConstructor("create")
public class Level
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull List<String> gameObjectNames;

    private Level() { }

    public static @NotNull Level create(@NotNull String name)
    {
        Level result = new Level();
        result.name = name;
        result.gameObjectNames = new ArrayList<>();
        return result;
    }

    public void addGameObject(@NotNull String gameObjectName)
    {
        if (!gameObjectNames.contains(gameObjectName))
            gameObjectNames.add(gameObjectName);
    }

    public void removeGameObject(@NotNull String gameObjectName)
    {
        gameObjectNames.remove(gameObjectName);
    }

    public @NotNull List<String> getGameObjectNames()
    {
        return Collections.unmodifiableList(gameObjectNames);
    }

    public @NotNull String getName()
    {
        return name;
    }

    public void setName(@NotNull String name)
    {
        this.name = name;
    }

    public void setGameObjectNames(@NotNull List<String> gameObjectNames)
    {
        this.gameObjectNames.clear();
        this.gameObjectNames.addAll(gameObjectNames);
    }

    public void saveToFile(@NotNull File file)
    {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
        {
            dos.writeInt(name.length());
            dos.writeUTF(name);

            dos.writeInt(gameObjectNames.size());

            for (String gameObjectName : gameObjectNames)
            {
                dos.writeInt(gameObjectName.length());
                dos.writeUTF(gameObjectName);
            }
        }
        catch (IOException exception)
        {
            System.err.println("Failed to serialize level! " + exception.getMessage());
        }
    }

    public static @Nullable Level loadFromFile(@NotNull File file)
    {
        if (!file.exists())
        {
            System.err.println("Level file does not exist: " + file.getAbsolutePath());
            return null;
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file))))
        {
            int nameLength = dis.readInt();
            String levelName = dis.readUTF();

            Level level = Level.create(levelName);

            int gameObjectCount = dis.readInt();

            for (int i = 0; i < gameObjectCount; i++)
            {
                int objNameLength = dis.readInt();
                String gameObjectName = dis.readUTF();
                level.addGameObject(gameObjectName);
            }

            return level;
        }
        catch (IOException exception)
        {
            System.err.println("Failed to deserialize level! " + exception.getMessage());
            return null;
        }
    }
}
