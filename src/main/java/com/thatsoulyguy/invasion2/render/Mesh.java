package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.core.Settings;
import com.thatsoulyguy.invasion2.core.Window;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.thread.MainThreadExecutor;
import com.thatsoulyguy.invasion2.world.TextureAtlas;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@CustomConstructor("create")
public class Mesh extends Component
{
    private final @NotNull List<Vertex> vertices = new CopyOnWriteArrayList<>();
    private final @NotNull List<Integer> indices = new CopyOnWriteArrayList<>();

    private transient int vao, vbo, cbo, nbo, uvbo, ibo;

    private boolean isTransparent = false;

    private transient @EffectivelyNotNull CountDownLatch initializationLatch = new CountDownLatch(1);

    private Mesh() { }

    @Override
    public void initialize()
    {
        initializationLatch = new CountDownLatch(1);

        vao = -1;
        vbo = -1;
        cbo = -1;
        nbo = -1;
        uvbo = -1;
        ibo = -1;
    }

    @Override
    public void onLoad()
    {
        if (vao == -1)
        {
            MainThreadExecutor.submit(() ->
            {
                try
                {
                    createOrUpdateBuffers();
                }
                finally
                {
                    initializationLatch.countDown();
                }
            });
        }
        else
        {
            MainThreadExecutor.submit(() ->
            {
                try
                {
                    updateBufferData();
                }
                finally
                {
                    initializationLatch.countDown();
                }
            });
        }
    }

    @Override
    public void renderDefault(@Nullable Camera camera)
    {
        if (!checkInitialization() || camera == null)
            return;

        if (isTransparent)
        {
            GL41.glEnable(GL41.GL_BLEND);
            GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
        }

        Texture texture = getGameObject().getComponent(Texture.class);

        if (texture == null)
            texture = Objects.requireNonNull(getGameObject().getComponent(TextureAtlas.class)).getOutputTexture();

        Shader shader = Settings.DEFAULT_RENDERING_SHADER.getValue();

        GL41.glBindVertexArray(vao);

        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glEnableVertexAttribArray(2);
        GL41.glEnableVertexAttribArray(3);

        assert texture != null;

        texture.bind(0);
        shader.bind();

        shader.setShaderUniform("diffuseTexture", 0);
        shader.setShaderUniform("projection", camera.getProjectionMatrix());
        shader.setShaderUniform("view", camera.getViewMatrix());

        shader.setShaderUniform("model", getGameObject().getTransform().getModelMatrix());

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        shader.unbind();
        texture.unbind();

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glDisableVertexAttribArray(2);
        GL41.glDisableVertexAttribArray(3);
        GL41.glBindVertexArray(0);

        if (isTransparent)
            GL41.glDisable(GL41.GL_BLEND);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (renderDefault): " + error);
    }

