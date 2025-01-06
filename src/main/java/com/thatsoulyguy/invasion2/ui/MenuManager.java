package com.thatsoulyguy.invasion2.ui;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Menu.class)
public class MenuManager
{
    private static final @NotNull ConcurrentMap<String, Menu> menuMap = new ConcurrentHashMap<>();

    private MenuManager() { }

    public static void register(@NotNull Menu object)
    {
        menuMap.putIfAbsent(object.getRegistryName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        menuMap.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return menuMap.containsKey(name);
    }

    public static @Nullable Menu get(@NotNull String name)
    {
        Menu instance = menuMap.getOrDefault(name, null).clone();

        if (instance == null)
            return null;

        instance.initialize();

        return instance;
    }

    public static @NotNull List<Menu> getAll()
    {
        return List.copyOf(menuMap.values());
    }

    public static void uninitialize() { }
}