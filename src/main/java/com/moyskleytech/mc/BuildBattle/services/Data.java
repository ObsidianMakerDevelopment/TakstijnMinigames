package com.moyskleytech.mc.BuildBattle.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.obsidian.material.ObsidianMaterialKeyDeserializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import lombok.Getter;

public class Data extends Service {
    @Getter
    ObjectMapper objectMapper;

    public static Data getInstance()
    {
        return Service.get(Data.class);
    }
    /**
     * The name of a provided class.
     * Used as the file name for persisting.
     *
     * @param clazz The class whose name is to be returned
     * @return The name of the configuration file
     */
    private static String getName(Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    // ------------------------------------------------------------ //
    // GET NAME - What should we call this type of object?
    // ------------------------------------------------------------ //

    /**
     * The name of a provided object.
     * Used as the file name for persisting.     *
     *
     * @param instance The object whose name is to be returned
     * @return The name of the configuration file
     */
    public static String getName(Object instance) {
        return getName(instance.getClass());
    }

    /**
     * The name of a provided type.
     * Used as the file name for persisting.
     *
     * @param type The type whose name is to be returned
     * @return The name of the configuration file
     * @see Type
     */
    public static String getName(Type type) {
        return getName(type.getClass());
    }

    // ------------------------------------------------------------ //
    // GET FILE - In which file would we like to store this object?
    // ------------------------------------------------------------ //

    /**
     * The file corresponding to the name.
     * Used for serialization.
     *
     * @param name The name of the configuration without file extension
     * @return The file where the config with the name should be stored
     */
    public File getFile(String name) {
        return new File(BuildBattle.getInstance().getDataFolder(), name + ".yml");
    }

    /**
     * The file corresponding to the class.
     * Used for serialization.
     *
     * @param clazz The class which should be stored
     * @return The file where the config with the name should be stored
     */
    public File getFile(Class<?> clazz) {
        return getFile(getName(clazz));
    }

    /**
     * The file corresponding to the object.
     * Used for serialization.
     *
     * @param instance The object which should be stored
     * @return The file where the config with the name should be stored
     */
    public File getFile(Object instance) {
        return getFile(getName(instance));
    }

    // SAVE

    /**
     * Saves an object to a file.
     *
     * @param instance The instance of the object which should be saved
     */
    public void save(Object instance) {
        save(instance, getFile(instance));
    }

    /**
     * Saves an object to a file.
     *
     * @param instance The instance of the object which should be saved
     * @param file     The file where the object should be persisted to
     */
    private void save(Object instance, File file) {
        try {
            objectMapper.writeValue(file, instance);
        } catch (IOException e) {
            BuildBattle.getInstance().getLogger().severe("Failed to save " + file.toString() + ": " + e.getMessage());
        }
    }

    /**
     * Converts an object to a string which can be saved.
     * Uses the specified {@link PersistType}.
     * Might be empty if an {@link IOException} occurs.
     *
     * @param instance The instance which should be converted
     * @return The string representation of the object.
     */
    public String toString(Object instance) {
        try {
            return objectMapper.writeValueAsString(instance);
        } catch (IOException e) {
            BuildBattle.getInstance().getLogger().severe("Failed to save " + instance.toString() + ": " + e.getMessage());
        }
        return "";
    }

    // LOAD BY CLASS

    /**
     * Creates a new instance of a class and deserializes it automatically from the corresponding file.
     * Returns the new instance.
     *
     * @param clazz The class which should be loaded
     * @param <T>   The type of class
     * @return The loaded class. Might be null
     */
    public <T> T load(Class<T> clazz) {
        return load(clazz, getFile(clazz));
    }

    /**
     * Creates a new instance of a class and deserializes the provided file content.
     * Returns the new instance.
     *
     * @param clazz The class which should be loaded
     * @param file  The file where the class should be loaded from
     * @param <T>   The type of class
     * @return The loaded class. Might be null
     */
    public <T> T load(Class<T> clazz, File file) {
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, clazz);
            } catch (IOException e) {
                BuildBattle.getInstance().getLogger().severe("Failed to parse " + file + ": " + e.getMessage());
            }
        }
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new instance of a class and deserializes the provided content.
     * Returns the new instance.
     *
     * @param clazz   The class which should be loaded
     * @param content The content which should be loaded into the class
     * @param <T>     The type of class
     * @return The loaded class. Might be null
     */
    public <T> T load(Class<T> clazz, String content) {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onLoad() throws ServiceLoadException {

        super.onLoad();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper(new YAMLFactory()).configure(JsonParser.Feature.IGNORE_UNDEFINED,
                    true);
            try {
                ObsidianMaterialKeyDeserializer.registerKeyDeserializer(objectMapper);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
