package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

@CustomConstructor("create")
public class Rigidbody extends Component
{
    private Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

    private final float gravity = -9.8f;
    private final float timeStep = 1.0f / 60.0f;

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

        velocity.y += gravity * timeStep;

        Vector3f currentPosition = getGameObject().getTransform().getWorldPosition();

        Vector3f newPosition = new Vector3f(
                currentPosition.x + velocity.x * timeStep,
                currentPosition.y + velocity.y * timeStep,
                currentPosition.z + velocity.z * timeStep
        );

        getGameObject().getTransform().setLocalPosition(newPosition);

        List<Collider> colliders = ColliderManager.getAll();

        colliders = colliders.stream()
                .filter(collider -> self != collider)
                .filter(collider -> collider.getPosition().distance(getGameObject().getTransform().getWorldPosition()) < 25)
                .toList();

        for (Collider collider : colliders)
        {
            if (self.intersects(collider))
            {
                self.resolve(collider);

                velocity.y = 0.0f;
            }
        }
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}