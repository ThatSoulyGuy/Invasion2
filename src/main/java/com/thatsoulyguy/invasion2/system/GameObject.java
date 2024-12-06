package com.thatsoulyguy.invasion2.system;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import com.thatsoulyguy.invasion2.annotation.EffectivelyNotNull;
import com.thatsoulyguy.invasion2.math.Transform;
import com.thatsoulyguy.invasion2.render.TextureManager;
import com.thatsoulyguy.invasion2.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CustomConstructor("create")
public class GameObject implements Serializable
{
    private @EffectivelyNotNull String name;
    private final @NotNull ConcurrentMap<Class<? extends Component>, Component> componentMap = new ConcurrentHashMap<>();

    private GameObject() { }

    public static @NotNull GameObject create(@NotNull String name)
    {
        GameObject result = new GameObject();

        result.name = name;
        result.addComponent(Transform.create(new Vector3f(0.0f), new Vector3f(0.0f), new Vector3f(1.0f)));

        GameObjectManager.register(result);

        return result;
    }

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

    public void update()
    {
        componentMap.values().parallelStream().forEach(Component::update);
    }

    public void render()
    {
        componentMap.values().forEach(Component::render);
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
    }

    public static @NotNull GameObject loadFromStream(@NotNull DataInputStream dis) throws IOException, ClassNotFoundException
    {
        int availableStart = dis.available();
        System.out.println("Bytes available at start: " + availableStart);

        try
        {
            String gameObjectName = dis.readUTF();
            System.out.println("GameObject Name: " + gameObjectName);

            GameObject gameObject = GameObject.create(gameObjectName);

            int componentCount = dis.readInt();
            System.out.println("Component Count: " + componentCount);

            for (int i = 0; i < componentCount; i++)
            {
                String componentClassName = dis.readUTF();
                System.out.println("Component Class Name: " + componentClassName);

                int componentDataLength = dis.readInt();
                System.out.println("Component Data Length: " + componentDataLength);

                byte[] componentData = new byte[componentDataLength];

                dis.readFully(componentData);
                System.out.println("Read " + componentData.length + " bytes for component data.");

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

            int availableEnd = dis.available();

            System.out.println("Bytes available at end: " + availableEnd);

            gameObject.componentMap.values().forEach(Component::onLoad);

            return gameObject;
        }
        catch (EOFException e)
        {
            System.err.println("Reached end of stream unexpectedly.");

            throw e;
        }
    }

    public void saveToFile(@NotNull File file) throws IOException
    {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
        {
            saveToStream(dos);
        }
    }

    public static @Nullable GameObject loadFromFile(@NotNull File file) throws IOException, ClassNotFoundException
    {
        if (!file.exists())
        {
            System.err.println("GameObject file does not exist: " + file.getAbsolutePath());
            return null;
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file))))
        {
            return loadFromStream(dis);
        }
    }

    public void uninitialize()
    {
        componentMap.values().forEach(Component::uninitialize);
        componentMap.clear();
    }
}