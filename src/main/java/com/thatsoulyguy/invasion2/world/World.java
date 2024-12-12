package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.math.Transform;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.util.CoordinateHelper;
import com.thatsoulyguy.invasion2.util.SerializableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CustomConstructor("create")
public class World extends Component
{
    public static final int WORLD_HEIGHT = 256;
    public static final int VERTICAL_CHUNKS = WORLD_HEIGHT / Chunk.SIZE;

    public static final byte RENDER_DISTANCE = 3;

    private @EffectivelyNotNull String name;

    public @Nullable Transform chunkLoader;

    private final @NotNull Set<Vector3i> loadedChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull Set<Vector3i> generatingChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull TerrainGenerator terrainGenerator = TerrainGenerator.create(
            0.006,
            Chunk.SIZE,
            12345L
    );

    private transient @EffectivelyNotNull ExecutorService chunkGenerationExecutor;

    private final SerializableObject chunkLock = new SerializableObject();

    private World() { }

    @Override
    public void initialize()
    {
        generatingChunks.clear();
        chunkGenerationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    }

    @Override
    public void update()
    {
        loadCloseChunks();
        unloadFarChunks();
    }

    public @NotNull Chunk generateChunk(@NotNull Vector3i chunkPosition)
    {
        GameObject object = GameObject.create("chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z);

        object.getTransform().setLocalPosition(CoordinateHelper.chunkToWorldCoordinates(chunkPosition));

        object.addComponent(Objects.requireNonNull(ShaderManager.get("default")));
        object.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        object.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        short[][][] blocks = new short[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

        terrainGenerator.generateBlocks(blocks, chunkPosition);

        object.addComponent(Chunk.create(blocks));

        Objects.requireNonNull(object.getComponent(Chunk.class)).onLoad();

        return Objects.requireNonNull(object.getComponent(Chunk.class));
    }

    public void unloadChunk(@NotNull Vector3i chunkPosition)
    {
        if (!loadedChunks.contains(chunkPosition))
        {
            System.err.println("Loaded chunks list does not contain key: " + chunkPosition + "!");
            return;
        }

        GameObjectManager.unregister("chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, true);

        loadedChunks.remove(chunkPosition);
    }

    public void loadCloseChunks()
    {
        if (chunkLoader == null)
            return;

        Vector3f playerWorldPosition = chunkLoader.getWorldPosition();

        Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

        for (int cx = playerChunkPosition.x - RENDER_DISTANCE; cx <= playerChunkPosition.x + RENDER_DISTANCE; cx++)
        {
            for (int cz = playerChunkPosition.z - RENDER_DISTANCE; cz <= playerChunkPosition.z + RENDER_DISTANCE; cz++)
            {
                for (int cy = 0; cy < VERTICAL_CHUNKS; cy++)
                {
                    Vector3i currentChunk = new Vector3i(cx, cy, cz);

                    if (loadedChunks.contains(currentChunk) || !generatingChunks.add(currentChunk))
                        continue;

                    chunkGenerationExecutor.submit(() ->
                    {
                        try
                        {
                            Chunk chunk = generateChunk(currentChunk);
                            loadedChunks.add(currentChunk);
                        }
                        catch (Exception e)
                        {
                            System.err.println("Error generating chunk " + currentChunk + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                        finally
                        {
                            generatingChunks.remove(currentChunk);
                        }
                    });
                }
            }
        }
    }

    private void unloadFarChunks()
    {
        synchronized (chunkLock)
        {
            if (chunkLoader == null)
                return;

            Vector3f playerWorldPosition = chunkLoader.getWorldPosition();
            Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

            int unloadDistance = RENDER_DISTANCE + 1;

            loadedChunks.removeIf(chunkPosition ->
            {
                int dx = Math.abs(chunkPosition.x - playerChunkPosition.x);
                int dz = Math.abs(chunkPosition.z - playerChunkPosition.z);

                if (dx > unloadDistance || dz > unloadDistance)
                {
                    unloadChunk(chunkPosition);
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    public void uninitialize()
    {
        chunkGenerationExecutor.shutdown();

        try
        {
            if (!chunkGenerationExecutor.awaitTermination(20, TimeUnit.SECONDS))
                chunkGenerationExecutor.shutdownNow();
        }
        catch (InterruptedException e)
        {
            chunkGenerationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static @NotNull World create(@NotNull String name)
    {
        World result = new World();

        result.name = name;

        return result;
    }
}