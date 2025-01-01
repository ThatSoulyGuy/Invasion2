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
    private boolean isTransient = false;

    public void initialize() { }
    public void updateMainThread() { }
    public void update() { }
    public void renderDefault(@Nullable Camera camera) { }
    public void renderUI() { }
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

    public void setTransient(boolean isTransient)
    {
        this.isTransient = isTransient;
    }

    public boolean getTransient()
    {
        return isTransient;
    }
}