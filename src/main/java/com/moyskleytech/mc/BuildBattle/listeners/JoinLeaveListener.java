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

public class JoinLeaveListener extends Service implements Listener {

    List<World> worlds = new ArrayList<>();
    int threashold;

    @Override
    public void onLoad() throws ServiceLoadException {
        registerListener();
        super.onLoad();
    }

    public void registerListener() {
        BuildBattle.getInstance().registerListener(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Arenas a = Service.get(Arenas.class);
        RunningArena arena = a.getArenaForPlayer(event.getPlayer());
        if (arena != null) {
            arena.leave(event.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Arenas a = Service.get(Arenas.class);
        RunningArena arena = a.getArenaForPlayer(event.getPlayer());
        if (arena != null) {
            if (arena.isBlockMovement()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Arenas a = Service.get(Arenas.class);
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            RunningArena arena = a.getArenaForPlayer(player);
            if (arena != null) {
                event.setCancelled(isEnabled());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        Arenas a = Service.get(Arenas.class);
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            RunningArena arena = a.getArenaForPlayer(player);
            if (arena != null) {
                event.setCancelled(isEnabled());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Arenas a = Service.get(Arenas.class);
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            RunningArena arena = a.getArenaForPlayer(player);
            if (arena != null) {
                event.setCancelled(isEnabled());
            }
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        Arenas a = Service.get(Arenas.class);
        if (a.isArena(event.getLocation().getWorld())) {
            if (event.getEntity() instanceof LivingEntity living)
                living.setAI(false);
        }
    }

}
