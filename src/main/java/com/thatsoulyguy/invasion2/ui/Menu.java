package com.thatsoulyguy.invasion2.ui;

import org.jetbrains.annotations.NotNull;

public abstract class Menu
{
    protected Menu() { }

    public abstract void initialize();

    public void update() { }

    public static <T extends Menu> @NotNull T create(@NotNull Class<T> clazz)
    {
        try
        {
            T result = clazz.getDeclaredConstructor().newInstance();

            result.initialize();

            return result;
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Menu! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}