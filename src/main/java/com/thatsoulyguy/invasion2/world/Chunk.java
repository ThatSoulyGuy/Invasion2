package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        List<Vector3f> voxelPositions = new ArrayList<>();

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

                    if (shouldRenderFace(new Vector3i(x, y, z + 1)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(0, 0, 1), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[2], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[2], 180));
                    }

                    if (shouldRenderFace(new Vector3i(x, y, z - 1)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(0, 0, -1), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[3], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[3], 180));
                    }

                    if (shouldRenderFace(new Vector3i(x, y + 1, z)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(0, 1, 0), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[0], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[0]));
                    }

                    if (shouldRenderFace(new Vector3i(x, y - 1, z)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(0, -1, 0), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[1], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[1]));
                    }

                    if (shouldRenderFace(new Vector3i(x + 1, y, z)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(1, 0, 0), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[4], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[4], -90));
                    }

                    if (shouldRenderFace(new Vector3i(x - 1, y, z)))
                    {
                        if (!voxelPositions.contains(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f)))
                            voxelPositions.add(new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f));

                        addFace(new Vector3i(x, y, z), new Vector3i(-1, 0, 0), Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getColors()[5], textureAtlas.getSubTextureCoordinates(Objects.requireNonNull(BlockRegistry.get(blocks[x][y][z])).getTextures()[5], 90));
                    }
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

            mesh.onLoad();

            collider.setVoxels(voxelPositions);
        }
    }

    public void setBlock(@NotNull Vector3i blockPosition, short type)
    {
        if (blockPosition.x < 0 || blockPosition.x >= SIZE || blockPosition.y < 0 || blockPosition.y >= SIZE || blockPosition.z < 0 || blockPosition.z >= SIZE)
            return;

        if (blocks[blockPosition.x][blockPosition.y][blockPosition.z] == type)
            return;

        blocks[blockPosition.x][blockPosition.y][blockPosition.z] = type;

        onLoad();
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        if (position.x < 0 || position.x >= SIZE || position.y < 0 || position.y >= SIZE || position.z < 0 || position.z >= SIZE)
            return true;

        return blocks[position.x][position.y][position.z] == BlockRegistry.BLOCK_AIR.getID();
    }

    private void addFace(@NotNull Vector3i position, @NotNull Vector3i normal, @NotNull Vector3f color, @Nullable Vector2f[] uvs)
    {
        Vector3i[] faceVertices = getFaceVerticesForNormal(position, normal);

        if (uvs == null)
            return;

        for (int i = 0; i < 4; i++)
        {
            this.vertices.add(Vertex.create
            (
                new Vector3f(faceVertices[i].x, faceVertices[i].y, faceVertices[i].z),
                color,
                new Vector3f(normal.x, normal.y, normal.z),
                uvs[i]
            ));
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

    private Vector3i[] getFaceVerticesForNormal(Vector3i position, Vector3i normal)
    {
        int x = position.x;
        int y = position.y;
        int z = position.z;

        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
        {
            return new Vector3i[]
            {
                new Vector3i(x,   y,   z+1),
                new Vector3i(x,   y+1, z+1),
                new Vector3i(x+1, y+1, z+1),
                new Vector3i(x+1, y,   z+1)
            };
        }
        else if (normal.x == 0 && normal.y == 0 && normal.z == -1)
        {
            return new Vector3i[]
            {
                new Vector3i(x+1, y,   z),
                new Vector3i(x+1, y+1, z),
                new Vector3i(x,   y+1, z),
                new Vector3i(x,   y,   z)
            };
        }
        else if (normal.x == 0 && normal.y == 1 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x,   y+1, z),
                new Vector3i(x,   y+1, z+1),
                new Vector3i(x+1, y+1, z+1),
                new Vector3i(x+1, y+1, z)
            };
        }
        else if (normal.x == 0 && normal.y == -1 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x+1, y, z),
                new Vector3i(x+1, y, z+1),
                new Vector3i(x,   y, z+1),
                new Vector3i(x,   y, z)
            };
        }
        else if (normal.x == 1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x+1, y,   z),
                new Vector3i(x+1, y,   z+1),
                new Vector3i(x+1, y+1, z+1),
                new Vector3i(x+1, y+1, z)
            };
        }
        else if (normal.x == -1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x, y+1, z),
                new Vector3i(x, y+1, z+1),
                new Vector3i(x, y,   z+1),
                new Vector3i(x, y,   z)
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