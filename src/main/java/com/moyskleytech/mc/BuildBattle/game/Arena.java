package com.moyskleytech.mc.BuildBattle.game;

import java.util.Optional;

import org.bukkit.World;
import org.bukkit.World.Environment;

import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;

import lombok.Getter;

@Getter
public class Arena extends BaseArena {
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
    public int voteDuration = 30;
    public int winnerDuration = 60;
    public int minimumPlayers = 2;

    public Optional<RunningArena> start() {
        Arenas arenas = Service.get(Arenas.class);
        BaseArenas barenas = Service.get(BaseArenas.class);
        WorldPool worlds = Service.get(WorldPool.class);

        Optional<World> world = worlds.getWorld(type);
        if (world.isPresent()) {
            RunningArena running = new RunningArena(this, world.get());

            arenas.addRunning(running);
            barenas.addRunning(running);

            return Optional.of(running);
        } else
            return Optional.empty();
    }

    public Optional<RunningArena> start(String theme) {
        Arenas arenas = Service.get(Arenas.class);
        WorldPool worlds = Service.get(WorldPool.class);

        Optional<World> world = worlds.getWorld(type);
        if (world.isPresent()) {

            RunningArena running = new RunningArena(this, world.get(), theme);

            arenas.addRunning(running);

            return Optional.of(running);
        } else
            return Optional.empty();
    }
}
