package com.thatsoulyguy.invasion2.world;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.Vertex;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

@CustomConstructor("create")
public class Chunk extends Component
{
    public static final byte SIZE = 16;

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    private final short[][][] blocks = new short[SIZE][SIZE][SIZE];

    private Chunk() { }

    @Override
    public void initialize()
    {
        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    blocks[x][y][z] = 1;
                }
            }
        }
    }

    @Override
    public void onLoad()
    {
        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    if (blocks[x][y][z] == 0)
                        continue;

                    TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);

                    if (textureAtlas == null)
                    {
                        System.err.println("Texture atlas was not found on chunk object!");
                        return;
                    }

                    if (shouldRenderFace(new Vector3i(x, y, z + 1)))
                        addFace(new Vector3i(x, y, z), new Vector3i(0, 0, 1), textureAtlas.getSubTextureMap().get("brick"));

                    if (shouldRenderFace(new Vector3i(x, y, z - 1)))
                        addFace(new Vector3i(x, y, z), new Vector3i(0, 0, -1), textureAtlas.getSubTextureMap().get("brick"));

                    if (shouldRenderFace(new Vector3i(x, y + 1, z)))
                        addFace(new Vector3i(x, y, z), new Vector3i(0, 1, 0), textureAtlas.getSubTextureMap().get("brick"));

                    if (shouldRenderFace(new Vector3i(x, y - 1, z)))
                        addFace(new Vector3i(x, y, z), new Vector3i(0, -1, 0), textureAtlas.getSubTextureMap().get("brick"));

                    if (shouldRenderFace(new Vector3i(x + 1, y, z)))
                        addFace(new Vector3i(x, y, z), new Vector3i(1, 0, 0), textureAtlas.getSubTextureMap().get("brick"));

                    if (shouldRenderFace(new Vector3i(x - 1, y, z)))
                        addFace(new Vector3i(x, y, z), new Vector3i(-1, 0, 0), textureAtlas.getSubTextureMap().get("brick"));
                }
            }
        }

        Mesh mesh = getGameObject().getComponent(Mesh.class);

        if (mesh == null)
        {
            System.err.println("Mesh component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        mesh.onLoad();
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        if (position.x < 0 || position.x >= SIZE || position.y < 0 || position.y >= SIZE || position.z < 0 || position.z >= SIZE)
            return true;

        return blocks[position.x][position.y][position.z] == 0;
    }

    private void addFace(@NotNull Vector3i position, @NotNull Vector3i normal, @NotNull Vector2f[] uvs)
    {
        Vector3i[] faceVertices = getFaceVerticesForNormal(position, normal);

        for (int i = 0; i < 4; i++)
        {
            this.vertices.add(Vertex.create
            (
                new Vector3f(faceVertices[i].x, faceVertices[i].y, faceVertices[i].z),
                new Vector3f(1.0f),
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
        return new Chunk();
    }
}