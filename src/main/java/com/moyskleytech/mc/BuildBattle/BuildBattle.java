package com.moyskleytech.mc.BuildBattle;

import com.moyskleytech.mc.obsidianbb.ObsidianBB.BuildConfig;
import com.moyskleytech.mc.BuildBattle.commands.CommandManager;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.SpleefArenas;
import com.moyskleytech.mc.BuildBattle.game.TickService;
import com.moyskleytech.mc.BuildBattle.generator.VoidGen;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.listeners.ArenaProtection;
import com.moyskleytech.mc.BuildBattle.listeners.JoinLeaveListener;
import com.moyskleytech.mc.BuildBattle.placeholderapi.BuildBattleExpansion;
import com.moyskleytech.mc.BuildBattle.scoreboard.ScoreboardManager;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.service.Service.ServiceLoadException;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.services.Paster;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.VillagerTrade;
import com.moyskleytech.mc.BuildBattle.utils.Logger.Level;

import lombok.Getter;
import net.kyori.adventure.util.TriState;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

public class BuildBattle extends JavaPlugin {

    private static BuildBattle instance;
    @Getter
    BuildBattleExpansion exp;

    public static BuildBattle getInstance() {
        return instance;
    }

    private JavaPlugin cachedPluginInstance;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final List<Service> registeredServices = new ArrayList<>();
    private VoidGen chunkGenerator;
    private List<World> worlds = new ArrayList<>();

    public BuildBattleExpansion papi() {
        return exp;
    }

    public static JavaPlugin getPluginInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not yet been initialized!");
        }
        if (instance.cachedPluginInstance == null) {
            instance.cachedPluginInstance = (JavaPlugin) instance;
        }
        return instance.cachedPluginInstance;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
        cachedPluginInstance = instance;
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(cachedPluginInstance, "BungeeCord");
        Logger.init(cachedPluginInstance);

        // Load services
        registeredServices.add(new Data());
        registeredServices.add(new ObsidianConfig(this));
        registeredServices.add(new LanguageConfig(this));
        registeredServices.add(new WorldPool());

        registeredServices.add(new CommandManager());
        registeredServices.add(new Paster());
        registeredServices.add(new JoinLeaveListener());
        registeredServices.add(new ArenaProtection());
        registeredServices.add(new Arenas());
        registeredServices.add(new SpleefArenas());
        registeredServices.add(new TickService());
        registeredServices.add(new ScoreboardManager());

        for (var service : registeredServices) {
            try {
                service.onPreload();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
        }
        this.chunkGenerator = new VoidGen();
    }

    public World createEmptyWorld(World.Environment environment, String name) {
        long begin = System.nanoTime();
        WorldCreator worldCreator = new WorldCreator("bb_worlds/" + name)
                .generator(chunkGenerator)
                .keepSpawnLoaded(TriState.FALSE)
                .environment(environment);
        try {
            World w = Bukkit.createWorld(worldCreator);

            w.setDifficulty(Difficulty.NORMAL);
            w.setSpawnFlags(true, true);
            //w.setPVP(false);
            w.setStorm(false);
            w.setThundering(false);
            w.setWeatherDuration(Integer.MAX_VALUE);
            w.setKeepSpawnInMemory(false);
            w.setTicksPerSpawns(SpawnCategory.ANIMAL, 1);
            w.setTicksPerSpawns(SpawnCategory.MONSTER, 1);
            w.setAutoSave(false);
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            w.setGameRule(GameRule.DO_FIRE_TICK, false);
            w.setGameRule(GameRule.MOB_GRIEFING, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            w.setGameRule(GameRule.DROWNING_DAMAGE, false);

            Logger.trace("Created world in " + (System.nanoTime() - begin) + " nanoseconds");
            worlds.add(w);
            return w;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public void reload() {

        for (var service : registeredServices) {
            try {
                Logger.trace("{}", service.getClass().getName());
                service.onReload();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
        }
        getRidOfWorlds();
    }

    private void getRidOfWorlds() {
        worlds.forEach(world -> {
            File toDelete = world.getWorldFolder();
            Bukkit.unloadWorld(world, false);
            deleteDirectory(toDelete);
        });
        worlds.clear();
    }

    public void deleteWorld(World world) {
        File toDelete = world.getWorldFolder();
        world.getPlayers().forEach(player -> player.teleport(ObsidianUtil.getMainLobby()));
        Chunk[] chunks = world.getLoadedChunks();
        for (Chunk chunk : chunks) {
            chunk.unload(false);
        }
        if (Bukkit.unloadWorld(world, false)) {
            Chunk[] chunkss = world.getLoadedChunks();
            for (Chunk chunk : chunkss) {
                chunk.unload(false);
            }
            Scheduler.getInstance().runTaskTimer((task) -> {
                try {
                    deleteDirectory(toDelete);
                    worlds.remove(world);
                    task.cancel();
                } catch (Throwable t) {

                }
            }, 50, 100);
        } else {
            Logger.error("Could not delete world {}", world);
        }
    }

    public boolean deleteDirectory(File path) {
        try {
            if (path.exists()) {
                File files[] = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
            return (path.delete());
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        for (var service : registeredServices) {
            try {
                service.onLoad();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Logger.trace("Registering banking placeholder expansion...");
            exp = new BuildBattleExpansion();
            exp.register();
        }

        Logger.info("Plugin has finished loading!");
        Logger.info("BuildBattle Initialized on JAVA {}", System.getProperty("java.version"));
        Logger.trace("API has been registered!");

        Logger.setMode(Level.WARNING);

        for(VillagerTrade r: ObsidianUtil.getAllVillagersRecipes())
        {
            Logger.info("Recipe {} {}", r.getClass(), r.toString());
        }
    }

    public void registerListener(Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, getPluginInstance());
        Logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    public void unregisterListener(Listener listener) {
        Logger.trace("Attempting to unregister: {}", listener.getClass().getSimpleName());

        try {
            HandlerList.unregisterAll(listener);
            Class<? extends Listener> cl = listener.getClass();
            List<Method> b = new ArrayList<>(Arrays.asList(cl.getDeclaredMethods()));
            b.addAll(Arrays.asList(cl.getMethods()));
            for (Method m : b) {
                for (Class<?> paramType : m.getParameterTypes()) {
                    try {
                        HandlerList hl = (HandlerList) paramType.getMethod("getHandlerList").invoke(null);
                        ;
                        hl.unregister(listener);
                    } catch (NoSuchMethodException | NoClassDefFoundError nsm) {
                        // Do nothing
                    }

                }
            }
            registeredListeners.remove(listener);
        } catch (java.lang.NoClassDefFoundError nclass) {
            // Do nothing, a dependancy isn't present
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Logger.trace("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        for (var service : registeredServices) {
            service.onUnload();
        }
        getRidOfWorlds();

        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT") || getVersion().contains("dev");
    }

    public String getVersion() {
        return BuildConfig.APP_VERSION;
    }

    public JavaPlugin getJavaPlugin() {
        return instance;
    }

}
