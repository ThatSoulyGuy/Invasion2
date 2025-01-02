package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.block.Block;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.Vertex;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

@CustomConstructor("create")
public class Chunk extends Component
{
    public static final byte SIZE = 16;

    private transient List<Vertex> vertices = new ArrayList<>();
    private transient List<Integer> indices = new ArrayList<>();

    private Map<Integer, Short> blocks = new HashMap<>();

    private Chunk() { }

    @Override
    public void onLoad()
    {
        vertices = new ArrayList<>();
        indices = new ArrayList<>();

        List<Vector3f> renderingVoxelPositions = new ArrayList<>();

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);
                    if (blockId == BlockRegistry.BLOCK_AIR.getId()) continue;

                    TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);

                    if (textureAtlas == null)
                    {
                        System.err.println("Texture atlas was not found on chunk object!");
                        return;
                    }

                    Block block = Objects.requireNonNull(BlockRegistry.get(blockId));
                    renderFaceIfNeeded(x, y, z, textureAtlas, block, renderingVoxelPositions);
                }
            }
        }

        Mesh mesh = getGameObject().getComponent(Mesh.class);
        VoxelMeshCollider collider = getGameObject().getComponent(VoxelMeshCollider.class);

        if (mesh == null)
        {
            System.err.println("Mesh component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (collider == null)
        {
            System.err.println("VoxelMeshCollider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        mesh.setTransient(true);
        collider.setTransient(true);

        if (!vertices.isEmpty() && !indices.isEmpty())
        {
            MainThreadExecutor.submit(() ->
            {
                mesh.setVertices(vertices);
                mesh.setIndices(indices);
            });

            collider.setVoxels(renderingVoxelPositions);

            mesh.onLoad();
        }
    }

    private void renderFaceIfNeeded(int x, int y, int z, TextureAtlas textureAtlas, Block block, List<Vector3f> renderingVoxelPositions)
    {
        Vector3f basePosition = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        if (!renderingVoxelPositions.contains(basePosition))
            renderingVoxelPositions.add(basePosition);

        Vector3i[] directions =
                {
                new Vector3i(0, 0, 1),
                new Vector3i(0, 0, -1),
                new Vector3i(0, 1, 0),
                new Vector3i(0, -1, 0),
                new Vector3i(1, 0, 0),
                new Vector3i(-1, 0, 0)
        };

        int[] textureRotations = {180, 180, 0, 0, -90, 90};
        int[] colorIndices = {2, 3, 0, 1, 4, 5};

        for (int i = 0; i < directions.length; i++)
        {
            Vector3i direction = directions[i];

            int colorIndex = colorIndices[i];
            int rotation = textureRotations[i];

            Vector3i neighborPosition = new Vector3i(x + direction.x, y + direction.y, z + direction.z);

            if (shouldRenderFace(neighborPosition))
            {
                addFace(
                        new Vector3i(x, y, z),
                        direction,
                        block.getColors()[colorIndex],
                        textureAtlas.getSubTextureCoordinates(block.getTextures()[colorIndex], rotation)
                );
            }
        }
    }

    private void addRenderingVoxelPosition(@NotNull List<Vector3f> voxelPositions, int x, int y, int z)
    {
        Vector3f position = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        if (!voxelPositions.contains(position))
            voxelPositions.add(position);
    }

    /**
     * Sets the block at the given position to 'type' and updates the chunk mesh.
     * If you "break" a block (set it to air), this will remove its faces.
     * If you place a new block, it'll add its faces.
     *
     * @param blockPosition The (x, y, z) position in chunk space
     * @param type The block ID to place
     */
    public void setBlock(@NotNull Vector3i blockPosition, short type) {
        if (!isValidPosition(blockPosition)) return;

        int index = toIndex(blockPosition.x, blockPosition.y, blockPosition.z);
        if (type == BlockRegistry.BLOCK_AIR.getId()) {
            blocks.remove(index);
        } else {
            blocks.put(index, type);
        }

        rebuildMeshAndCollider();
    }

    /**
     * Gets the type of the block at the specified position.
     * Returns -1 if the block is outside the bounds of the chunk
     *
     * @param blockPosition The position in block coordinates
     * @return The type of block retrieved
     */
    public short getBlock(@NotNull Vector3i blockPosition)
    {
        if (!isValidPosition(blockPosition))
            return -1;

        return getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
    }

    private short getBlock(int x, int y, int z)
    {
        int index = toIndex(x, y, z);
        return blocks.getOrDefault(index, BlockRegistry.BLOCK_AIR.getId());
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        if (!isValidPosition(position))
            return true;

        return getBlock(position.x, position.y, position.z) == BlockRegistry.BLOCK_AIR.getId();
    }

    private boolean isValidPosition(@NotNull Vector3i position)
    {
        return position.x >= 0 && position.x < SIZE &&
                position.y >= 0 && position.y < SIZE &&
                position.z >= 0 && position.z < SIZE;
    }

    private static int toIndex(int x, int y, int z)
    {
        return (x & 0xF) | ((y & 0xF) << 4) | ((z & 0xF) << 8);
    }

    private void rebuildMeshAndCollider()
    {
        vertices.clear();
        indices.clear();

        List<Vector3f> renderingVoxelPositions = new ArrayList<>();

        TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);
        if (textureAtlas == null)
        {
            System.err.println("Texture atlas was not found on chunk object!");
            return;
        }

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockID = getBlock(x, y, z);

                    if (blockID == BlockRegistry.BLOCK_AIR.getId())
                        continue;

                    Block block = Objects.requireNonNull(BlockRegistry.get(blockID));
                    renderFaceIfNeeded(x, y, z, textureAtlas, block, renderingVoxelPositions);
                }
            }
        }

        Mesh mesh = getGameObject().getComponent(Mesh.class);
        VoxelMeshCollider collider = getGameObject().getComponent(VoxelMeshCollider.class);

        if (mesh == null)
        {
            System.err.println("Mesh component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (collider == null)
        {
            System.err.println("VoxelMeshCollider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (!vertices.isEmpty() && !indices.isEmpty())
        {
            mesh.setTransient(true);
            collider.setTransient(true);

            MainThreadExecutor.submit(() -> mesh.modify(
                    vertList ->
                    {
                        vertList.clear();
                        vertList.addAll(vertices);
                    },
                    idxList ->
                    {
                        idxList.clear();
                        idxList.addAll(indices);
                    }
            ));

            collider.setVoxels(renderingVoxelPositions);
        }
        else
        {
            MainThreadExecutor.submit(() -> mesh.modify
            (
                    List::clear,
                    List::clear
            ));

            collider.setVoxels(Collections.emptyList());
        }
    }

    private void addFace(@NotNull Vector3i position, @NotNull Vector3i normal, @NotNull Vector3f baseColor, @Nullable Vector2f[] uvs)
    {
        if (uvs == null)
            return;

        Vector3i[] faceVertices = getFaceVerticesForNormal(position, normal);

        for (int i = 0; i < 4; i++)
            vertices.add(Vertex.create(new Vector3f(faceVertices[i]), baseColor, new Vector3f(normal.x, normal.y, normal.z), uvs[i]));

        int startIndex = this.vertices.size() - 4;

        boolean isTop = (normal.x == 0 && normal.y == 1 && normal.z == 0);
        boolean isBottom = (normal.x == 0 && normal.y == -1 && normal.z == 0);

        if (isTop || isBottom)
        {
            indices.add(startIndex);
            indices.add(startIndex + 1);
            indices.add(startIndex + 2);
            indices.add(startIndex + 2);
            indices.add(startIndex + 3);
            indices.add(startIndex);
        }
        else
        {
            indices.add(startIndex);
            indices.add(startIndex + 2);
            indices.add(startIndex + 1);
            indices.add(startIndex + 2);
            indices.add(startIndex);
            indices.add(startIndex + 3);
        }
    }

    private static float[] getAmbientOcclusionSubstituteLighting(@NotNull Vector3i normal)
    {
        float[] ao;

        if (normal.y > 0)
            ao = new float[]
            {
                1.0f,
                1.0f,
                1.0f,
                1.0f
            };
        else if (normal.z < 0)
            ao = new float[]
            {
                0.65f,
                0.65f,
                0.65f,
                0.65f
            };
        else if (normal.x < 0)
            ao = new float[]
            {
                0.85f,
                0.85f,
                0.85f,
                0.85f
            };
        else
            ao = new float[]
            {
                1.0f,
                1.0f,
                1.0f,
                1.0f
            };

        return ao;
    }

    private @NotNull Vector3i[] getFaceVerticesForNormal(@NotNull Vector3i position, @NotNull Vector3i normal)
    {
        int x = position.x;
        int y = position.y;
        int z = position.z;

        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
        {
            return new Vector3i[]{
                    new Vector3i(x, y, z + 1),
                    new Vector3i(x, y + 1, z + 1),
                    new Vector3i(x + 1, y + 1, z + 1),
                    new Vector3i(x + 1, y, z + 1)
            };
        }
        else if (normal.x == 0 && normal.y == 0 && normal.z == -1)
        {
            return new Vector3i[]{
                    new Vector3i(x + 1, y, z),
                    new Vector3i(x + 1, y + 1, z),
                    new Vector3i(x, y + 1, z),
                    new Vector3i(x, y, z)
            };
        }
        else if (normal.x == 0 && normal.y == 1 && normal.z == 0)
        {
            return new Vector3i[]{
                    new Vector3i(x, y + 1, z),
                    new Vector3i(x, y + 1, z + 1),
                    new Vector3i(x + 1, y + 1, z + 1),
                    new Vector3i(x + 1, y + 1, z)
            };
        }
        else if (normal.x == 0 && normal.y == -1 && normal.z == 0)
        {
            return new Vector3i[]
                    {
                            new Vector3i(x + 1, y, z),
                            new Vector3i(x + 1, y, z + 1),
                            new Vector3i(x, y, z + 1),
                            new Vector3i(x, y, z)
                    };
        }
        else if (normal.x == 1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
                    {
                            new Vector3i(x + 1, y, z),
                            new Vector3i(x + 1, y, z + 1),
                            new Vector3i(x + 1, y + 1, z + 1),
                            new Vector3i(x + 1, y + 1, z)
                    };
        }
        else if (normal.x == -1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
                    {
                            new Vector3i(x, y + 1, z),
                            new Vector3i(x, y + 1, z + 1),
                            new Vector3i(x, y, z + 1),
                            new Vector3i(x, y, z)
                    };
        }

        return new Vector3i[]
                {
                        new Vector3i(x, y, z),
                        new Vector3i(x, y, z),
                        new Vector3i(x, y, z),
                        new Vector3i(x, y, z)
                };
    }

    public static @NotNull Chunk create()
    {
        return create(new short[SIZE][SIZE][SIZE]);
    }

    public static @NotNull Chunk create(short[][][] blocks)
    {
        Chunk result = new Chunk();

        Map<Integer, Short> blockMap = new HashMap<>();

        for (int x = 0; x < blocks.length; x++)
        {
            for (int y = 0; y < blocks[x].length; y++)
            {
                for (int z = 0; z < blocks[x][y].length; z++)
                {
                    short blockType = blocks[x][y][z];

                    if (blockType != BlockRegistry.BLOCK_AIR.getId())
                    {
                        int index = toIndex(x, y, z);

                        blockMap.put(index, blockType);
                    }
                }
            }
        }

        result.blocks = blockMap;

        return result;
    }
}