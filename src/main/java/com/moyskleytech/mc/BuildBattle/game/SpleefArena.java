package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;

import lombok.Getter;

@Getter
public class SpleefArena {
    public UUID id;
    public Environment type = Environment.NORMAL;
    public int plotSize = 20;
    public int plotHeight = 40;
    public String name = "arena";
    public int contourSize = 5;
    public LocationDB plotSchematicCenter;
    public LocationDB lobbyCenter;
    public int lobbySize = 10;
    public int lobbyHeight = 10;
    public int lobbyDuration = 60;
    public int gameDuration = 180;
    public int minimumPlayers = 2;
    public List<Location> spawnOffsets = new ArrayList<>();

    public SpleefRunningArena start() {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        WorldPool worlds = Service.get(WorldPool.class);

        World world = worlds.getWorld(type);
        SpleefRunningArena running = new SpleefRunningArena(this, world);

        arenas.addRunning(running);

        return running;
    }
}
