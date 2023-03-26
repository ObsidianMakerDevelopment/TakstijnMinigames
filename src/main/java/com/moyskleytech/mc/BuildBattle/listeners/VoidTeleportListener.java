package com.moyskleytech.mc.BuildBattle.listeners;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.service.Service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidTeleportListener extends Service implements Listener {

    List<World> worlds = new ArrayList<>();
    int threashold;

    @Override
    public void onLoad() throws ServiceLoadException {
        registerListener();
        super.onLoad();
    }
    public void registerListener() {
        if (ObsidianConfig.getInstance().voidTeleport().enabled()) {
            BuildBattle.getInstance().registerListener(this);

            threashold = ObsidianConfig.getInstance().voidTeleport().threashold();
            for (String wName : ObsidianConfig.getInstance().voidTeleport().worlds()) {
                try{
                    worlds.add(Bukkit.getWorld(wName));
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        Location l = event.getTo();
        if(l.getY() < threashold)
        {
            if(worlds.contains(l.getWorld()))
            {
                event.getPlayer().teleport(l.getWorld().getSpawnLocation());
            }
        }
    }
   
}
