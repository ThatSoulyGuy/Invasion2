package com.thatsoulyguy.invasion2.util;

import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Static
public class FileHelper
{
    private FileHelper() { }

    public static @Nullable String getExtension(@NotNull String path)
    {
        int index = path.lastIndexOf('.');

        if(index == -1)
            return null;

        return path.substring(index + 1);
    }

    public static @NotNull String getName(@NotNull String path)
    {
        int index = path.lastIndexOf('/');

        if(index == -1)
            return path;

        return path.substring(index + 1);
    }

    public static @Nullable String getDirectory(@NotNull String path)
    {
        int index = path.lastIndexOf('/');

        if(index == -1)
            return null;

        return path.substring(0, index);
    }

    public static @NotNull String getFileName(@NotNull String path)
    {
        int index = path.lastIndexOf('.');

        if(index == -1)
            return path;

        return path.substring(0, index);
    }

    public static @Nullable String readFile(@NotNull String path)
    {
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(FileHelper.class.getResourceAsStream(path)))))
        {
            String line = "";

            while ((line = reader.readLine()) != null)
                result.append(line).append("\n");
        }
        catch (IOException e)
        {
            System.err.println("Couldn't find the file at " + path);
        }

        return result.toString();
    }
}