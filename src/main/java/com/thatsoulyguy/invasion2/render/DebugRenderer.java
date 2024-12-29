package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.util.FileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Static
public class DebugRenderer //TODO: Restructure this entire disaster
{
    private static final ConcurrentHashMap<Line, Boolean> linesToRender = new ConcurrentHashMap<>();
    private static int bufferSize = 0;

    private static ShaderProgram shaderProgram;
    private static int vaoId;
    private static int vboId;

    private DebugRenderer() { }

    /**
     * Initializes the DebugRenderer by setting up shaders, VAO, and VBO.
     */
    public static void initialize()
    {
        try
        {
            shaderProgram = new ShaderProgram("/assets/invasion2/shader/debugVertex.glsl", "/assets/invasion2/shader/debugFragment.glsl");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        vaoId = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vaoId);

        vboId = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);

        int initialBufferSize = 10000 * 2 * 6;
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, initialBufferSize * Float.BYTES, GL41.GL_DYNAMIC_DRAW);

        int stride = 6 * Float.BYTES;

        GL41.glEnableVertexAttribArray(0);
        GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, stride, 0);

        GL41.glEnableVertexAttribArray(1);
        GL41.glVertexAttribPointer(1, 3, GL41.GL_FLOAT, false, stride, 3 * Float.BYTES);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
        GL41.glBindVertexArray(0);
    }


    /**
     * Cleans up the DebugRenderer by deleting shaders, VAO, and VBO.
     */
    public static void uninitialize()
    {
        if (shaderProgram != null)
            shaderProgram.uninitialize();

        if (vaoId != 0)
            GL41.glDeleteVertexArrays(vaoId);

        if (vboId != 0)
            GL41.glDeleteBuffers(vboId);
    }

    /**
     * Adds a box defined by its minimum and maximum corners.
     *
     * @param min   The minimum (x, y, z) corner of the box.
     * @param max   The maximum (x, y, z) corner of the box.
     * @param color The color of the box lines.
     */
    public static void addBox(@NotNull Vector3f min, @NotNull Vector3f max, @NotNull Vector3f color)
    {
        Vector3f v1 = new Vector3f(min.x, min.y, min.z);
        Vector3f v2 = new Vector3f(max.x, min.y, min.z);
        Vector3f v3 = new Vector3f(max.x, max.y, min.z);
        Vector3f v4 = new Vector3f(min.x, max.y, min.z);
        Vector3f v5 = new Vector3f(min.x, min.y, max.z);
        Vector3f v6 = new Vector3f(max.x, min.y, max.z);
        Vector3f v7 = new Vector3f(max.x, max.y, max.z);
        Vector3f v8 = new Vector3f(min.x, max.y, max.z);

        addLine(v1, v2, color);
        addLine(v2, v3, color);
        addLine(v3, v4, color);
        addLine(v4, v1, color);

        addLine(v5, v6, color);
        addLine(v6, v7, color);
        addLine(v7, v8, color);
        addLine(v8, v5, color);

        addLine(v1, v5, color);
        addLine(v2, v6, color);
        addLine(v3, v7, color);
        addLine(v4, v8, color);
    }

    /**
     * Adds a line defined by two vertices.
     *
     * @param start The start vertex of the line.
     * @param end   The end vertex of the line.
     * @param color The color of the line.
     */
    public static void addLine(@NotNull Vector3f start, @NotNull Vector3f end, @NotNull Vector3f color)
    {
        Line line = new Line(start, end, color);

        if (linesToRender.containsKey(line))
            bufferSize += 2 * 2 * 6;

        linesToRender.putIfAbsent(line, Boolean.TRUE);
    }

    /**
     * Renders all queued lines.
     *
     * @param camera The camera used to obtain projection and view matrices.
     */
    public static void render(@Nullable Camera camera)
    {
        if (linesToRender.isEmpty() || camera == null)
            return;

        shaderProgram.bind();

        int projLoc = GL41.glGetUniformLocation(shaderProgram.getProgramId(), "projection");
        int viewLoc = GL41.glGetUniformLocation(shaderProgram.getProgramId(), "view");

        FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
        camera.getProjectionMatrix().get(projBuffer);
        GL41.glUniformMatrix4fv(projLoc, false, projBuffer);

        FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        camera.getViewMatrix().get(viewBuffer);
        GL41.glUniformMatrix4fv(viewLoc, false, viewBuffer);

        GL41.glBindVertexArray(vaoId);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboId);

        int requiredBufferSize = linesToRender.size() * 2 * 6;

        int currentBufferSize = GL41.glGetBufferParameteri(GL41.GL_ARRAY_BUFFER, GL41.GL_BUFFER_SIZE);

        if (requiredBufferSize * Float.BYTES > currentBufferSize)
        {
            while (requiredBufferSize * Float.BYTES > currentBufferSize)
                currentBufferSize *= 2;

            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, currentBufferSize, GL41.GL_DYNAMIC_DRAW);
        }

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(requiredBufferSize);

        try
        {
            for (Line line : linesToRender.keySet())
            {
                vertexBuffer.put(line.start.x).put(line.start.y).put(line.start.z);
                vertexBuffer.put(line.color.x).put(line.color.y).put(line.color.z);

                vertexBuffer.put(line.end.x).put(line.end.y).put(line.end.z);
                vertexBuffer.put(line.color.x).put(line.color.y).put(line.color.z);
            }
        }
        catch (Exception _) { } //TODO: Avoid this

        vertexBuffer.flip();

        GL41.glBufferSubData(GL41.GL_ARRAY_BUFFER, 0L, vertexBuffer);

        int vertexCount = linesToRender.size() * 2;
        GL41.glLineWidth(3);
        GL41.glDrawArrays(GL41.GL_LINES, 0, vertexCount);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
        GL41.glBindVertexArray(0);

        shaderProgram.unbind();

        linesToRender.clear();
    }

    public static class Line
    {
        public Vector3f start;
        public Vector3f end;
        public Vector3f color;

        public Line(Vector3f start, Vector3f end, Vector3f color)
        {
            this.start = start;
            this.end = end;
            this.color = color;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (!(obj instanceof Line line))
                return false;

            return start.equals(line.start) && end.equals(line.end) && color.equals(line.color);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(start, end, color);
        }
    }

    public static class ShaderProgram
    {
        private final int programId;

        public ShaderProgram(String vertexPath, String fragmentPath) throws Exception
        {
            programId = GL41.glCreateProgram();

            if (programId == 0)
                throw new Exception("Could not create Shader");

            int vertexShaderId = createShader(FileHelper.readFile(vertexPath), GL41.GL_VERTEX_SHADER);
            int fragmentShaderId = createShader(FileHelper.readFile(fragmentPath), GL41.GL_FRAGMENT_SHADER);

            GL41.glAttachShader(programId, vertexShaderId);
            GL41.glAttachShader(programId, fragmentShaderId);

            GL41.glLinkProgram(programId);

            if (GL41.glGetProgrami(programId, GL41.GL_LINK_STATUS) == 0)
                throw new Exception("Error linking Shader code: " + GL41.glGetProgramInfoLog(programId, 1024));

            GL41.glDetachShader(programId, vertexShaderId);
            GL41.glDetachShader(programId, fragmentShaderId);
            GL41.glDeleteShader(vertexShaderId);
            GL41.glDeleteShader(fragmentShaderId);
        }

        private int createShader(String shaderCode, int shaderType) throws Exception
        {
            int shaderId = GL41.glCreateShader(shaderType);

            if (shaderId == 0)
                throw new Exception("Error creating shader. Type: " + shaderType);

            GL41.glShaderSource(shaderId, shaderCode);
            GL41.glCompileShader(shaderId);

            if (GL41.glGetShaderi(shaderId, GL41.GL_COMPILE_STATUS) == 0)
                throw new Exception("Error compiling Shader code: " + GL41.glGetShaderInfoLog(shaderId, 1024));

            return shaderId;
        }

        public void bind()
        {
            GL41.glUseProgram(programId);
        }

        public void unbind()
        {
            GL41.glUseProgram(0);
        }

        public void uninitialize()
        {
            GL41.glDeleteProgram(programId);
        }

        public int getProgramId()
        {
            return programId;
        }
    }
}