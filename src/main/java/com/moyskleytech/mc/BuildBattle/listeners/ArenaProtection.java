package com.moyskleytech.mc.BuildBattle.listeners;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;

import io.papermc.paper.event.entity.EntityDamageItemEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    public void BlockPistonEvent(BlockPistonEvent event) {
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

    
}
