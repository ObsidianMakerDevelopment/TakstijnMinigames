package com.moyskleytech.mc.BuildBattle.game;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

public class BaseArenas extends Service implements Listener {
    private List<BaseArena> arenas;
    private final Set<BaseRunningArena> runningArenas = new HashSet<>();
    private final Map<Player, BaseRunningArena> arenaForPlayer = new HashMap<>();
    private File arenasFolder;

    public List<BaseArena> getArenas() {
        Logger.trace("Arenas::getArenas()");
        return new ArrayList<>(arenas);
    }

    public List<BaseRunningArena> getRunningArenas() {
        // Logger.trace("Arenas::getRunningArenas()");
        return new ArrayList<>(runningArenas);
    }

    public void addRunning(BaseRunningArena arena) {
        Logger.trace("Arenas::addRunning({})", arena);
        runningArenas.add(arena);
    }

    public void removeRunning(BaseRunningArena arena) {
        Logger.trace("Arenas::removeRunning({})", arena);
        runningArenas.remove(arena);
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        
        BuildBattle.getInstance().registerListener(this);
        super.onLoad();
    }

    @Override
    public void onUnload() {
        Logger.trace("Arenas::onUnload()");

        getRunningArenas().forEach(arena -> arena.stop());
        arenaForPlayer.clear();
        super.onUnload();
    }

    public void put(Player p, BaseRunningArena runningArena) {
        Logger.trace("Arenas::put({},{})", p, runningArena);

        if (runningArena == null)
            arenaForPlayer.remove(p);
        else
            arenaForPlayer.put(p, runningArena);
    }

    public BaseRunningArena getArenaForPlayer(Player p) {
        // Logger.trace("Arenas::getArenaForPlayer({})",p);

        return arenaForPlayer.get(p);
    }

    public boolean isArena(World w) {
        // Logger.trace("Arenas::isArena({})",w);
        return runningArenas.stream().anyMatch(arena -> arena.world.equals(w));
    }

   

    public BaseArena byName(String map) {
        Logger.trace("Arenas::byName({})", map);

        return arenas.stream().filter(ar -> ar.getName().equalsIgnoreCase(map)).findAny().orElse(null);
    }

    public BaseArena byId(UUID map) {
        Logger.trace("Arenas::byId({})", map);

        return arenas.stream().filter(ar -> ar.getId().equals(map)).findAny().orElse(null);
    }

    public @NonNull List<String> names() {
        Logger.trace("Arenas::names()");

        return arenas.stream().map(arena -> arena.getName()).toList();
    }
}
