package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.block.Block;
import com.thatsoulyguy.invasion2.block.BlockRegistry;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.Vertex;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import com.thatsoulyguy.invasion2.util.ChunkAlgorithms;
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

    private short[][][] blocks;

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
                    if (blocks[x][y][z] == BlockRegistry.BLOCK_AIR.getID())
                        continue;

                    TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);

                    if (textureAtlas == null)
                    {
                        System.err.println("Texture atlas was not found on chunk object!");
                        return;
                    }

                    Block block = Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z]));

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
        if (shouldRenderFace(new Vector3i(x, y, z + 1)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(0, 0, 1),
                    block.getColors()[2],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[2], 180)
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
        }

        if (shouldRenderFace(new Vector3i(x, y, z - 1)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(0, 0, -1),
                    block.getColors()[3],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[3], 180)
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
        }

        if (shouldRenderFace(new Vector3i(x, y + 1, z)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(0, 1, 0),
                    block.getColors()[0],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[0])
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
        }

        if (shouldRenderFace(new Vector3i(x, y - 1, z)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(0, -1, 0),
                    block.getColors()[1],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[1])
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
        }

        if (shouldRenderFace(new Vector3i(x + 1, y, z)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(1, 0, 0),
                    block.getColors()[4],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[4], -90)
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
        }

        if (shouldRenderFace(new Vector3i(x - 1, y, z)))
        {
            addFace(
                    new Vector3i(x, y, z),
                    new Vector3i(-1, 0, 0),
                    block.getColors()[5],
                    textureAtlas.getSubTextureCoordinates(block.getTextures()[5], 90)
            );

            addRenderingVoxelPosition(renderingVoxelPositions, x, y, z);
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
    public void setBlock(@NotNull Vector3i blockPosition, short type)
    {
        if (blockPosition.x < 0 || blockPosition.x >= SIZE || blockPosition.y < 0 || blockPosition.y >= SIZE || blockPosition.z < 0 || blockPosition.z >= SIZE)
            return;

        if (blocks[blockPosition.x][blockPosition.y][blockPosition.z] == type)
            return;

        blocks[blockPosition.x][blockPosition.y][blockPosition.z] = type;

        rebuildMeshAndCollider();
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        if (position.x < 0 || position.x >= SIZE ||
                position.y < 0 || position.y >= SIZE ||
                position.z < 0 || position.z >= SIZE)
            return true;

        return blocks[position.x][position.y][position.z] == BlockRegistry.BLOCK_AIR.getID();
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
                    short blockID = blocks[x][y][z];

                    if (blockID == BlockRegistry.BLOCK_AIR.getID())
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

        float[] lighting = ChunkAlgorithms.getAmbientOcclusionLighting(position, normal, blocks);

        Vector3i[] faceVertices = getFaceVerticesForNormal(position, normal);

        for (int i = 0; i < 4; i++)
        {
            Vector3f aoColor = new Vector3f(baseColor).mul(lighting[i]);
            vertices.add(Vertex.create(new Vector3f(faceVertices[i]), aoColor, new Vector3f(normal.x, normal.y, normal.z), uvs[i]));
        }

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

        result.blocks = blocks;

        return result;
    }
}