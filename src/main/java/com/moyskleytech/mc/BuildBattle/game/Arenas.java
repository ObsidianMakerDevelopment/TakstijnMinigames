package com.moyskleytech.mc.BuildBattle.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;

public class Arenas extends Service implements Listener {
    private List<Arena> arenas;
    private List<RunningArena> runningArenas = new ArrayList<>();
    private Map<Player, RunningArena> arenaForPlayer = new HashMap<>();

    public List<Arena> getArenas() {
        return new ArrayList<>(arenas);
    }

    public List<RunningArena> getRunningArenas() {
        return new ArrayList<>(runningArenas);
    }

    public void addRunning(RunningArena arena) {
        runningArenas.add(arena);
    }

    public void removeRunning(RunningArena arena) {
        runningArenas.remove(arena);
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        BuildBattle plugin = BuildBattle.getInstance();
        File folder = plugin.getDataFolder();
        File arenasFolder = new File(folder, "arenas");
        arenasFolder.mkdir();

        Data data = Service.get(Data.class);
        arenas = new ArrayList<>();
        for (File arena : arenasFolder.listFiles()) {
            arenas.add(data.load(Arena.class, arena));
        }

        BuildBattle.getInstance().registerListener(this);
        super.onLoad();
    }

    @Override
    public void onUnload() {
        getRunningArenas().forEach(arena -> arena.stop());
        arenaForPlayer.clear();
        super.onUnload();
    }

    public void put(Player p, RunningArena runningArena) {
        if (runningArena == null)
            arenaForPlayer.remove(p);
        else
            arenaForPlayer.put(p, runningArena);
    }

    public RunningArena getArenaForPlayer(Player p)
    {
        return arenaForPlayer.get(p);
    }

    public boolean isArena(World w)
    {
        return runningArenas.stream().anyMatch(arena->arena.world.equals(w));
    }
}
