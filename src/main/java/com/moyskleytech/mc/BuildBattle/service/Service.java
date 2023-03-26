package com.moyskleytech.mc.BuildBattle.service;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.Listener;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

public class Service {
    private static Map<Class<? extends Service>, Service> serviceCache = new HashMap<>();
    private boolean enabled=false;
    public Service()
    {
        serviceCache.put(this.getClass(), this);
    }
    @SuppressWarnings("unchecked")
    public static <T extends Service> T get(Class<T> class1) {
        return (T)serviceCache.get(class1);
    }

    public boolean isEnabled(){return enabled;}

    public void onLoad() throws ServiceLoadException{
        Logger.warn("Loading service {}", this);
        enabled=true;

    }

    public void onPreload() throws ServiceLoadException{
        Logger.warn("Preloading service {}", this);
    }

    public void onReload() throws ServiceLoadException{
        Logger.warn("Reloading service {}", this);

        onUnload();
        onLoad();
    }

    public void onUnload(){
        Logger.warn("Unloading service {}", this);
        if(this instanceof Listener l)
        {
            BuildBattle.getInstance().unregisterListener(l);

            Logger.warn("Unloading-- listener {}", l);
        }
        enabled=false;
    }


    public class ServiceLoadException extends Exception
    {

        public ServiceLoadException() {
        }

        public ServiceLoadException(String message) {
            super(message);
        }

        public ServiceLoadException(Throwable cause) {
            super(cause);
        }

        public ServiceLoadException(String message, Throwable cause) {
            super(message, cause);
        }

        public ServiceLoadException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }
}
