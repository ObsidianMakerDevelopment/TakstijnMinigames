package com.moyskleytech.mc.BuildBattle.game;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;

public class TickService extends Service implements Listener {
    
    BukkitTask task;
    @Override
    public void onLoad() throws ServiceLoadException {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Arenas arenas = Service.get(Arenas.class);
                arenas.getRunningArenas().forEach(arena->arena.tick());
            }
        }.runTaskTimer(BuildBattle.getInstance(), 20, 20);
        super.onLoad();
    }

    @Override
    public void onUnload() {
        task.cancel();
        super.onUnload();
    }

}
