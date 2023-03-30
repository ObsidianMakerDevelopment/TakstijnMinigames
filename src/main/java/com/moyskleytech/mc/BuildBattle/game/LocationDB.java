package com.moyskleytech.mc.BuildBattle.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDB {
    public String world;
    public double x;
    public double y;
    public double z;
    public Location toBukkit()
    {
        Location bukkitLoc = new Location(Bukkit.getWorld(world), x, y, z);
        return bukkitLoc;
    }
    public static LocationDB fromBukkit(Location bukkitLoc)
    {
        LocationDB dbLoc=new LocationDB();
        World world = bukkitLoc.getWorld();
        if(world!=null)
            dbLoc.world = world.getName();
        dbLoc.setX(bukkitLoc.getX());
        dbLoc.setY(bukkitLoc.getY());
        dbLoc.setZ(bukkitLoc.getZ());
        return dbLoc;
    }
}
