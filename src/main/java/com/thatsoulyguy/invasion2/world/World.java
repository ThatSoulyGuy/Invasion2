package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.math.Transform;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.system.GameObjectManager;
import com.thatsoulyguy.invasion2.system.Layer;
import com.thatsoulyguy.invasion2.util.CoordinateHelper;
import com.thatsoulyguy.invasion2.util.SerializableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.*;

@CustomConstructor("create")
public class World extends Component
{
    public static final int WORLD_HEIGHT = 256;
    public static final int VERTICAL_CHUNKS = WORLD_HEIGHT / Chunk.SIZE;

    public static final byte RENDER_DISTANCE = 5;

    public long seed = 354576879657L;

    private @EffectivelyNotNull String name;

    public @Nullable Transform chunkLoader;

    private final @NotNull Set<Vector3i> loadedChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull Set<Vector3i> generatingChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull ConcurrentMap<Vector3i, Future<?>> ongoingChunkGenerations = new ConcurrentHashMap<>();

    private final @NotNull List<TerrainGenerator> terrainGenerators = new ArrayList<>();

    private transient @EffectivelyNotNull ExecutorService chunkGenerationExecutor;

    private final @NotNull SerializableObject chunkLock = new SerializableObject();

    private World() { }

    @Override
    public void initialize()
    {
        generatingChunks.clear();
        chunkGenerationExecutor = Executors.newFixedThreadPool(3);
    }

    @Override
    public void update()
    {
        loadCloseChunks();
        unloadFarChunks();
    }

    public @NotNull Chunk generateChunk(@NotNull Vector3i chunkPosition)
    {
        GameObject object = GameObject.create("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, Layer.DEFAULT);

        object.getTransform().setLocalPosition(CoordinateHelper.chunkToWorldCoordinates(chunkPosition));

        object.addComponent(Collider.create(VoxelMeshCollider.class));

        object.addComponent(Objects.requireNonNull(ShaderManager.get("pass.geometry")));
        object.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        object.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        short[][][] blocks = new short[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

        terrainGenerators.forEach(generator ->
        {
            generator.setSeed(seed);
            generator.setScale(0.006d);
            generator.generateBlocks(blocks, chunkPosition);
        });

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

        GameObjectManager.unregister("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, true);

        loadedChunks.remove(chunkPosition);
    }

    public @NotNull List<Vector3i> getLoadedChunks()
    {
        return loadedChunks.stream().toList();
    }

    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Sets the type of a block in the world.
     *
     * @param worldPosition The world position at which a block is set.
     * @param type The type of the block being set
     */
    public boolean setBlock(@NotNull Vector3f worldPosition, short type)
    {
        Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(worldPosition);
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(worldPosition);

        if (!loadedChunks.contains(chunkCoordinates))
            return false;
        else
        {
            Objects.requireNonNull(getChunk(chunkCoordinates)).setBlock(blockCoordinates, type);
            return true;
        }
    }

    /**
     * Gets the type of block in the world.
     * Returns -1 if no block is found
     *
     * @param worldPosition The position of the block in world coordinates
     * @return The type of the block
     */
    public short getBlock(@NotNull Vector3f worldPosition)
    {
        Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(worldPosition);
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(worldPosition);

        if (!loadedChunks.contains(chunkCoordinates))
            return -1;
        else
        {
            return Objects.requireNonNull(getChunk(chunkCoordinates)).getBlock(blockCoordinates);
        }
    }

    /**
     * Gets the type of block in the world.
     * Returns -1 if no block is found
     *
     * @param blockPosition The position of the block in global block coordinates
     * @return The type of the block
     */
    public short getBlock(@NotNull Vector3i blockPosition)
    {
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(CoordinateHelper.globalBlockToWorldCoordinates(blockPosition));

        if (!loadedChunks.contains(chunkCoordinates))
            return -1;
        else
        {
            return Objects.requireNonNull(getChunk(chunkCoordinates)).getBlock(blockPosition);
        }
    }

    public @Nullable Chunk getChunk(@NotNull Vector3i chunkPosition)
    {
        if (!loadedChunks.contains(chunkPosition))
            return null;

        return Objects.requireNonNull(GameObjectManager.get("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z)).getComponent(Chunk.class);
    }

    public static @NotNull World getLocalWorld()
    {
        return Objects.requireNonNull(Objects.requireNonNull(GameObjectManager.get("default.world")).getComponent(World.class));
    }

    public void addTerrainGenerator(@NotNull TerrainGenerator generator)
    {
        terrainGenerators.add(generator);
    }

    public void loadCloseChunks()
    {
        if (chunkLoader == null)
            return;

        Vector3f playerWorldPosition = chunkLoader.getWorldPosition();
        Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

        List<Vector3i> chunkPositions = new ArrayList<>();

        for (int cx = playerChunkPosition.x - RENDER_DISTANCE; cx <= playerChunkPosition.x + RENDER_DISTANCE; cx++)
        {
            for (int cz = playerChunkPosition.z - RENDER_DISTANCE; cz <= playerChunkPosition.z + RENDER_DISTANCE; cz++)
            {
                for (int cy = 0; cy < VERTICAL_CHUNKS; cy++)
                    chunkPositions.add(new Vector3i(cx, cy, cz));
            }
        }

        chunkPositions.sort(Comparator.comparingInt(pos -> Math.toIntExact(playerChunkPosition.distanceSquared(pos))));

        for (Vector3i currentChunk : chunkPositions)
        {
            if (loadedChunks.contains(currentChunk) || ongoingChunkGenerations.containsKey(currentChunk))
                continue;

            Future<?> future = chunkGenerationExecutor.submit(() ->
            {
                try
                {
                    generateChunk(currentChunk);

                    synchronized (chunkLock)
                    {
                        if (!loadedChunks.add(currentChunk))
                            System.err.println("Chunk already loaded: " + currentChunk);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Error generating chunk " + currentChunk + ": " + e.getMessage());
                    e.printStackTrace();
                }
                finally
                {
                    ongoingChunkGenerations.remove(currentChunk);
                }
            });

            ongoingChunkGenerations.put(currentChunk, future);
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