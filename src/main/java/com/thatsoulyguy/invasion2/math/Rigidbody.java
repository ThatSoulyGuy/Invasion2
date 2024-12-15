package com.thatsoulyguy.invasion2.math;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.collider.Collider;
import com.thatsoulyguy.invasion2.collider.ColliderManager;
import com.thatsoulyguy.invasion2.collider.colliders.BoxCollider;
import com.thatsoulyguy.invasion2.core.Time;
import com.thatsoulyguy.invasion2.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CustomConstructor("create")
public class Rigidbody extends Component
{
    private static final int MAX_SUBSTEPS = 5;
    private final Vector3f velocity = new Vector3f(0,0,0);
    private final Vector3f accumulatedForce = new Vector3f(0,0,0);

    private boolean grounded = false;
    public static final float GRAVITY = -9.8f;
    public static final float MASS = 1.0f;

    private Rigidbody() { }

    @Override
    public void update()
    {
        Vector3f accumulatedMovement = new Vector3f();

        if (!grounded)
            accumulatedForce.y += GRAVITY * MASS;

        float dt = Time.getDeltaTime();

        Vector3f acceleration = new Vector3f(accumulatedForce).div(MASS);

        velocity.fma(dt, acceleration);

        accumulatedForce.set(0, 0, 0);

        float maxFallSpeed = -20.0f;

        if (velocity.y < maxFallSpeed)
            velocity.y = maxFallSpeed;

        Vector3f displacement = new Vector3f(velocity).mul(dt);

        grounded = false;

        float remainingTime = 1.0f;
        int steps = 0;

        getGameObject().getTransform().translate(new Vector3f(0, -0.0001f, 0)); //TODO: Add a better ground check then moving forced intersection (e.g. raycasting downward)

        Collider self = getGameObject().getComponent(BoxCollider.class);

        while (remainingTime > 0.0f && steps < MAX_SUBSTEPS)
        {
            steps++;

            if (self == null)
                break;

            List<Collider> rawColliders = new ArrayList<>(ColliderManager.getAll());

            List<Collider> colliders = rawColliders.stream()
                    .filter(collider -> self != collider)
                    .filter(collider -> self.getPosition().distance(collider.getPosition()) < 80)
                    .toList();

            Vector3f frameDisplacement = new Vector3f(displacement).mul(remainingTime);

            List<CompletableFuture<Float>> tList = new ArrayList<>(colliders.size());

            for (final Collider other : colliders)
                tList.add(CompletableFuture.supplyAsync(() -> self.sweepTest(other, frameDisplacement)));

            Float earliestT = null;
            Collider hitCollider = null;

            for (int i = 0; i < tList.size(); i++)
            {
                Float t = tList.get(i).join();

                if (t != null && (earliestT == null || t < earliestT))
                {
                    earliestT = t;
                    hitCollider = colliders.get(i);
                }
            }

            if (hitCollider == null)
            {
                accumulatedMovement.add(frameDisplacement);
                remainingTime = 0.0f;
            }
            else
            {
                float effectiveT = (earliestT == 0.0f && velocity.y > 0) ? 0.001f : earliestT;

                Vector3f partialMove = new Vector3f(displacement).mul(remainingTime * effectiveT);

                accumulatedMovement.add(partialMove);

                Vector3f resolution = self.resolve(hitCollider);

                Vector3f normal = new Vector3f(resolution).normalize();

                if (normal.y > 0.7f && velocity.y <= 0)
                {
                    grounded = true;
                    velocity.y = 0.0f;
                }
                else
                {
                    float vDotN = velocity.dot(normal);

                    if (vDotN < 0)
                    {
                        Vector3f velCorrection = new Vector3f(normal).mul(vDotN);
                        velocity.sub(velCorrection);
                    }
                }

                remainingTime = remainingTime * (1.0f - earliestT);

                if (velocity.lengthSquared() < 1e-8f)
                    remainingTime = 0.0f;
            }
        }

        if (grounded && velocity.y < 0)
            velocity.y = 0.0f;

        {
            List<Collider> rawColliders = new ArrayList<>(ColliderManager.getAll());

            getGameObject().getTransform().translate(accumulatedMovement);

            boolean resolvedSomething;

            int maxIterations = 5;

            do
            {
                resolvedSomething = false;

                List<Collider> colliders = rawColliders.stream()
                        .filter(collider -> self != collider)
                        .filter(collider ->
                        {
                            assert self != null;
                            return self.getPosition().distance(collider.getPosition()) < 80;
                        })
                        .toList();

                for (Collider c : colliders)
                {
                    if (self.intersects(c))
                    {
                        Vector3f res = self.resolve(c);
                        if (res.lengthSquared() > 0)
                            resolvedSomething = true;
                    }
                }

                maxIterations--;
            } while (resolvedSomething && maxIterations > 0);
        }

        System.out.println("Is grounded: " + grounded);
    }

    public void addForce(Vector3f force)
    {
        accumulatedForce.add(force);
    }

    public boolean isGrounded()
    {
        return grounded;
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}