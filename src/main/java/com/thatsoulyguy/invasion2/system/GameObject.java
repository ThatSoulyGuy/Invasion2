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
            this.parent.children.remove(this);

        this.parent = parent;

        if (parent != null && !parent.children.containsValue(this))
            parent.children.putIfAbsent(name, this);

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

        children.putIfAbsent(child.name, child);
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

    public void saveToStream(@NotNull DataOutputStream dos) throws IOException
    {
        dos.writeUTF(name);

        dos.writeInt(componentMap.size());

        for (Component component : componentMap.values())
        {
            String componentClassName = component.getClass().getName();
            dos.writeUTF(componentClassName);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (ObjectOutputStream oos = new ObjectOutputStream(baos))
            {
                oos.writeObject(component);
                oos.flush();

                byte[] componentData = baos.toByteArray();

                dos.writeInt(componentData.length);
                dos.write(componentData);
            }
        }

        dos.writeInt(children.size());

        for (GameObject child : children.values())
            dos.writeUTF(child.getName());
    }

    public static @NotNull GameObject loadFromStream(@NotNull DataInputStream dis, @NotNull File parentDirectory) throws IOException, ClassNotFoundException
    {
        String gameObjectName = dis.readUTF();
        GameObject gameObject = GameObject.create(gameObjectName);

        int componentCount = dis.readInt();
        for (int i = 0; i < componentCount; i++)
        {
            String componentClassName = dis.readUTF();

            int componentDataLength = dis.readInt();
            byte[] componentData = new byte[componentDataLength];

            dis.readFully(componentData);

            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(componentData)))
            {
                Object obj = ois.readObject();
                if (obj instanceof Component component)
                {
                    if (component instanceof ManagerLinkedClass linkedClass)
                    {
                        try
                        {
                            Method getMethod = linkedClass.getManagingClass().getDeclaredMethod("get", String.class);

                            if (!getMethod.canAccess(null))
                                getMethod.setAccessible(true);

                            boolean isStatic = Modifier.isStatic(getMethod.getModifiers());

                            if (!isStatic)
                                throw new NoSuchMethodException("Method 'get' in manager class was non-static! This shouldn't happen!");

                            Object result = getMethod.invoke(null, linkedClass.getManagedItem());

                            if (result instanceof Component)
                            {
                                component = (Component) result;
                                System.out.println("Successfully linked component on Game Object: '" + gameObjectName + "'.");
                            }
                            else
                                System.err.println("Method 'get' did not return a Component instance.");
                        }
                        catch (NoSuchMethodException e)
                        {
                            System.err.println("Method 'get(String)' not found in linked class '" + linkedClass.getManagingClass().getSimpleName() + "'.");
                        }
                        catch (IllegalAccessException e)
                        {
                            System.err.println("Cannot access method 'get(String)' in linked class '" + linkedClass.getManagingClass().getSimpleName() + "'.");
                        }
                        catch (InvocationTargetException e)
                        {
                            System.err.println("An exception occurred while invoking method 'get(String)' in linked class '" + linkedClass.getManagingClass().getSimpleName() + "'.");
                        }
                    }

                    gameObject.addComponent(component);
                    System.out.println("Added component: " + component.getClass().getName());
                }
                else
                    System.err.println("Invalid component type: " + componentClassName);
            }
        }

        int childrenCount = dis.readInt();

        for (int i = 0; i < childrenCount; i++)
        {
            String childName = dis.readUTF();

            File childDir = new File(parentDirectory, gameObjectName);
            File childFile = new File(childDir, childName + ".bin");

            if (childFile.exists())
            {
                GameObject child = GameObject.loadFromFile(childFile);

                if (child != null)
                    gameObject.addChild(child);
                else
                    System.err.println("Failed to load child GameObject: " + childName);
            }
            else
                System.err.println("Child GameObject file does not exist: " + childFile.getAbsolutePath());
        }

        gameObject.componentMap.values().forEach(Component::onLoad);

        return gameObject;
    }

    public void saveToFile(@NotNull File parentDirectory) throws IOException
    {
        parentDirectory = new File(parentDirectory.getAbsolutePath().replace(".bin", ""));
        File gameObjectDirectory = new File(parentDirectory, name);

        if (!parentDirectory.exists() && !(parentDirectory.toPath().getParent().endsWith(name) && parentDirectory.toPath().endsWith(name)))
        {
            if (!parentDirectory.mkdirs())
                throw new IOException("Failed to create directory for GameObject: " + name);
        }

        File file = new File(parentDirectory.toPath().getParent().toFile(), name + ".bin");

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
        {
            saveToStream(dos);
        }

        for (GameObject child : children.values())
            child.saveToFile(gameObjectDirectory);
    }

    public static @Nullable GameObject loadFromFile(@NotNull File file) throws IOException, ClassNotFoundException
    {
        if (!file.exists())
        {
            System.err.println("GameObject file does not exist: " + file.getAbsolutePath());
            return null;
        }

        File parentDirectory = file.getParentFile();

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file))))
        {
            return loadFromStream(dis, parentDirectory);
        }
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