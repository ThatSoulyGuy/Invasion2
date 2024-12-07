package com.thatsoulyguy.invasion2.entity;

import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;

public abstract class Entity extends Component
{
    private float currentHealth;

    protected Entity()
    {
        currentHealth = getMaximumHealth();
    }

    public abstract String getDisplayName();

    public abstract String getRegistryName();

    public abstract float getWalkingSpeed();

    public abstract float getRunningSpeed();

    public abstract float getMaximumHealth();

    public void setCurrentHealth(float currentHealth)
    {
        this.currentHealth = currentHealth;
    }

    public float getCurrentHealth()
    {
        return currentHealth;
    }

    public static <T extends Entity> @NotNull T create(Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Entity! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}