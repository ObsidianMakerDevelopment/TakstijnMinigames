package com.moyskleytech.mc.BuildBattle.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Plot {
    public Location center;
    public Player owner;
    public Map<UUID, Integer> vote = new HashMap<>();//0-5
    public int getScore()
    {
        AtomicInteger score=new AtomicInteger();
        vote.values().forEach(v->{
            if(v!=null)
                score.addAndGet(v.intValue());
        });
        return score.get();
    }
}
