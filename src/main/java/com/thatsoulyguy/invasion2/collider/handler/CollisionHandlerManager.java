package com.thatsoulyguy.invasion2.collider.handler;

import com.thatsoulyguy.invasion2.annotation.Static;
import com.thatsoulyguy.invasion2.collider.Collider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for managing collision handlers
 * <p>
 * Annotates: {@code @Static}
 */
@Static
public class CollisionHandlerManager
{
    private static final @NotNull Map<ColliderPair, CollisionHandler> handlers = new HashMap<>();

    private CollisionHandlerManager() { }

    /**
     * Registers a collision handler for a specific pair of collider types.
     * @param classA The first collider class.
     * @param classB The second collider class.
     * @param handler The collision handler.
     */
    public static void register(@NotNull Class<? extends Collider> classA, @NotNull Class<? extends Collider> classB, @NotNull CollisionHandler handler)
    {
        handlers.put(new ColliderPair(classA, classB), handler);
    }

    /**
     * Checks if two colliders intersect by delegating to the appropriate handler.
     * @param a The first collider.
     * @param b The second collider.
     * @return True if they intersect, false otherwise.
     */
    public static boolean intersects(@NotNull Collider a, @NotNull Collider b)
    {
        CollisionHandler handler = get(a.getClass(), b.getClass());

        if (handler != null)
        {
            CollisionResult result = handler.handle(a, b, true);

            return result.intersects();
        }

        return false;
    }

    /**
     * Resolves the collision between two colliders by delegating to the appropriate handler.
     * @param a The first collider.
     * @param b The second collider.
     * @param selfIsMovable Indicates if the first collider is movable.
     * @return A Vector3f representing the resolution vector.
     */
    public static Vector3f resolve(@NotNull Collider a, @NotNull Collider b, boolean selfIsMovable)
    {
        CollisionHandler handler = get(a.getClass(), b.getClass());

        if (handler != null)
        {
            CollisionResult result = handler.handle(a, b, selfIsMovable);
            return result.resolution();
        }

        return new Vector3f();
    }

    private static @Nullable CollisionHandler get(@NotNull Class<? extends Collider> classA, @NotNull Class<? extends Collider> classB)
    {
        CollisionHandler handler = handlers.get(new ColliderPair(classA, classB));

        if (handler != null)
            return handler;

        handler = handlers.get(new ColliderPair(classB, classA));

        if (handler != null)
        {
            CollisionHandler finalHandler = handler;

            return (a, b, selfIsMovable) ->
            {
                CollisionResult result = finalHandler.handle(b, a, !selfIsMovable);

                return new CollisionResult(result.intersects(), result.resolution().negate());
            };
        }

        return null;
    }

    private record ColliderPair(@NotNull Class<? extends Collider> first, @NotNull Class<? extends Collider> second)
    {
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof ColliderPair(Class<? extends Collider> first1, Class<? extends Collider> second1)))
                return false;

            return first.equals(first1) && second.equals(second1);
        }
    }
}