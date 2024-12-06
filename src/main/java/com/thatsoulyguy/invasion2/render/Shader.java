package com.thatsoulyguy.invasion2.render;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.util.AssetPath;
import com.thatsoulyguy.invasion2.util.FileHelper;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;

@CustomConstructor("create")
public class Shader extends Component implements ManagerLinkedClass
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull AssetPath localPath;
    private @EffectivelyNotNull String vertexPath, fragmentPath;
    private @EffectivelyNotNull String vertexSource, fragmentSource;

    private int program = -1;

    private Shader() { }

    public void bind()
    {
        if (program == -1)
        {
            System.err.println("Shader '" + name + "' is invalid!");
            return;
        }

        GL41.glUseProgram(program);
    }

    public void unbind()
    {
        GL41.glUseProgram(0);
    }

    public void setShaderUniform(@NotNull String name, int value)
    {
        GL41.glUniform1i(GL41.glGetUniformLocation(program, name), value);
    }

    public void setShaderUniform(@NotNull String name, float value)
    {
        GL41.glUniform1f(GL41.glGetUniformLocation(program, name), value);
    }

    public void setShaderUniform(@NotNull String name, @NotNull Vector2f value)
    {
        GL41.glUniform2f(GL41.glGetUniformLocation(program, name), value.x, value.y);
    }

    public void setShaderUniform(@NotNull String name, @NotNull Vector3f value)
    {
        GL41.glUniform3f(GL41.glGetUniformLocation(program, name), value.x, value.y, value.z);
    }

    public void setShaderUniform(@NotNull String name, @NotNull Vector4f value)
    {
        GL41.glUniform4f(GL41.glGetUniformLocation(program, name), value.x, value.y, value.z, value.w);
    }

    public void setShaderUniform(@NotNull String name, @NotNull Matrix4f value)
    {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

        value.get(buffer);

        GL41.glUniformMatrix4fv(GL41.glGetUniformLocation(program, name), false, buffer);
    }

    @Override
    public void onLoad()
    {
        int vertex = GL41.glCreateShader(GL41.GL_VERTEX_SHADER);
        GL41.glShaderSource(vertex, vertexSource);
        GL41.glCompileShader(vertex);

        if(GL41.glGetShaderi(vertex, GL41.GL_COMPILE_STATUS) == GL41.GL_FALSE)
            throw new RuntimeException("Failed to compile vertex shader: " + GL41.glGetShaderInfoLog(vertex));

        int fragment = GL41.glCreateShader(GL41.GL_FRAGMENT_SHADER);
        GL41.glShaderSource(fragment, fragmentSource);
        GL41.glCompileShader(fragment);

        if(GL41.glGetShaderi(fragment, GL41.GL_COMPILE_STATUS) == GL41.GL_FALSE)
            throw new RuntimeException("Failed to compile fragment shader: " + GL41.glGetShaderInfoLog(fragment));

        program = GL41.glCreateProgram();

        GL41.glAttachShader(program, vertex);
        GL41.glAttachShader(program, fragment);
        GL41.glLinkProgram(program);

        if(GL41.glGetProgrami(program, GL41.GL_LINK_STATUS) == GL41.GL_FALSE)
            throw new RuntimeException("Failed to link program: " + GL41.glGetProgramInfoLog(program));

        GL41.glDeleteShader(vertex);
        GL41.glDeleteShader(fragment);
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull AssetPath getLocalPath()
    {
        return localPath;
    }

    public @NotNull String getVertexPath()
    {
        return vertexPath;
    }

    public @NotNull String getFragmentPath()
    {
        return fragmentPath;
    }

    @Override
    public @NotNull Class<?> getManagingClass()
    {
        return ShaderManager.class;
    }

    @Override
    public @NotNull String getManagedItem()
    {
        return name;
    }

    public void uninitialize_NoOverride()
    {
        GL41.glDeleteProgram(program);
    }

    public static @NotNull Shader create(@NotNull String name, @NotNull AssetPath localPath)
    {
        Shader result = new Shader();

        result.name = name;
        result.localPath = localPath;
        result.vertexPath = localPath.getFullPath() + "Vertex.glsl";
        result.fragmentPath = localPath.getFullPath() + "Fragment.glsl";
        result.vertexSource = FileHelper.readFile(result.vertexPath);
        result.fragmentSource = FileHelper.readFile(result.fragmentPath);

        result.onLoad();

        return result;
    }
}