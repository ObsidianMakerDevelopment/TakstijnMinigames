package com.moyskleytech.mc.obsidiancore;

import com.moyskleytech.mc.obsidiancore.commands.CommandManager;
import com.moyskleytech.mc.obsidiancore.config.ObsidianConfig;
import com.moyskleytech.mc.obsidiancore.config.BankingConfig;
import com.moyskleytech.mc.obsidiancore.config.LanguageConfig;
import com.moyskleytech.mc.obsidiancore.listeners.AFKListener;
import com.moyskleytech.mc.obsidiancore.listeners.BedwarsListener;
import com.moyskleytech.mc.obsidiancore.listeners.ConsumeListener;
import com.moyskleytech.mc.obsidiancore.listeners.DeathListener;
import com.moyskleytech.mc.obsidiancore.listeners.FishingListener;
import com.moyskleytech.mc.obsidiancore.listeners.InventoryListener;
import com.moyskleytech.mc.obsidiancore.listeners.JoinListener;
import com.moyskleytech.mc.obsidiancore.listeners.LuckPermsListener;
import com.moyskleytech.mc.obsidiancore.listeners.SkyblockListener;
import com.moyskleytech.mc.obsidiancore.listeners.StarterKitListener;
import com.moyskleytech.mc.obsidiancore.listeners.TabListener;
import com.moyskleytech.mc.obsidiancore.listeners.VoidTeleportListener;
import com.moyskleytech.mc.obsidiancore.placeholderapi.BankingExpansion;
import com.moyskleytech.mc.obsidiancore.service.Service;
import com.moyskleytech.mc.obsidiancore.service.Service.ServiceLoadException;
import com.moyskleytech.mc.obsidiancore.storage.BankingStorage;
import com.moyskleytech.mc.obsidiancore.storage.Storage;
import com.moyskleytech.mc.obsidiancore.utils.Logger;
import com.moyskleytech.mc.obsidiancore.utils.Logger.Level;
import com.samleighton.xquiset.sethomes.SetHomes;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.moyskleytech.mc.mangrove.Mangrove;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ObsidianCore extends JavaPlugin {

    private static ObsidianCore instance;
    Storage storage;
    BankingStorage bankingStorage;
    @Getter
    BankingExpansion exp;

    public static ObsidianCore getInstance() {
        return instance;
    }

    private JavaPlugin cachedPluginInstance;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private final List<Service> registeredServices = new ArrayList<>();

    public BankingExpansion papi() {
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
        registeredServices.add(new ObsidianConfig(this));
        registeredServices.add(new BankingConfig(this));
        registeredServices.add(new LanguageConfig(this));

        registeredServices.add(new CommandManager());

        registeredServices.add(new AFKListener());
        registeredServices.add(new SkyblockListener());
        registeredServices.add(new BedwarsListener());
        registeredServices.add(new ConsumeListener());
        registeredServices.add(new TabListener());
        registeredServices.add(new JoinListener());
        registeredServices.add(new LuckPermsListener());
        registeredServices.add(new FishingListener());
        registeredServices.add(new DeathListener());
        registeredServices.add(new StarterKitListener());
        registeredServices.add(new InventoryListener());
        registeredServices.add(new SetHomes());
        registeredServices.add(new VoidTeleportListener());
        registeredServices.add(new Mangrove());

        for (var service : registeredServices) {
            try {
                service.onPreload();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
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
            exp = new BankingExpansion();
            exp.register();
        }
        storage = new Storage();
        bankingStorage = new BankingStorage();

        Logger.info("Plugin has finished loading!");
        Logger.info("ObsidianCore Initialized on JAVA {}", System.getProperty("java.version"));
        Logger.trace("API has been registered!");

        Logger.setMode(Level.ERROR);

        // if
        // (ObsidianCore.getPluginInstance().getServer().getPluginManager().getPlugin("Citizens")
        // != null
        // &&
        // ObsidianCore.getPluginInstance().getServer().getPluginManager().getPlugin("Citizens").isEnabled())
        // {
        // net.citizensnpcs.api.CitizensAPI.getTraitFactory()
        // .registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(HologramTrait.class));
        // }
    }

    public void registerListener(@NotNull Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, getPluginInstance());
        Logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    public void unregisterListener(@NotNull Listener listener) {
        Logger.error("Attempting to unregister: {}", listener.getClass().getSimpleName());

        try {
            HandlerList.unregisterAll(listener);
            Class<? extends Listener> cl = listener.getClass();
            List<Method> b = new ArrayList<>(Arrays.asList(cl.getDeclaredMethods()));
            b.addAll(Arrays.asList(cl.getMethods()));
            for (Method m : b) {
                for (Class<?> paramType : m.getParameterTypes()) {
                    try {
                        Logger.warn("{} has getHandlerList?", paramType);
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
            Logger.error("Unregistered listener: {}", listener.getClass().getSimpleName());
        }
        Logger.error("Unregistered listener: {}", listener.getClass().getSimpleName());
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

        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT") || getVersion().contains("dev");
    }

    public String getVersion() {
        return this.getDescription().getVersion();
    }

    public JavaPlugin getJavaPlugin() {
        return instance;
    }

    public Storage getStorage() {
        return storage;
    }

    public BankingStorage getBankingStorage() {
        return bankingStorage;
    }
}
