package com.moyskleytech.mc.BuildBattle.services;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.service.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        int i = 0;

        for (String w : ObsidianConfig.getInstance().worlds()) {
            try {
                World ww = Bukkit.getWorld(w);
                if (ww == null)
                    throw new Exception("World missing");
                free_worlds.add(ww);
            } catch (Exception ignored) {
                BuildBattle.getInstance().getLogger().warning("World " + w + " is not importable, will be ignored");
                ignored.printStackTrace();
            }
        }
        if (free_worlds.size() == 0)
            BuildBattle.getInstance().getLogger().severe("No worlds loaded, minigames will be unavailable");
    }

    @Override
    public void onUnload() {
        used_worlds.forEach(this::freeWorld);
        free_worlds.clear();

        super.onUnload();
    }

    public Optional<World> getWorld(Environment type) {
        synchronized (lock) {
            Optional<World> maybeFreeWorld = free_worlds.stream().filter(w -> w.getEnvironment() == type).findFirst();
            if (maybeFreeWorld.isPresent()) {
                World w = maybeFreeWorld.get();
                free_worlds.remove(w);
                used_worlds.add(w);
                return maybeFreeWorld;
            }
            return Optional.empty();
        }
    }

    public void freeWorld(World w) {
        synchronized (lock) {
            used_worlds.remove(w);
            free_worlds.add(w);
        }
    }

}
