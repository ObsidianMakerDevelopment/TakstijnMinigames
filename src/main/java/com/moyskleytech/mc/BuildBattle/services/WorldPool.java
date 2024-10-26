package com.moyskleytech.mc.BuildBattle.services;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.Bukkit;

public class WorldPool extends Service {

    private Object lock = new Object();
    private List<World> free_worlds = new ArrayList<>();
    private List<World> used_worlds = new ArrayList<>();

    public static WorldPool getInstance() {
        return Service.get(WorldPool.class);
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        super.onLoad();
        if (free_worlds.size() == 0)
            BuildBattle.getInstance().deleteDirectory(new File("bb_worlds"));
        Scheduler.getInstance().runTaskTimer(
                new Consumer<Scheduler.Task>() {
                    int i = 0;
                    List<World> worlds = new ArrayList<>();
                    @Override
                    public void accept(Task t) {
                        worlds.add(getWorld(Environment.NORMAL));
                        if (i++ == 3)
                        {
                            for(World w:worlds)
                            {
                                freeWorld(w);
                            }
                            t.cancel();
                        }
                    }

                }, 20, 20);
        /*
         * new Runnable(){ {
         * for (int i = 0; i < 2; i++) {
         * freeWorld(getWorld(Environment.NORMAL));
         * }
         * }
         */
    }

    public World getWorld(Environment type) {
        synchronized (lock) {
            Optional<World> maybeFreeWorld = free_worlds.stream().filter(w -> w.getEnvironment() == type).findFirst();
            if (maybeFreeWorld.isPresent()) {
                World w = maybeFreeWorld.get();
                free_worlds.remove(w);
                used_worlds.add(w);
                return w;
            } else {
                try {
                    World w = BuildBattle.getInstance().createEmptyWorld(type, UUID.randomUUID().toString());
                    if (w == null)
                        w = Bukkit.getWorlds().get(0);
                    used_worlds.add(w);
                    return w;
                } catch (Throwable t) {
                    return Bukkit.getWorlds().get(0);
                }
            }
        }
    }

    public void freeWorld(World w) {
        synchronized (lock) {
            used_worlds.remove(w);
            free_worlds.add(w);
        }
    }

}
