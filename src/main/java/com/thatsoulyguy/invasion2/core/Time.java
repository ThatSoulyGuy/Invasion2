package com.thatsoulyguy.invasion2.core;

import com.thatsoulyguy.invasion2.annotation.Static;

@Static
public class Time
{
    private static long lastTime = System.nanoTime();
    private static float deltaTime = 0;

    private Time() { }

    public static float getDeltaTime()
    {
        return deltaTime;
    }

    public static void update()
    {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;
    }

    public static void reset()
    {
        lastTime = System.nanoTime();
        deltaTime = 0;
    }
}