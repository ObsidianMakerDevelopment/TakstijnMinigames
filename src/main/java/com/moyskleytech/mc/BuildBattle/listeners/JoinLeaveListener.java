package com.moyskleytech.mc.BuildBattle.listeners;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.ArenaState;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.ui.UI;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

import io.papermc.paper.event.entity.EntityDamageItemEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

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
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.*;

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

    @EventHandler
    public void InventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Arenas a = Service.get(Arenas.class);
        Player player = (Player) event.getInitiator().getViewers().get(0);
        RunningArena arena = a.getArenaForPlayer(player);
        if (arena != null) {
            if (arena.isPreventBuildDestroy()) {
                Logger.trace("InventoryMoveItemEvent");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        Arenas a = Service.get(Arenas.class);
        Player player = (Player) event.getPlayer();
        RunningArena arena = a.getArenaForPlayer(player);
        if (arena != null) {
            if (arena.getState() == ArenaState.SHOWING_BUILDS) {
                Player p = event.getPlayer();
                int slot = p.getInventory().getHeldItemSlot();
                if (slot <= 6) {
                    arena.getCurrent_plot().vote.put(p.getUniqueId(), slot - 1);
                    p.sendMessage(LanguageConfig.getInstance().voted().with(p).component());

                    try {
                        Sound musicDisc = Sound.sound(Key.key("entity.cat.hiss"), Sound.Source.MUSIC, 1f, 1f);
                        p.playSound(musicDisc);
                    } catch (Throwable t) {

                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void PlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        Arenas a = Service.get(Arenas.class);
        Player player = (Player) event.getPlayer();
        RunningArena arena = a.getArenaForPlayer(player);
        if (arena != null) {
            if (arena.getState() == ArenaState.SHOWING_BUILDS) {
                Player p = event.getPlayer();
                int slot = p.getInventory().getHeldItemSlot();
                if (slot <= 6) {
                    arena.getCurrent_plot().vote.put(p.getUniqueId(), slot - 1);
                    p.sendMessage(LanguageConfig.getInstance().voted().with(p).component());

                    try {
                        Sound musicDisc = Sound.sound(Key.key("entity.cat.hiss"), Sound.Source.MUSIC, 1f, 1f);
                        p.playSound(musicDisc);
                    } catch (Throwable t) {

                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void PlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Arenas a = Service.get(Arenas.class);
        Player player = (Player) event.getPlayer();
        RunningArena arena = a.getArenaForPlayer(player);
        if (arena != null) {
            if (arena.getState() == ArenaState.SHOWING_BUILDS) {
                Player p = event.getPlayer();
                int slot = p.getInventory().getHeldItemSlot();
                if (slot <= 6) {
                    arena.getCurrent_plot().vote.put(p.getUniqueId(), slot - 1);
                    p.sendMessage(LanguageConfig.getInstance().voted().with(p).component());

                    try {
                        Sound musicDisc = Sound.sound(Key.key("entity.cat.hiss"), Sound.Source.MUSIC, 1f, 1f);
                        p.playSound(musicDisc);
                    } catch (Throwable t) {

                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getInventory().getHolder() != null
                && event.getInventory().getHolder() instanceof UI) {

            event.setCancelled(true);
            if (event.getClickedInventory() == event.getInventory()) {
                UI gui = (UI) event.getInventory().getHolder();
                gui.click(event);
            }
        }
    }

}
