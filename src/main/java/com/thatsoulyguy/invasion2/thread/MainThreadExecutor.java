package com.thatsoulyguy.invasion2.thread;

import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@Static
public class MainThreadExecutor
{
    private static final ConcurrentLinkedQueue<FutureTask<Void>> taskQueue = new ConcurrentLinkedQueue<>();
    private static Thread mainThread;

    private MainThreadExecutor() { }

    public static void initialize()
    {
        mainThread = Thread.currentThread();
    }

    public static Future<Void> submit(Callable<Void> task)
    {
        FutureTask<Void> futureTask = new FutureTask<>(task);

        taskQueue.add(futureTask);

        return futureTask;
    }

    public static Future<Void> submit(Runnable task)
    {
        return submit(() ->
        {
            task.run();
            return null;
        });
    }

    public static void execute()
    {
        if (Thread.currentThread() != mainThread)
        {
            throw new IllegalStateException("Tasks must be executed on the main thread!");
        }

        while (!taskQueue.isEmpty())
        {
            FutureTask<Void> task = taskQueue.poll();

            if (task != null)
                task.run();
        }
    }
}