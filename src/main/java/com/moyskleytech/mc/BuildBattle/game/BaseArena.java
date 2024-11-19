package com.moyskleytech.mc.BuildBattle.game;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;

import lombok.Getter;

@Getter
public abstract class BaseArena {
    public UUID id;
    public String name = "arena";
}
