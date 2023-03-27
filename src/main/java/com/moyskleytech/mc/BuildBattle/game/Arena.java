package com.moyskleytech.mc.BuildBattle.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;

import lombok.Getter;

@Getter
public class Arena {
    public Environment type = Environment.NORMAL;
    public int plotSize = 20;
    public int plotHeight=40;
    public String name = "arena";
    public int contourSize = 5;
    public Location plotSchematicCenter;
    public Location lobbyCenter;
    public int lobbySize=10;
    public int lobbyHeight=10;
    public int lobbyDuration=60;
    public int gameDuration=180;
    public int voteDuration=30;
    public int winnerDuration=60;
    public int minimumPlayers=2;
    public RunningArena start()
    {
        Arenas arenas = Service.get(Arenas.class);
        
        World world = BuildBattle.getInstance().createEmptyWorld(type, name+"_"+arenas.getRunningArenas().size());
        RunningArena running = new RunningArena(this,world);

        arenas.addRunning(running);

        return running;
    }
}
