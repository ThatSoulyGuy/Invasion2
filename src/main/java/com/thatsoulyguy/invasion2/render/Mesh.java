package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@CustomConstructor("create")
public class Mesh extends Component
{
    private final @NotNull List<Vertex> vertices = Collections.synchronizedList(new ArrayList<>());
    private final @NotNull List<Integer> indices = Collections.synchronizedList(new ArrayList<>());

    private transient int vao, vbo, cbo, nbo, uvbo, ibo;

    private @Nullable transient Future<Void> initializationFuture;

    private Mesh() { }

    @Override
    public void onLoad()
    {
        if (vao == 0)
        {
            initializationFuture = MainThreadExecutor.submit(() ->
            {
                vao = GL41.glGenVertexArrays();
                GL41.glBindVertexArray(vao);

                vbo = GL41.glGenBuffers();
                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getPosition), GL41.GL_DYNAMIC_DRAW);
                GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 0, 0);
                GL41.glEnableVertexAttribArray(0);

                cbo = GL41.glGenBuffers();
                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, cbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getColor), GL41.GL_DYNAMIC_DRAW);
                GL41.glVertexAttribPointer(1, 3, GL41.GL_FLOAT, false, 0, 0);
                GL41.glEnableVertexAttribArray(1);

                nbo = GL41.glGenBuffers();
                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, nbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getNormal), GL41.GL_DYNAMIC_DRAW);
                GL41.glVertexAttribPointer(2, 3, GL41.GL_FLOAT, false, 0, 0);
                GL41.glEnableVertexAttribArray(2);

                uvbo = GL41.glGenBuffers();
                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, uvbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getUVs), GL41.GL_DYNAMIC_DRAW);
                GL41.glVertexAttribPointer(3, 2, GL41.GL_FLOAT, false, 0, 0);
                GL41.glEnableVertexAttribArray(3);

                ibo = GL41.glGenBuffers();
                GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);
                GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, toBuffer(indices), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindVertexArray(0);
            });
        }
        else
        {
            initializationFuture = MainThreadExecutor.submit(() ->
            {
                GL41.glBindVertexArray(vao);

                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getPosition), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, cbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getColor), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, nbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getNormal), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, uvbo);
                GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getUVs), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);
                GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, toBuffer(indices), GL41.GL_DYNAMIC_DRAW);

                GL41.glBindVertexArray(0);
            });
        }
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        try
        {
            if (initializationFuture != null)
                initializationFuture.get(0, TimeUnit.SECONDS);
        }
        catch (TimeoutException | InterruptedException | ExecutionException _)
        {
            return;
        }

        if (initializationFuture == null)
            return;

        if (camera == null)
            return;

        Texture texture = getGameObject().getComponent(Texture.class);

        if (texture == null)
            texture = Objects.requireNonNull(getGameObject().getComponent(TextureAtlas.class)).getOutputTexture();

        Shader shader = getGameObject().getComponent(Shader.class);

        if (texture == null || shader == null)
        {
            System.err.println("Shader or Texture component(s) missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        GL41.glBindVertexArray(vao);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at VAO binding: " + error + " | " + this);

        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glEnableVertexAttribArray(2);
        GL41.glEnableVertexAttribArray(3);

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at vertex attribute array binding: " + error + " | " + this);

        texture.bind(0);

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at texture binding: " + error + " | " + this);

        shader.bind();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at shader binding: " + error + " | " + this);

        shader.setShaderUniform("projection", camera.getProjectionMatrix());
        shader.setShaderUniform("view", camera.getViewMatrix());
        shader.setShaderUniform("model", getGameObject().getTransform().getModelMatrix());

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at shader uniform binding: " + error + " | " + this);

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at drawing: " + error + " | " + this);

        shader.unbind();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at shader unbinding: " + error + " | " + this);

        texture.unbind();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at texture unbinding: " + error + " | " + this);

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glDisableVertexAttribArray(2);
        GL41.glDisableVertexAttribArray(3);

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at vertex attribute array unbinding: " + error + " | " + this);

        GL41.glBindVertexArray(0);

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL error at VAO unbinding: " + error + " | " + this);
    }

    public @NotNull List<Vertex> getVertices()
    {
        return Collections.unmodifiableList(vertices);
    }

    public @NotNull List<Integer> getIndices()
    {
        return Collections.unmodifiableList(indices);
    }

    public void setVertices(@NotNull List<Vertex> vertices)
    {
        this.vertices.clear();
        this.vertices.addAll(vertices);
    }

    public void setIndices(@NotNull List<Integer> indices)
    {
        this.indices.clear();
        this.indices.addAll(indices);
    }

    private static <T> FloatBuffer toBuffer(List<Vertex> vertices, Function<Vertex, T> extractor)
    {
        if (vertices.isEmpty())
            throw new IllegalArgumentException("The list of vertices cannot be empty.");

        int dimensions;
        
        Object sample = extractor.apply(vertices.getFirst());

        if (sample instanceof Vector3f)
            dimensions = 3;
        else if (sample instanceof Vector2f)
            dimensions = 2;
        else
            throw new IllegalArgumentException("Unsupported vector type: " + sample.getClass().getName());

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * dimensions);

        for (Vertex vertex : vertices)
        {
            T vector = extractor.apply(vertex);

            if (vector instanceof Vector3f vector3f)
                buffer.put(vector3f.x).put(vector3f.y).put(vector3f.z);
            else if (vector instanceof Vector2f vector2f)
                buffer.put(vector2f.x).put(vector2f.y);
            else
                throw new IllegalStateException("Unexpected vector type encountered.");
        }

        buffer.flip();
        return buffer;
    }

    private static IntBuffer toBuffer(List<Integer> indices)
    {
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.size());

        for (int index : indices)
            buffer.put(index);

        buffer.flip();

        return buffer;
    }

    @Override
    public void uninitialize()
    {
        MainThreadExecutor.submit(() ->
        {
            GL41.glDeleteVertexArrays(vao);
            GL41.glDeleteBuffers(vbo);
            GL41.glDeleteBuffers(cbo);
            GL41.glDeleteBuffers(nbo);
            GL41.glDeleteBuffers(uvbo);
            GL41.glDeleteBuffers(ibo);
        });
    }

    public static @NotNull Mesh create(@NotNull List<Vertex> vertices, @NotNull List<Integer> indices)
    {
        Mesh result = new Mesh();

        result.vertices.clear();
        result.indices.clear();

        result.vertices.addAll(vertices);
        result.indices.addAll(indices);

        return result;
    }
}