package com.moyskleytech.mc.BuildBattle.game;

import org.bukkit.event.Listener;

import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler.Task;

public class TickService extends Service implements Listener {

    Task task;

    @Override
    public void onLoad() throws ServiceLoadException {
        task=Scheduler.getInstance().runTaskTimerAsync((task)->{
            Arenas arenas = Service.get(Arenas.class);
            arenas.getRunningArenas().forEach(RunningArena::tick);

            SpleefArenas sarenas = Service.get(SpleefArenas.class);
            sarenas.getRunningArenas().forEach(SpleefRunningArena::tick);

            PillarArenas parenas = Service.get(PillarArenas.class);
            parenas.getRunningArenas().forEach(PillarRunningArena::tick);
        }, 20, 20);
        super.onLoad();
    }

    @Override
    public void onUnload() {
        task.cancel();
        super.onUnload();
    }

}
