package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CustomConstructor("create")
public class Rigidbody extends Component
{
    private Rigidbody() { }

    @Override
    public void update()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        List<Collider> colliders = ColliderManager.getAll();

        colliders = colliders.stream()
                .filter(collider -> self != collider)
                .filter(collider -> collider.getPosition().distance(getGameObject().getTransform().getWorldPosition()) < 25)
                .toList();

        for (Collider collider : colliders)
        {
            if (self.intersects(collider))
                self.resolve(collider);
        }
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}