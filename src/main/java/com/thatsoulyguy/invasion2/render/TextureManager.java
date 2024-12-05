package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Texture.class)
public class TextureManager
{
    private static final @NotNull ConcurrentMap<String, Texture> registeredTextures = new ConcurrentHashMap<>();

    private TextureManager() { }

    public static void register(@NotNull Texture object)
    {
        registeredTextures.put(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        registeredTextures.remove(name);
    }

    public static @Nullable Texture get(@NotNull String name)
    {
        if (!registeredTextures.containsKey(name))
            return null;

        return registeredTextures.get(name);
    }

    public static @NotNull List<Texture> getAll()
    {
        return List.copyOf(registeredTextures.values());
    }

    public static void uninitialize()
    {
        registeredTextures.values().forEach(Texture::uninitialize_NoOverride);
    }
}