    @Override
    public void renderUI()
    {
        if (!checkInitialization())
            return;

        GL41.glDisable(GL41.GL_CULL_FACE);

        if (isTransparent)
        {
            GL41.glEnable(GL41.GL_BLEND);
            GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
        }

        Texture texture = getGameObject().getComponent(Texture.class);

        if (texture == null)
            texture = Objects.requireNonNull(getGameObject().getComponent(TextureAtlas.class)).getOutputTexture();

        Shader shader = getGameObject().getComponent(Shader.class);
        if (texture == null || shader == null)
        {
            System.err.println("Shader or Texture component(s) missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        Vector2i windowDimensions = Window.getDimensions();
        int windowWidth = windowDimensions.x;
        int windowHeight = windowDimensions.y;

        Matrix4f projectionMatrix = new Matrix4f().ortho2D(0, windowWidth, windowHeight, 0);

        GL41.glBindVertexArray(vao);

        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glEnableVertexAttribArray(2);
        GL41.glEnableVertexAttribArray(3);

        texture.bind(0);
        shader.bind();

        shader.setShaderUniform("diffuse", 0);
        shader.setShaderUniform("projection", projectionMatrix);

        shader.setShaderUniform("model", getGameObject().getTransform().getModelMatrix());

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        shader.unbind();
        texture.unbind();

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glDisableVertexAttribArray(2);
        GL41.glDisableVertexAttribArray(3);
        GL41.glBindVertexArray(0);

        GL41.glEnable(GL41.GL_CULL_FACE);

        if (isTransparent)
            GL41.glDisable(GL41.GL_BLEND);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (renderUI): " + error);
    }

    /**
     * Provide read-only access to vertices.
     */
    public @NotNull List<Vertex> getVertices()
    {
        return List.copyOf(vertices);
    }

    /**
     * Provide read-only access to indices.
     */
    public @NotNull List<Integer> getIndices()
    {
        return List.copyOf(indices);
    }

    /**
     * Set the vertex list completely (replaces the old one).
     */
    public void setVertices(@NotNull List<Vertex> vertices)
    {
        synchronized (this.vertices)
        {
            this.vertices.clear();
            this.vertices.addAll(vertices);
        }
    }

    /**
     * Set the index list completely (replaces the old one).
     */
    public void setIndices(@NotNull List<Integer> indices)
    {
        synchronized (this.indices)
        {
            this.indices.clear();
            this.indices.addAll(indices);
        }
    }

    public void setTransparent(boolean transparent)
    {
        isTransparent = transparent;
    }

    public boolean isTransparent()
    {
        return isTransparent;
    }

    /**
     * Modify existing vertices and/or indices in-place. Once complete,
     * the data is re-uploaded to the GPU so the changes appear in the mesh.
     *
     * @param vertexModifier The consumer for modifying the vertices
     * @param indexModifier  The consumer for modifying the indices
     */
    public void modify(@NotNull Consumer<List<Vertex>> vertexModifier, @NotNull Consumer<List<Integer>> indexModifier)
    {
        synchronized (this.vertices)
        {
            vertexModifier.accept(vertices);
            indexModifier.accept(indices);

            if (vao != -1)
                MainThreadExecutor.submit(this::updateBufferData);
        }
    }

    private boolean checkInitialization()
    {
        if (initializationLatch.getCount() != 0)
            return false;

        return vao != -1 && vbo != -1 && cbo != -1 && uvbo != -1 && ibo != -1;
    }

    private void createOrUpdateBuffers()
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

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (createOrUpdateBuffers):" + error);
    }

    private void updateBufferData()
    {
        GL41.glBindVertexArray(vao);

        FloatBuffer positionBuffer = toBuffer(vertices, Vertex::getPosition);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
        resizeOrSubData(GL41.GL_ARRAY_BUFFER, positionBuffer);

        FloatBuffer colorBuffer = toBuffer(vertices, Vertex::getColor);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, cbo);
        resizeOrSubData(GL41.GL_ARRAY_BUFFER, colorBuffer);

        // Update normal buffer
        FloatBuffer normalBuffer = toBuffer(vertices, Vertex::getNormal);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, nbo);
        resizeOrSubData(GL41.GL_ARRAY_BUFFER, normalBuffer);

        FloatBuffer uvBuffer = toBuffer(vertices, Vertex::getUVs);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, uvbo);
        resizeOrSubData(GL41.GL_ARRAY_BUFFER, uvBuffer);

        IntBuffer indexBuffer = toBuffer(indices);
        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);
        resizeOrSubData(GL41.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);

        GL41.glBindVertexArray(0);

        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (updateBufferData): " + error);
    }

    private <T extends Buffer> void resizeOrSubData(int target, T data)
    {
        int newSize = data.capacity() * (data instanceof FloatBuffer ? Float.BYTES : Integer.BYTES);
        int currentSize = GL41.glGetBufferParameteri(target, GL41.GL_BUFFER_SIZE);

        if (newSize > currentSize)
            GL41.glBufferData(target, newSize, GL41.GL_DYNAMIC_DRAW);

        if (data instanceof FloatBuffer buffer)
            GL41.glBufferSubData(target, 0, buffer);
        else if (data instanceof IntBuffer buffer)
            GL41.glBufferSubData(target, 0, buffer);
    }

    private static <T> FloatBuffer toBuffer(List<Vertex> vertices, Function<Vertex, T> extractor)
    {
        if (vertices.isEmpty())
            throw new IllegalArgumentException("The list of vertices cannot be empty.");

        Object sample = extractor.apply(vertices.getFirst());

        final int dimensions;

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
            if (vector instanceof Vector3f vec3)
                buffer.put(vec3.x).put(vec3.y).put(vec3.z);
            else if (vector instanceof Vector2f vec2)
                buffer.put(vec2.x).put(vec2.y);
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