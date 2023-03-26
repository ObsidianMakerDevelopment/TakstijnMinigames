package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Paster;

import lombok.Getter;

@SuppressWarnings("unchecked")
@Getter
public class RunningArena {
    World world;
    Arena arena;
    ArenaState state = ArenaState.LOBBY;
    List<Player> players = new ArrayList<>();
    Map<UUID, Plot> plots = new HashMap<>();

    boolean blockMovement = false;
    private Player winner;

    public String getName() {
        return world.getName();
    }

    public RunningArena(Arena arena, World world) {
        this.arena = arena;
        this.world = world;

        setState(ArenaState.LOBBY);
    }

    public CompletableFuture<Void> pasteLobby() {
        blockMovement = true;
        CompletableFuture<Void> pasting = Service.get(Paster.class).paste(arena.lobbyCenter, world.getSpawnLocation(),
                arena.lobbySize, arena.lobbySize, arena.lobbyHeight);

        pasting = pasting.thenAccept(e -> blockMovement = false);

        return pasting;
    }

    public void stop() {
        BuildBattle.getInstance().deleteWorld(this.world);
        Arenas arenas = Service.get(Arenas.class);
        arenas.removeRunning(this);
    }

    public void join(Player p) {
        if (state == ArenaState.LOBBY) {
            players.add(p);
            Arenas arenas = Service.get(Arenas.class);
            arenas.put(p, this);
            // TODO:teleport to lobby
        }
        // Do not join if the arena isn't in lobby mode
    }

    public void leave(Player p) {
        players.remove(p);
        if (players.size() == 1) {
            winner = players.get(0);
            setState(ArenaState.SHOWING_WINNER);
        }
        if (players.size() == 0) {
            stop();
        }
        Arenas arenas = Service.get(Arenas.class);
        arenas.put(p, null);
    }

    private void setState(ArenaState state) {

        this.state = state;
        if (state == ArenaState.LOBBY) {
            pasteLobby();
            // TODO: check for amount of player above minimum to start the vote GUI, not
            // here but as players joins
        }
        if (state == ArenaState.STARTING) {
            blockMovement = true;
            CompletableFuture<Void> unpasteLobby = Service.get(Paster.class).unpaste(world.getSpawnLocation(),
                    arena.lobbySize, arena.lobbySize, arena.lobbyHeight);
            unpasteLobby.thenAccept(ignored -> {
                AtomicInteger plotIds = new AtomicInteger(0);
                players.forEach(
                        player -> {
                            // Create plot
                            int plot = plotIds.incrementAndGet();
                            Location center = world.getSpawnLocation().add(plot * (arena.plotSize + arena.contourSize),
                                    0, 0);
                            Plot playerPlot = new Plot();
                            playerPlot.center = center;
                            plots.put(player.getUniqueId(), playerPlot);
                        });
                // Paste the plots
                Object[] plotPasting = plots.values().stream().map(plot -> {
                    return new CompletableFuture<Void>();
                }).toArray();
                CompletableFuture<Void> pasterAll = CompletableFuture.allOf((CompletableFuture<Void>[]) plotPasting);
                players.forEach(
                        player -> {
                            // Teleport players to plots
                            blockMovement = false;
                            player.teleport(plots.get(player.getUniqueId()).center);
                            // Teleport players to plots
                            blockMovement = true;
                        });
                pasterAll.thenAccept(ignored_ -> {
                    setState(ArenaState.BUILDING);
                    blockMovement = false;
                });
            });
        }
        if (state == ArenaState.BUILDING) {
            // TODO: Start countdown
        }
        if (state == ArenaState.SHOWING_BUILDS) {
            // TODO: Teleport players to plots, 1 by one
            // TODO: Put voting items in player hotbars
        }
        if (state == ArenaState.SHOWING_WINNER) {
            // TODO: Show Score, if present
            // TODO: Teleport to plot
            // TODO: Start countdown for next stage
        }
        if (state == ArenaState.ENDING) {
            blockMovement = true;
            CompletableFuture<Void> unpaster = new CompletableFuture<>();

            unpaster.thenAccept(e -> stop());
            // TODO: Unpaste all plots
            unpaster.complete(null);
        }
        // TODO: Implement
    }
}
