package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.math.Transform;
import com.thatsoulyguy.invasion2.render.Camera;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CustomConstructor("create")
public class GameObject implements Serializable
{
    private @EffectivelyNotNull String name;
    private final @NotNull ConcurrentMap<Class<? extends Component>, Component> componentMap = new ConcurrentHashMap<>();

    private @Nullable transient GameObject parent;
    private final @NotNull ConcurrentMap<String, GameObject> children = new ConcurrentHashMap<>();

    private GameObject() { }

    public <T extends Component> void addComponent(@NotNull T component)
    {
        component.setGameObject(this);

        component.initialize();

        componentMap.putIfAbsent(component.getClass(), component);
    }

    public @Nullable <T extends Component> T getComponent(@NotNull Class<T> clazz)
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

    public void addChild(@NotNull GameObject child)
    {
        if (isAncestor(child))
            throw new IllegalArgumentException("Cannot add ancestor as child to prevent circular reference.");

        GameObjectManager.unregister(child.getName());

        children.put(child.name, child);
        child.setParent(this);
    }

    public void removeChild(@NotNull GameObject child)
    {
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

    public void update()
    {
        componentMap.values().parallelStream().forEach(Component::update);

        synchronized (children)
        {
            children.values().forEach(GameObject::update);
        }
    }

    public void render(@Nullable Camera camera)
    {
        componentMap.values().forEach((component) -> component.render(camera));

        synchronized (children)
        {
            children.values().forEach((gameObject) -> gameObject.render(camera));
        }
    }

    public void save(@NotNull File file)
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file))
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            File childrenDirectory = new File(file.getAbsolutePath().replace(".bin", "/"));

            objectOutputStream.writeUTF(name);

            objectOutputStream.writeInt(componentMap.size());
            objectOutputStream.writeInt(children.size());

            componentMap.values().forEach((component ->
            {
                try
                {
                    System.out.println("Saving component '" + component.getClass().getSimpleName() + "' on game object '" + name + "'...");

                    objectOutputStream.writeObject(component);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }));

            if (!children.isEmpty())
            {
                if (!childrenDirectory.exists())
                    childrenDirectory.mkdirs();

                children.values().forEach((child ->
                {
                    try
                    {
                        objectOutputStream.writeUTF(child.name);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }));
            }

            System.out.println("Saved game object '" + name + "' at position/rotation/scale: " + getTransform());

            if (!children.isEmpty())
                children.values().forEach((child -> child.save(new File(childrenDirectory, child.name + ".bin"))));

            objectOutputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to serialize game object '" + name + "'! " + exception.getMessage());
        }
    }

    public static @NotNull GameObject load(@NotNull File file)
    {
        GameObject result = new GameObject();

        try (FileInputStream fileInputStream = new FileInputStream(file))
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            result.name = objectInputStream.readUTF();

            System.out.println("Deserializing game object '" + result.name + "'...");

            int componentCount = objectInputStream.readInt();
            int childrenCount = objectInputStream.readInt();

            List<Component> components = new ArrayList<>();

            for (int c = 0; c < componentCount; c++)
            {
                Object object = objectInputStream.readObject();

                if (object instanceof Component component)
                {
                    if (component instanceof ManagerLinkedClass linkedClass)
                        component = (Component) linkedClass.getManagingClass().getMethod("get", String.class).invoke(null, linkedClass.getManagedItem());

                    System.out.println("Deserialized component '" + component.getClass().getSimpleName() + "'.");

                    components.add(component);
                }
                else
                    System.err.println("Non-component saved in components list!");
            }

            for (Component component : components)
            {
                if (component instanceof Transform)
                    result.addComponent(component);
            }

            for (Component component : components)
            {
                if (component instanceof Transform)
                    continue;

                result.addComponent(component);
            }

            result.componentMap.values().forEach(Component::onLoad);

            System.out.println("Deserialized game object '" + result.name + "' at position/rotation/scale: " + result.getTransform());

            if (childrenCount > 0)
            {
                for (int c = 0; c < childrenCount; c++)
                    result.addChild(GameObject.load(new File(file.getAbsolutePath().replace(".bin", "/"), objectInputStream.readUTF() + ".bin")));
            }

            objectInputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to deserialize game object at '" + file.getAbsolutePath() + "'! " + exception.getMessage());
        }

        return result;
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
        componentMap.values().forEach(Component::uninitialize);
        componentMap.clear();

        synchronized (children)
        {
            children.values().forEach(GameObject::uninitialize);

            children.clear();
        }
    }

    public static @NotNull GameObject create(@NotNull String name)
    {
        GameObject result = new GameObject();

        result.name = name;
        result.addComponent(Transform.create(new Vector3f(0.0f), new Vector3f(0.0f), new Vector3f(1.0f)));

        GameObjectManager.register(result);

        return result;
    }
}