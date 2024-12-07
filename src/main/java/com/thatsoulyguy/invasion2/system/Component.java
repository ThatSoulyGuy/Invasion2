package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.render.Camera;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

public abstract class Component implements Serializable
{
    private @EffectivelyNotNull transient GameObject gameObject;

    public void initialize() { }
    public void update() { }
    public void render(@Nullable Camera camera) { }
    public void uninitialize() { }

    public void onLoad() { }
    public void onUnload() { }

    public void setGameObject(@NotNull GameObject gameObject)
    {
        this.gameObject = gameObject;
    }

    public @NotNull GameObject getGameObject()
    {
        return gameObject;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }
}