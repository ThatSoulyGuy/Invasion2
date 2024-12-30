package com.thatsoulyguy.invasion2.render.advanced.core;

import com.thatsoulyguy.invasion2.render.Camera;
import org.jetbrains.annotations.Nullable;

public interface RenderPass
{
    void initialize();

    void render(@Nullable Camera camera);

    void uninitialize();
}