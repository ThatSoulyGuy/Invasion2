package com.thatsoulyguy.invasion2.ui;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public abstract class Menu implements Serializable, Cloneable
{
    protected Menu() { }

    public abstract void initialize();

    public abstract @NotNull String getRegistryName();

    public void update() { }

    public static <T extends Menu> @NotNull T create(@NotNull Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Menu! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }

    @Override
    public Menu clone()
    {
        try
        {
            return (Menu) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }
}