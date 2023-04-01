package com.moyskleytech.mc.BuildBattle.listeners;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockDamageEvent(BlockDamageEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {

                event.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void BlockExplodeEvent(BlockExplodeEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockFadeEvent(BlockFadeEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPistonExtendEvent(BlockPistonExtendEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPistonRetractEvent(BlockPistonRetractEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
                    .filter(ra -> ra.getWorld() == event.getBlock().getWorld()).findAny().orElse(null);
            if (arena != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getBlock().getWorld())) {
            RunningArena arena = a.getRunningArenas().stream()
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
