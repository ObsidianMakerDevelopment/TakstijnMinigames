package com.moyskleytech.mc.BuildBattle.listeners;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.BaseArenas;
import com.moyskleytech.mc.BuildBattle.game.BaseRunningArena;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.generator.VoidGen;
import com.moyskleytech.mc.BuildBattle.service.Service;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class ArenaProtection extends Service implements Listener {

    @Override
    public void onLoad() throws ServiceLoadException {
        registerListener();
        super.onLoad();
    }

    public void registerListener() {
        BuildBattle.getInstance().registerListener(this);
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void BlockBreakEvent(BlockBurnEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockDamageEvent(BlockDamageEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void BlockDropItemEvent(BlockDropItemEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {

                event.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void BlockExplodeEvent(BlockExplodeEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockFadeEvent(BlockFadeEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPistonExtendEvent(BlockPistonExtendEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPistonRetractEvent(BlockPistonRetractEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void BlockSpreadEvent(BlockSpreadEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void LeavesDecayEvent(LeavesDecayEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void MoistureChangeEvent(MoistureChangeEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void BlockFromToEvent(BlockFromToEvent event) {
        BaseArenas a = Service.get(BaseArenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            BaseRunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                if (arena.isPreventBuildDestroy()) {
                    event.setCancelled(true);
                } else {
                    if (!arena.belongToPlot(event.getBlock())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void WorldInitEvent(org.bukkit.event.world.WorldInitEvent event) {
        if (event.getWorld().getGenerator() instanceof VoidGen)
        {
            event.getWorld().setKeepSpawnInMemory(false);
            event.getWorld().setAutoSave(false);
            event.getWorld().setSpawnLocation(0, 64, 0);
        }
    }

}
