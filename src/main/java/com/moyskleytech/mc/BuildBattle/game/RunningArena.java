package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;

import lombok.Getter;

@Getter
public class RunningArena {
    World world;
    Arena arena;
    ArenaState state = ArenaState.LOBBY;
    List<Player> players = new ArrayList<>();

    boolean blockMovement = false;
    private Player winner;

    public String getName() {
        return world.getName();
    }

    public RunningArena(Arena arena, World world) {
        this.arena = arena;
        this.world = world;
    }

    public CompletableFuture<Void> pasteLobby() {
        blockMovement = true;
        CompletableFuture<Void> pasting = new CompletableFuture<>();
        pasting = pasting.thenAccept(e -> blockMovement = false);
        // TODO: Actual pasting
        pasting.complete(null);
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
            //TODO:teleport to lobby
        }
        //Do not join if the arena isn't in lobby mode
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
            // TODO: check for amount of player above minimum to start the vote GUI
        }
        if (state == ArenaState.STARTING) {
            // TODO: Teleport players to plots
            // TODO: paste the plots
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
