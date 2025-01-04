package com.thatsoulyguy.invasion2.thread;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

@Static
public class MainThreadExecutor
{
    private static final @NotNull ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private static @EffectivelyNotNull Thread mainThread;

    private MainThreadExecutor() { }

    public static void initialize()
    {
        mainThread = Thread.currentThread();
    }

    public static void submit(@NotNull Runnable task)
    {
        taskQueue.add(task);
    }

    public static void execute()
    {
        if (Thread.currentThread() != mainThread)
            throw new IllegalStateException("Tasks must be executed on the main thread!");

        while (!taskQueue.isEmpty())
        {
            Runnable task = taskQueue.poll();

            if (task != null)
                task.run();
        }
    }
}