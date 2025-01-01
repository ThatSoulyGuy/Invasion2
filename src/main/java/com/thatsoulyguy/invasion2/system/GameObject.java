package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.math.Transform;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameObject implements Serializable
{
    private @EffectivelyNotNull String name;
    private @NotNull Layer layer = Layer.UNKNOWN;
    private final @NotNull ConcurrentMap<Class<? extends Component>, Component> componentMap = new ConcurrentHashMap<>();

    private @Nullable transient GameObject parent;
    private final @NotNull Map<String, GameObject> children = new LinkedHashMap<>();

    private final @NotNull AtomicBoolean isUpdating = new AtomicBoolean(false);

    private final @NotNull ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean isTransient = false;
    private boolean isActive = true;

    private GameObject() { }

    public <T extends Component> T addComponent(@NotNull T component)
    {
        return addComponent(component, true);
    }

    public <T extends Component> T addComponent(@NotNull T component, boolean initialize)
    {
        component.setGameObject(this);

        if (initialize)
            component.initialize();

        componentMap.putIfAbsent(component.getClass(), component);

        return component;
    }

    public <T extends Component> void setComponent(@NotNull T component)
    {
        component.setGameObject(this);
        component.initialize();

        componentMap.put(component.getClass(), component);
    }

    public @Nullable <T extends Component> T getComponent(@NotNull Class<T> clazz)
    {
        return clazz.cast(componentMap.get(clazz));
    }

    public @NotNull List<Component> getComponents()
    {
        return List.copyOf(componentMap.values());
    }

    public @NotNull <T extends Component> T getComponentNotNull(@NotNull Class<T> clazz)
    {
        return clazz.cast(componentMap.get(clazz));
    }

    public <T extends Component> boolean hasComponent(@NotNull Class<T> clazz)
    {
        return componentMap.containsKey(clazz);
    }

    public <T extends Component> void removeComponent(@NotNull Class<T> clazz)
    {
        Component removed = componentMap.remove(clazz);
        if (removed != null)
            removed.uninitialize();
    }

    public @NotNull Transform getTransform()
    {
        return Objects.requireNonNull(getComponent(Transform.class));
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @Nullable GameObject getParent()
    {
        return parent;
    }

    public void setParent(@Nullable GameObject parent)
    {
        if (this.parent != null)
            this.parent.children.remove(name);

        this.parent = parent;

        if (parent != null && !parent.children.containsValue(this))
            parent.children.put(name, this);

        if (parent != null)
            getTransform().setParent(parent.getTransform());
        else
            getTransform().setParent(null);
    }

    public @NotNull GameObject addChild(@NotNull GameObject child)
    {
        if (isAncestor(child))
            throw new IllegalArgumentException("Cannot add ancestor as child to prevent circular reference.");

        if (GameObjectManager.has(child.getName()))
            GameObjectManager.unregister(child.getName());

        getTransform().addChild(child.getTransform());

        children.put(child.name, child);
        child.setParent(this);

        return child;
    }

    public void removeChild(@NotNull GameObject child)
    {
        getTransform().removeChild(child.getTransform());

        children.remove(child.name);
        child.setParent(null);

        GameObjectManager.register(child);
    }

    public @NotNull GameObject getChild(@NotNull String name)
    {
        return children.get(name);
    }

    public @NotNull Collection<GameObject> getChildren()
    {
        return Collections.unmodifiableCollection(children.values());
    }

    public boolean isTransient()
    {
        return isTransient;
    }

    public void setTransient(boolean isTransient)
    {
        this.isTransient = isTransient;
    }

    public @NotNull Layer getLayer()
    {
        return layer;
    }

    public void updateMainThread()
    {
        if (!isUpdating.compareAndSet(false, true))
            return;

        try
        {
            if (!isActive)
                return;

            lock.writeLock().lock();

            try
            {
                componentMap.values().forEach(Component::updateMainThread);
                children.values().forEach(GameObject::updateMainThread);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        catch (Exception exception)
        {
            System.err.println("Exception in game object: '" + name + "'! " + exception.getMessage());
        }
        finally
        {
            isUpdating.set(false);
        }
    }

    public void update()
    {
        if (!isUpdating.compareAndSet(false, true))
            return;

        try
        {
            if (!isActive)
                return;

            lock.writeLock().lock();

            try
            {
                componentMap.values().forEach(Component::update);
                children.values().forEach(GameObject::update);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        catch (Exception exception)
        {
            System.err.println("Exception in game object: '" + name + "'! " + exception.getMessage());
        }
        finally
        {
            isUpdating.set(false);
        }
    }

    public void renderDefault(@Nullable Camera camera)
    {
        if (!isActive)
            return;

        lock.readLock().lock();

        try
        {
            if (layer == Layer.DEFAULT)
                componentMap.values().forEach((component) -> component.renderDefault(camera));

            children.values().forEach(gameObject -> gameObject.renderDefault(camera));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public void renderUI()
    {
        if (!isActive)
            return;

        lock.readLock().lock();

        try
        {
            if (layer == Layer.UI)
                componentMap.values().forEach(Component::renderUI);

            children.values().forEach(GameObject::renderUI);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public void save(@NotNull File file)
    {
        save(file, false);
    }

    public void save(@NotNull File file, boolean ignoreTransient)
    {
        if (isTransient && !ignoreTransient)
            return;

        try (FileOutputStream fileOutputStream = new FileOutputStream(file); ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream))
        {
            File childrenDirectory = new File(file.getAbsolutePath().replace(".bin", "/"));

            objectOutputStream.writeUTF(name);
            objectOutputStream.writeInt(componentMap.size());
            objectOutputStream.writeInt(children.size());
            objectOutputStream.writeObject(layer);

            ExecutorService componentExecutor = Executors.newVirtualThreadPerTaskExecutor();

            List<Future<?>> componentFutures = new ArrayList<>();
            componentMap.forEach(((aClass, component) ->
                    componentFutures.add(componentExecutor.submit(() ->
                    {
                        try
                        {
                            System.out.println("Saving component '" + component.getClass().getSimpleName() + "' on game object '" + name + "'...");

                            component.onUnload();

                            if (component.getTransient())
                            {
                                Constructor<?> constructor = aClass.getDeclaredConstructor();

                                constructor.setAccessible(true);

                                Component newComponent = (Component) constructor.newInstance();

                                newComponent.setTransient(true);

                                synchronized (objectOutputStream)
                                {
                                    objectOutputStream.writeObject(newComponent);
                                }
                            }
                            else
                            {
                                synchronized (objectOutputStream)
                                {
                                    objectOutputStream.writeObject(component);
                                }
                            }
                        }
                        catch (NoSuchMethodException | IOException | InstantiationException | IllegalAccessException | InvocationTargetException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }))));

            componentExecutor.shutdown();

            for (Future<?> future : componentFutures)
                future.get();

            if (!children.isEmpty())
            {
                if (!childrenDirectory.exists())
                    childrenDirectory.mkdirs();

                for (GameObject child : children.values())
                    objectOutputStream.writeUTF(child.name);
            }

            System.out.println("Saved game object '" + name + "' at position/rotation/scale: " + getTransform());

            ExecutorService childrenExecutor = Executors.newVirtualThreadPerTaskExecutor();
            List<Future<?>> childFutures = new ArrayList<>();

            for (GameObject child : children.values())
                childFutures.add(childrenExecutor.submit(() -> child.save(new File(childrenDirectory, child.name + ".bin"))));

            childrenExecutor.shutdown();

            for (Future<?> future : childFutures)
                future.get();

        }
        catch (Exception exception)
        {
            System.err.println("Failed to serialize game object '" + name + "'! " + exception.getMessage());
        }
    }

    public static @NotNull GameObject load(@NotNull File file)
    {
        GameObject result = new GameObject();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        ReentrantLock fileLock = new ReentrantLock();

        try (FileInputStream fileInputStream = new FileInputStream(file); ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream))
        {
            fileLock.lock();

            try
            {
                result.name = objectInputStream.readUTF();
                System.out.println("Deserializing game object '" + result.name + "'...");

                int componentCount = objectInputStream.readInt();
                int childrenCount = objectInputStream.readInt();

                result.layer = (Layer) objectInputStream.readObject();

                List<Component> componentsToInitialize = new ArrayList<>();

                for (int i = 0; i < componentCount; i++)
                {
                    Object object = objectInputStream.readObject();

                    switch (object)
                    {
                        case Transform component ->
                        {
                            System.out.println("Deserialized component '" + component.getClass().getSimpleName() + "'.");
                            result.addComponent(component);
                        }

                        case ManagerLinkedClass linkedClass ->
                        {
                            Component component = (Component) linkedClass.getManagingClass().getMethod("get", String.class).invoke(null, linkedClass.getManagedItem());

                            System.out.println("Deserialized component '" + component.getClass().getSimpleName() + "'.");

                            result.addComponent(component);
                        }

                        case Component component ->
                        {
                            System.out.println("Deserialized component '" + component.getClass().getSimpleName() + "'.");

                            componentsToInitialize.add(result.addComponent(component, false));
                        }

                        case null, default -> System.err.println("Invalid component found during deserialization.");
                    }
                }

                componentsToInitialize.forEach(Component::initialize);

                System.out.println("Components loaded for game object: " + result.name);

                List<GameObject> children = new ArrayList<>();

                for (int i = 0; i < childrenCount; i++)
                {
                    String childName = objectInputStream.readUTF();
                    File childFile = new File(file.getAbsolutePath().replace(".bin", "/"), childName + ".bin");

                    Future<GameObject> futureChild = executor.submit(() -> load(childFile));

                    children.add(futureChild.get());
                }

                for (GameObject child : children)
                    result.addChild(child);

                System.out.println("Children loaded for game object: " + result.name);

                result.componentMap.values().forEach(Component::onLoad);

                System.out.println("Deserialized game object '" + result.name + "' at position/rotation/scale: " + result.getTransform());
            }
            finally
            {
                fileLock.unlock();
            }

        }
        catch (Exception exception)
        {
            System.err.println("Failed to deserialize game object at '" + file.getAbsolutePath() + "'! " + exception.getMessage());
        }
        finally
        {
            executor.shutdown();
        }

        return result;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive()
    {
        return isActive;
    }

    public final @NotNull ReentrantReadWriteLock getLock()
    {
        return lock;
    }

    private boolean isAncestor(@NotNull GameObject potentialAncestor)
    {
        GameObject current = this.parent;

        while (current != null)
        {
            if (current == potentialAncestor)
                return true;

            current = current.parent;
        }

        return false;
    }

    public void uninitialize()
    {
        if (isUpdating.get())
        {
            GameObjectManager.unregister(this.name, true);
            return;
        }

        try
        {
            lock.writeLock().lock();

            isActive = false;

            componentMap.values().stream()
                    .filter(component -> !(component instanceof Transform))
                    .forEach(Component::uninitialize);

            componentMap.clear();

            synchronized (children)
            {
                children.values().forEach(GameObject::uninitialize);
                children.clear();
            }
        }
        catch (Exception exception)
        {
            System.err.println("Failed to uninitialize GameObject '" + name + "': " + exception.getMessage());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public static @NotNull GameObject create(@NotNull String name, @NotNull Layer layer)
    {
        GameObject result = new GameObject();

        result.name = name;
        result.layer = layer;
        result.addComponent(Transform.create(new Vector3f(0.0f), new Vector3f(0.0f), new Vector3f(1.0f)));

        GameObjectManager.register(result);

        return result;
    }
}
