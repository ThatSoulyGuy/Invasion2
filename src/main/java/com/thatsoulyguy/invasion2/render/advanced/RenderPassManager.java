package com.thatsoulyguy.invasion2.render.advanced;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.advanced.core.RenderPass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(RenderPass.class)
public class RenderPassManager
{
    private static final @NotNull ConcurrentMap<Class<? extends RenderPass>, RenderPass> renderPassMap = new ConcurrentHashMap<>();

    private RenderPassManager() { }

    public static void register(@NotNull RenderPass object)
    {
        renderPassMap.putIfAbsent(object.getClass(), object);
    }

    public static void unregister(@NotNull Class<? extends RenderPass> clazz)
    {
        renderPassMap.remove(clazz);
    }

    public static void render(@Nullable Camera camera)
    {
        renderPassMap.values().forEach(pass -> pass.render(camera));
    }

    public static boolean has(@NotNull Class<? extends RenderPass> clazz)
    {
        return renderPassMap.containsKey(clazz);
    }

    public static @Nullable RenderPass get(@NotNull Class<? extends RenderPass> clazz)
    {
        return renderPassMap.getOrDefault(clazz, null);
    }

    public static @NotNull List<RenderPass> getAll()
    {
        return List.copyOf(renderPassMap.values());
    }

    public static void uninitialize()
    {
        renderPassMap.values().forEach(RenderPass::uninitialize);
    }
}