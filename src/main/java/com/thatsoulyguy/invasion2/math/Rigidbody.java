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
    private final Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);

    private boolean grounded = false;

    public static final float GRAVITY = -9.8f;
    public static final float TIME_STEP = 1.0f / 60.0f;
    public static final float GROUNDED_THRESHOLD = 0.02f;

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

        if (!grounded)
            velocity.y += GRAVITY * TIME_STEP;

        Vector3f currentPosition = getGameObject().getTransform().getWorldPosition();

        Vector3f newPosition = new Vector3f(
                currentPosition.x + velocity.x * TIME_STEP,
                currentPosition.y + velocity.y * TIME_STEP,
                currentPosition.z + velocity.z * TIME_STEP
        );

        getGameObject().getTransform().setLocalPosition(newPosition);

        List<Collider> colliders = ColliderManager.getAll();

        colliders = colliders.stream()
                .filter(collider -> self != collider)
                .filter(collider -> collider.getPosition().distance(getGameObject().getTransform().getWorldPosition()) < 25)
                .toList();

        grounded = false;

        for (Collider collider : colliders)
        {
            if (self.intersects(collider))
            {
                Vector3f resolution = self.resolve(collider);

                if (resolution.y > 0)
                {
                    grounded = true;
                    velocity.y = 0.0f;
                }
                else if (resolution.y < 0)
                    velocity.y = 0.0f;
            }
        }

        if (grounded)
            velocity.y = 0.0f;
    }

    public void addForce(Vector3f force)
    {
        velocity.x += force.x;
        velocity.y += force.y;
        velocity.z += force.z;
    }

    /**
     * Checks if the Rigidbody is grounded based on its vertical velocity and collision state.
     *
     * @return true if the Rigidbody is grounded, false otherwise.
     */
    public boolean isGrounded()
    {
        return Math.abs(velocity.y) < GROUNDED_THRESHOLD;
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}