package com.thatsoulyguy.invasion2.ui.uielements;

import com.thatsoulyguy.invasion2.render.Mesh;
import com.thatsoulyguy.invasion2.render.ShaderManager;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.system.GameObject;
import com.thatsoulyguy.invasion2.ui.UIElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ImageUIElement extends UIElement
{
    @Override
    public void generate(@NotNull GameObject object)
    {
        object.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        object.addComponent(Objects.requireNonNull(TextureManager.get("error")));

        object.addComponent(Mesh.create(DEFAULT_VERTICES, DEFAULT_INDICES));

        object.getComponentNotNull(Mesh.class).onLoad();
    }
}