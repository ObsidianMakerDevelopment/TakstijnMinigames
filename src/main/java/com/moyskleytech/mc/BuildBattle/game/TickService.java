package com.moyskleytech.mc.BuildBattle.game;

import org.bukkit.event.Listener;

import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler.Task;

public class TickService extends Service implements Listener {

    Task task;

    @Override
    public void onLoad() throws ServiceLoadException {
        task=Scheduler.getInstance().runTaskTimer((task)->{
            BaseArenas arenas = Service.get(BaseArenas.class);
            arenas.getRunningArenas().forEach(BaseRunningArena::tick);
        }, 20, 20);
        super.onLoad();
    }

    @Override
    public void onUnload() {
        task.cancel();
        super.onUnload();
    }

}
