package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;
import com.moyskleytech.obsidian.material.ObsidianItemTemplate;

import lombok.Getter;

@Getter
public class PillarArena extends BaseArena {
    public UUID id;
    public Environment type = Environment.NORMAL;
    public String name = "arena";
    public int contourSize = 5;
    public LocationDB lobbyCenter;
    public int lobbySize = 10;
    public int lobbyHeight = 10;
    public int lobbyDuration = 60;
    public int gameDuration = 180;
    public int minimumPlayers = 2;
    public int distance = 10;
    public int timeBetweenItems = 2;
    public List<ObsidianItemTemplate> additionnalItems = new ArrayList<>();

    public Optional<PillarRunningArena> start() {
        PillarArenas arenas = Service.get(PillarArenas.class);
        WorldPool worlds = Service.get(WorldPool.class);

        Optional<World> world = worlds.getWorld(type);
        if (world.isPresent()) {
            PillarRunningArena running = new PillarRunningArena(this, world.get());

            arenas.addRunning(running);

            return Optional.of(running);
        }
        else
            return Optional.empty();
    }
}
