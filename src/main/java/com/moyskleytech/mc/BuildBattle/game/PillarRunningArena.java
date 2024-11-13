package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.WinnerMessageConfig;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import com.moyskleytech.mc.BuildBattle.scoreboard.ScoreboardManager;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.services.Paster;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;
import com.moyskleytech.mc.BuildBattle.ui.VotingUI;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unchecked")
@Getter
public class PillarRunningArena implements Listener {
    World world;
    PillarArena arena;
    PillarArenaState state = PillarArenaState.NONE;
    List<Player> players = new ArrayList<>();
    Plot playerPlot;
    Map<UUID, ItemStack[]> playersInventory = new HashMap<>();
    Map<UUID, Integer> playersRank = new HashMap<>();

    boolean blockMovement = false;
    boolean preventBuildDestroy = false;
    private Player winner;
    private int countdown = 0;
    CompletableFuture<Void> currentAction = null;
    private boolean stopping;
    private List<Vector> spawnLoc;

    public String getName() {
        return arena.getName();
    }

    public PillarRunningArena(PillarArena arena, World world) {
        this.arena = arena;
        this.world = world;

        setState(PillarArenaState.LOBBY);
        BuildBattle.getInstance().registerListener(this);
    }

    public CompletableFuture<Void> pasteLobby() {
        if (currentAction != null) {
            return currentAction.thenAccept(Void -> {
                currentAction = pasteLobby();
            });
        }
        blockMovement = true;
        CompletableFuture<Void> pasting = Service.get(Paster.class).paste(arena.lobbyCenter.toBukkit(),
                world.getSpawnLocation(),
                arena.lobbySize, arena.lobbySize, arena.lobbyHeight);

        pasting = pasting.thenAccept(e -> {
            blockMovement = false;
        });

        return currentAction = pasting;
    }

    public CompletableFuture<Void> unpasteLobby() {
        if (currentAction != null) {
            return currentAction.thenAccept(Void -> {
                currentAction = unpasteLobby();
            });
        }
        blockMovement = true;
        CompletableFuture<Void> pasting = Service.get(Paster.class).unpaste(
                world.getSpawnLocation(),
                arena.lobbySize, arena.lobbySize, arena.lobbyHeight);

        return currentAction = pasting;
    }

    public void stop() {
        stopping = true;
        BuildBattle.getInstance().unregisterListener(this);
        new ArrayList<>(players).forEach(this::leave);
        PillarArenas arenas = Service.get(PillarArenas.class);
        arenas.removeRunning(this);

        Logger.trace("Unpasting arena's lobby");
        unpasteLobby().thenAccept(Void -> {
            currentAction = runLaterOrNow().thenAccept(Void3 -> {
                Plot plot = playerPlot;
                Logger.trace("Unpasting arena's plot");

                CompletableFuture<Void> unpastePlot = Service.get(Paster.class).unpaste(plot.center,
                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                        arena.plotHeight + arena.contourSize);

                currentAction = runLaterOrNow().thenAccept(Void4 -> {
                    unpastePlot.thenAccept(Void2 -> {
                        Logger.trace("Unregistering world from used");

                        WorldPool.getInstance().freeWorld(this.world);
                    });
                });
            });
        });
    }

