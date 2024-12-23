package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.invasion2.collider.handler.CollisionResult;
import com.thatsoulyguy.invasion2.core.Time;
import com.thatsoulyguy.invasion2.system.Component;
import com.thatsoulyguy.invasion2.system.GameObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

@CustomConstructor("create")
public class Rigidbody extends Component
{
    public static final float GRAVITY = -9.8f;

    private final Vector3f velocity = new Vector3f(0,0,0);

    private boolean isGrounded = false;

    private Rigidbody() { }

    @Override
    public void update()
    {
        float deltaTime = Time.getDeltaTime();

        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (!isGrounded)
            velocity.y += GRAVITY * deltaTime;

        Transform transform = getGameObject().getTransform();

        Vector3f currentPosition = transform.getWorldPosition();

        Vector3f newPosition = new Vector3f(
                currentPosition.x + velocity.x * deltaTime,
                currentPosition.y + velocity.y * deltaTime,
                currentPosition.z + velocity.z * deltaTime
        );

        transform.setLocalPosition(newPosition);

        List<Collider> colliders = ColliderManager.getAll().stream()
                .filter(c -> c != self)
                .filter(c -> c.getPosition()
                        .distance(transform.getWorldPosition()) < 85)
                .toList();

        boolean collidedFromBelow = false;
        boolean groundCheckHit = false;

        for (Collider collider : colliders)
        {
            if (self.intersects(collider))
            {
                Vector3f resolution = self.resolve(collider, true);

                transform.translate(resolution);

                if (resolution.y > 0)
                {
                    collidedFromBelow = true;
                    velocity.y = 0.0f;
                }
                else if (resolution.y < 0)
                    velocity.y = 0.0f;
            }

            Vector3f boxPosition = self.getPosition();
            Vector3f boxSize = self.getSize();

            Vector3f boxMin = new Vector3f(boxPosition).sub(new Vector3f(boxSize).mul(0.5f));
            Vector3f boxMax = new Vector3f(boxPosition).add(new Vector3f(boxSize).mul(0.5f));

            if (collider instanceof VoxelMeshCollider meshCollider)
            {
                for (Vector3f voxel : meshCollider.getVoxels())
                {
                    Vector3f voxelWorldPosition = new Vector3f(meshCollider.getPosition()).add(voxel);

                    Vector3f voxelMin = new Vector3f(voxelWorldPosition).sub(0.5f, 0.5f, 0.5f);
                    Vector3f voxelMax = new Vector3f(voxelWorldPosition).add(0.5f, 0.5f, 0.5f);

                    if (Collider.isOnTopOf(boxMin, boxMax, voxelMin, voxelMax))
                    {
                        groundCheckHit = true;
                        break;
                    }
                }

                if (groundCheckHit)
                    break;
            }
        }

        isGrounded = (collidedFromBelow || groundCheckHit);

        if (isGrounded)
            velocity.y = 0.0f;
    }

    /**
     * Adds force to the Rigidbody
     *
     * @param force The force to add
     */
    public void addForce(@NotNull Vector3f force)
    {
        velocity.x += force.x;
        velocity.y += force.y;
        velocity.z += force.z;
    }

    /**
     * Checks if the Rigidbody is currently grounded.
     *
     * @return True if grounded, false otherwise.
     */
    public boolean isGrounded()
    {
        return isGrounded;
    }

    /**
     * Sets the velocity of the Rigidbody.
     * This can be used to apply external forces or impulses.
     *
     * @param velocity The new velocity vector.
     */
    public void setVelocity(Vector3f velocity)
    {
        this.velocity.set(velocity);
    }

    /**
     * Gets the current velocity of the Rigidbody.
     *
     * @return The velocity vector.
     */
    public Vector3f getVelocity()
    {
        return new Vector3f(velocity);
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}