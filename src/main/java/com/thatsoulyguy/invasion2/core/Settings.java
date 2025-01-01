package com.thatsoulyguy.invasion2.core;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.render.Shader;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.advanced.RenderPassManager;
import com.thatsoulyguy.invasion2.render.advanced.core.renderpasses.GeometryRenderPass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Static
public class Settings
{
    public static final Setting<Float> UI_SCALE = new Setting<>(1.5f);
    public static final Setting<Boolean> USE_ADVANCED_RENDERING_FEATURES = new Setting<>(true);
    public static final Setting<Shader> DEFAULT_RENDERING_SHADER = new Setting<>(null);

    private Settings() { }

    public static void initialize()
    {
        DEFAULT_RENDERING_SHADER.setValue(USE_ADVANCED_RENDERING_FEATURES.value ? ((GeometryRenderPass) Objects.requireNonNull(RenderPassManager.get(GeometryRenderPass.class))).getGeometryShader() : Objects.requireNonNull(ShaderManager.get("legacy.default")));
    }

    public static class Setting<T>
    {
        private T value;

        public Setting(T value)
        {
            this.value = value;
        }

        public @NotNull T getValue()
        {
            return value;
        }

        public void setValue(@NotNull T value)
        {
            this.value = value;
        }
    }
}