    private CompletableFuture<Void> runLaterOrNow() {
        if (currentAction == null)
            return CompletableFuture.completedFuture(null);
        else
            return currentAction;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public CompletableFuture<Boolean> join(Player p) {
        if (state == PillarArenaState.LOBBY) {
            players.add(p);
            this.playersInventory.put(p.getUniqueId(), p.getInventory().getContents());
            p.getInventory().clear();
            PillarArenas arenas = Service.get(PillarArenas.class);
            arenas.put(p, this);

            // teleport to lobby
            return p.teleportAsync(world.getSpawnLocation()).thenApply(teleport -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);

                try {
                    LanguagePlaceholder lp = LanguageConfig.getInstance().forPillarGameState(PillarArenaState.LOBBY,
                            -1);
                    if (lp != null)
                        p.sendMessage(lp.with(p).component());

                    createScoreboard(p);

                } catch (Throwable t) {
                    t.printStackTrace();
                }

                return teleport;
            });
        }
        return CompletableFuture.completedFuture(false);
        // Do not join if the arena isn't in lobby mode
    }

    public void leave(Player p) {
        players.remove(p);
        if (state != PillarArenaState.LOBBY && state != PillarArenaState.ENDING) {
            if (players.size() == 1) {
                winner = players.get(0);
                setState(PillarArenaState.ENDING);
            }
        }
        if (players.isEmpty()) {
            if (!stopping)
                stop();
        }
        PillarArenas arenas = Service.get(PillarArenas.class);
        arenas.put(p, null);

        ScoreboardManager.getInstance().fromCache(p.getUniqueId()).ifPresent(scoreboard -> scoreboard.destroy());
        p.getInventory().clear();
        p.getInventory().setContents(this.playersInventory.get(p.getUniqueId()));
        // this.playersInventory.put(p.getUniqueId(), p.getInventory().getContents());
        p.teleport(ObsidianUtil.getMainLobby());
    }

    private void setState(PillarArenaState state) {
        if (this.state == state)
            return;
        this.state = state;
        if (state == PillarArenaState.LOBBY) {
            preventBuildDestroy = true;
            pasteLobby();
            countdown = arena.getLobbyDuration();
        }
        if (state == PillarArenaState.STARTING) {
            preventBuildDestroy = true;
            blockMovement = true;

            // Paste Arena

            Location center = world.getSpawnLocation();
            playerPlot = new Plot();
            playerPlot.center = center;
            AtomicInteger i = new AtomicInteger();
            // Paste the plots

            currentAction = runLaterOrNow()
                    .thenAccept(
                            Void3 -> currentAction = ObsidianUtil.future(teleports).thenAccept(ignored_teleport -> {
                                Paster paster = Service.get(Paster.class);

                                // TODO: Create pillars and set pos1/pos2 of arena for unpasting later

                                List<CompletableFuture<Boolean>> teleports = players.stream().map(
                                        player -> {
                                            var pos = i.getAndIncrement();
                                            if (pos < spawnLoc.size()) {
                                                // Teleport players to plots
                                                player.setGameMode(GameMode.SURVIVAL);
                                                player.setAllowFlight(true);
                                                // player.setFlying(true);
                                                player.teleportAsync(playerPlot.center.add(spawnLoc.get(pos)));
                                                return CompletableFuture.completedFuture(true);

                                            } else {
                                                leave(player);
                                                return CompletableFuture.completedFuture(false);
                                            }
                                        }).toList();

                                currentAction = runLaterOrNow().thenAccept(Void4 -> {
                                    setState(PillarArenaState.BATTLE);
                                });
                            }));
        }
        if (state == PillarArenaState.BATTLE) {
            // Start countdown
            preventBuildDestroy = false;
            blockMovement = false;
            countdown = arena.getGameDuration();

            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.SURVIVAL);
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    });
        }

        if (state == PillarArenaState.ENDING) {
            blockMovement = true;
            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    });
            Paster paster = Service.get(Paster.class);
            // Unpaste all plots
            currentAction = runLaterOrNow().thenAccept(Void3 -> {
                Plot plot = playerPlot;
                var plotPasting = paster.unpaste(plot.center,
                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                        arena.plotHeight + arena.contourSize);

                CompletableFuture<Void> unpasterAll = plotPasting;

                currentAction = unpasterAll.thenAccept(e -> stop());
            });
        }
        LanguagePlaceholder lp = LanguageConfig.getInstance().forPillarGameState(state, -1);
        if (lp != null) {
            for (Player p : players)
                p.sendMessage(lp.with(p).component());
        }
    }

    public void tick() {
        LanguagePlaceholder lp = LanguageConfig.getInstance().forPillarGameState(state, countdown);
        if (lp != null) {
            for (Player p : players)
                p.sendMessage(lp.with(p).component());
        }
        if (state == PillarArenaState.LOBBY) {
            players.forEach(player -> player.getInventory().setItem(4,
                    Data.getInstance().getItems().voteItems.get(6).forPlayer(player).build()));
            if (players.size() >= arena.getMinimumPlayers()) {
                // Once enough players have joined reduce the countdown
                countdown--;
                if (countdown <= 0) {
                    setState(PillarArenaState.STARTING);
                }
            }
        }
    }

    private void setWinner(Player p) {
        winner = p;
    }

    public void createScoreboard(Player player) {
        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var title = LanguageConfig
                .getInstance()
                .scoreboard().animatedPillarTitle();

        Scoreboard.builder()
                .player(player)
                .updateInterval(10L)
                .animationInterval(2L)
                .animatedTitle(title)
                .updateCallback(board -> {
                    board.setLines(getScoreboardLines(player, board));
                    return true;
                })
                .build();
    }

    public List<LanguagePlaceholder> getScoreboardLines(Player player, Scoreboard board) {
        List<LanguagePlaceholder> scoreboard_lines = List.of();
        if (state == PillarArenaState.LOBBY)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyPillarScoreboard();
        else if (state == PillarArenaState.STARTING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().startingPillarScoreboard();
        else if (state == PillarArenaState.BATTLE)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().buildingPillarScoreboard();

        return processPlaceholders(scoreboard_lines, board.getPlayer());
    }

    public List<LanguagePlaceholder> processPlaceholders(List<LanguagePlaceholder> source, Player p) {
        final var lines = new ArrayList<LanguagePlaceholder>();
        source.stream()
                .filter(Objects::nonNull)
                .forEach(line -> {
                    lines.add(line.with(p)
                            .replace("%bb_version%", BuildBattle.getInstance().getVersion())
                            .replace("%state%", state.toString())
                            .replace("%countdown%", String.valueOf(countdown))
                            .replace("%winner%", winner != null ? winner.displayName() : Component.empty())
                            .replace("%rank%",
                                    playersRank.containsKey(p.getUniqueId())
                                            ? playersRank.get(p.getUniqueId()).toString()
                                            : "?")

                            .replace("%minutes%", String.valueOf(minutes()))
                            .replace("%seconds%", String.valueOf(seconds()))

                            .replace("%player_count%", String.valueOf(players.size()))
                            .replace("%arena%", arena.getName()));
                });
        return lines;
    }

    public int seconds() {
        return countdown % 60;
    }

    public int minutes() {
        return countdown / 60;
    }

    public boolean belongToPlot(@NotNull Block block) {
        Plot plot = playerPlot;

        double dx = Math.abs(block.getX() - plot.center.getX());
        double dy = Math.abs(block.getY() - plot.center.getY());
        double dz = Math.abs(block.getZ() - plot.center.getZ());

        return dx <= arena.plotSize && dz <= arena.plotSize && dy <= arena.plotHeight;
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent death) {
        if (players.contains(death.getPlayer())) {
            Player pl = death.getPlayer();
            pl.setGameMode(GameMode.SPECTATOR);
            long countLiving = players.stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).count();
            playersRank.put(pl.getUniqueId(), Integer.valueOf((int) countLiving));

            if (countLiving == 1) {
                setWinner(players.stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).findAny().get());
                setState(PillarArenaState.ENDING);
            }
            if (countLiving == 0) {
                setWinner(death.getPlayer());
                setState(PillarArenaState.ENDING);
            }
        }
    }
}
