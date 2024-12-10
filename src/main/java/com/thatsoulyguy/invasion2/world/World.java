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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@CustomConstructor("create")
public class World extends Component
{
    public static final byte RENDER_DISTANCE = 3;

    private @EffectivelyNotNull String name;

    public @Nullable Transform chunkLoader;

    private final List<Vector3i> loadedChunks = Collections.synchronizedList(new ArrayList<>());

    private World() { }

    @Override
    public void update()
    {
        loadCloseChunks();
        unloadFarChunks();
    }

    public @NotNull Chunk generateChunk(@NotNull Vector3i chunkPosition)
    {
        GameObject object = GameObject.create("chunk_" +  chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z);

        object.getTransform().setLocalPosition(CoordinateHelper.chunkToWorldCoordinates(chunkPosition));

        object.addComponent(Objects.requireNonNull(ShaderManager.get("default")));
        object.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        object.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        object.addComponent(Chunk.create());

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

        GameObjectManager.unregister("chunk_" +  chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, true);

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
            for (int cy = playerChunkPosition.y - RENDER_DISTANCE; cy <= playerChunkPosition.y + RENDER_DISTANCE; cy++)
            {
                for (int cz = playerChunkPosition.z - RENDER_DISTANCE; cz <= playerChunkPosition.z + RENDER_DISTANCE; cz++)
                {
                    Vector3i currentChunk = new Vector3i(cx, cy, cz);

                    if (loadedChunks.contains(currentChunk))
                        continue;

                    Chunk newChunk = generateChunk(currentChunk);
                    loadedChunks.add(currentChunk);
                }
            }
        }
    }

    private void unloadFarChunks()
    {
        if (chunkLoader == null)
            return;

        Vector3f playerWorldPosition = chunkLoader.getWorldPosition();
        Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

        int unloadDistance = RENDER_DISTANCE + 1;

        loadedChunks.removeIf(chunkPosition ->
        {
            int dx = Math.abs(chunkPosition.x - playerChunkPosition.x);
            int dy = Math.abs(chunkPosition.y - playerChunkPosition.y);
            int dz = Math.abs(chunkPosition.z - playerChunkPosition.z);

            if (dx > unloadDistance || dy > unloadDistance || dz > unloadDistance)
            {
                unloadChunk(chunkPosition);

                return true;
            }

            return false;
        });
    }

    public static @NotNull World create(@NotNull String name)
    {
        World result = new World();

        result.name = name;

        return result;
    }
}