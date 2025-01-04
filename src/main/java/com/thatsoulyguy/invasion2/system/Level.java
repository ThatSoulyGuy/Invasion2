package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;

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
        else
            System.err.println("Game object: '" + gameObjectName + "' already exists!");
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
}
