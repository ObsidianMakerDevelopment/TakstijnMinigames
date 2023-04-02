package com.moyskleytech.mc.BuildBattle.services;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.scheduler.BukkitRunnable;

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

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 2; i++) {
                        freeWorld(getWorld(Environment.NORMAL));
                        freeWorld(getWorld(Environment.NETHER));
                        freeWorld(getWorld(Environment.THE_END));
                    }
                }
            }.runTaskLater(BuildBattle.getInstance(), 20);
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
                World w = BuildBattle.getInstance().createEmptyWorld(type, UUID.randomUUID().toString());
                used_worlds.add(w);
                return w;
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
