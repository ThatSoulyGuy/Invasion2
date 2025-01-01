package com.thatsoulyguy.invasion2.core;

import com.thatsoulyguy.invasion2.annotation.Static;

@Static
public class Time
{
    private static long lastTime = System.nanoTime();
    private static float deltaTime = 0;
    private static int frames = 0;
    private static float fps = 0;
    private static float timeElapsed = 0;

    private Time() { }

    public static float getDeltaTime()
    {
        if (deltaTime > 0.015)
            return 0.008f;

        return deltaTime;
    }

    public static float getFPS()
    {
        return fps;
    }

    public static void update()
    {
        long currentTime = System.nanoTime();

        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;

        frames++;
        timeElapsed += deltaTime;

        if (timeElapsed >= 1.0f)
        {
            fps = frames / timeElapsed;
            frames = 0;
            timeElapsed = 0;
        }
    }

    public static void reset()
    {
        lastTime = System.nanoTime();
        deltaTime = 0;
        frames = 0;
        fps = 0;
        timeElapsed = 0;
    }
}