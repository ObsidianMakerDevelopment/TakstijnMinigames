package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import com.moyskleytech.mc.BuildBattle.scoreboard.ScoreboardManager;
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
    boolean preventBuildDestroy = false;
    private Player winner;
    private int countdown = 0;
    private int voteIndex = 0;
    private Iterator<Plot> plotsToVote;
    private String theme;
    private Plot current_plot;

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

    public CompletableFuture<Boolean> join(Player p) {
        if (state == ArenaState.LOBBY) {
            players.add(p);
            Arenas arenas = Service.get(Arenas.class);
            arenas.put(p, this);

            // teleport to lobby
            return p.teleportAsync(world.getSpawnLocation()).thenApply(teleport -> {
                return teleport;
            });
        }
        return CompletableFuture.completedFuture(false);
        // Do not join if the arena isn't in lobby mode
    }

    public void leave(Player p) {
        players.remove(p);
        if (state != ArenaState.LOBBY && state != ArenaState.ENDING) {
            if (players.size() == 1) {
                winner = players.get(0);
                setState(ArenaState.SHOWING_WINNER);
            }
        }
        if (players.size() == 0) {
            stop();
        }
        Arenas arenas = Service.get(Arenas.class);
        arenas.put(p, null);

        ScoreboardManager.getInstance().fromCache(p.getUniqueId()).ifPresent(scoreboard -> scoreboard.destroy());
    }

    private void setState(ArenaState state) {

        this.state = state;
        if (state == ArenaState.LOBBY) {
            preventBuildDestroy = true;
            pasteLobby();
            countdown = arena.getLobbyDuration();
        }
        if (state == ArenaState.STARTING) {
            preventBuildDestroy = true;
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
                            playerPlot.owner = player;
                            plots.put(player.getUniqueId(), playerPlot);
                        });
                // Paste the plots

                List<CompletableFuture<Boolean>> teleports = players.stream().map(
                        player -> {
                            // Teleport players to plots
                            player.setGameMode(GameMode.CREATIVE);
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            return player.teleportAsync(plots.get(player.getUniqueId()).center);
                        }).toList();

                CompletableFuture.allOf(
                        (CompletableFuture<Boolean>[]) teleports.toArray()).thenAccept(ignored_teleport -> {
                            Paster paster = Service.get(Paster.class);
                            Object[] plotPasting = plots.values().stream().map(plot -> {
                                return paster.paste(arena.plotSchematicCenter, plot.center,
                                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                                        arena.plotHeight);
                            }).toArray();
                            CompletableFuture<Void> pasterAll = CompletableFuture
                                    .allOf((CompletableFuture<Void>[]) plotPasting);

                            pasterAll.thenAccept(ignored_ -> {
                                setState(ArenaState.BUILDING);
                            });
                        });
            });
        }
        if (state == ArenaState.BUILDING) {
            // Start countdown
            preventBuildDestroy = false;
            blockMovement = false;
            countdown = arena.getGameDuration();
        }
        if (state == ArenaState.SHOWING_BUILDS) {
            preventBuildDestroy = true;
            countdown = arena.getVoteDuration();
            voteIndex = 0;
            plotsToVote = plots.values().iterator();
            showBuildForVote();
        }
        if (state == ArenaState.SHOWING_WINNER) {
            // TODO: Show Score, if present
            preventBuildDestroy = true;

            // Start countdown for next stage
            countdown = arena.getWinnerDuration();
            if (winner != null) {
                Plot plot = plots.get(winner.getUniqueId());
                if (plot != null) {
                    // Teleport to plot
                    players.forEach(player -> player.teleport(plot.center));
                } else {
                    // WTF, winner has no plot?
                    setState(ArenaState.ENDING);
                }
            } else {
                // No winner so go straight to ending
                setState(ArenaState.ENDING);
            }
        }
        if (state == ArenaState.ENDING) {
            blockMovement = true;

            Paster paster = Service.get(Paster.class);
            // Unpaste all plots
            Object[] plotPasting = plots.values().stream().map(plot -> {
                return paster.unpaste(plot.center,
                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                        arena.plotHeight);
            }).toArray();
            CompletableFuture<Void> unpasterAll = CompletableFuture
                    .allOf((CompletableFuture<Void>[]) plotPasting);

            unpasterAll.thenAccept(e -> stop());
        }
    }

    private void showBuildForVote() {
        current_plot = plotsToVote.next();
        // Teleport players to plots, 1 by one
        players.forEach(player -> player.teleport(current_plot.center));

        // TODO: Put voting items in player hotbars
    }

    public void tick() {
        if (state == ArenaState.LOBBY) {
            if (players.size() > arena.getMinimumPlayers()) {
                // TODO: check for amount of player above minimum to start the vote GUI
                // Once enough players have joined reduce the countdown
                countdown--;
                if (countdown == 0) {
                    setState(ArenaState.STARTING);
                }
            }
        }
        if (state == ArenaState.BUILDING) {
            countdown--;
            if (countdown == 0) {
                setState(ArenaState.SHOWING_BUILDS);
            }
        }
        if (state == ArenaState.SHOWING_BUILDS) {
            countdown--;
            if (countdown == 0) {
                voteIndex++;
                if (voteIndex >= plots.size()) {
                    setState(ArenaState.SHOWING_WINNER);
                } else {
                    showBuildForVote();
                }
            }
        }
        if (state == ArenaState.SHOWING_WINNER) {
            countdown--;
            if (countdown == 0) {
                setState(ArenaState.ENDING);
            }
        }
    }

    public void createScoreboard(Player player) {
        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var title = LanguageConfig
                .getInstance()
                .scoreboard().animatedTitle();

        Scoreboard.builder()
                .player(player)
                .displayObjective("bwa-game")
                .updateInterval(10L)
                .animationInterval(2L)
                .animatedTitle(title)
                .updateCallback(board -> {
                    board.setLines(getScoreboardLines(player, board));
                    return true;
                })
                .build();
    }

    public List<String> getScoreboardLines(Player player, Scoreboard board) {
        final var lines = new ArrayList<String>();

        final var arena = Service.get(Arenas.class).getArenaForPlayer(player);

        List<String> scoreboard_lines = List.of();
        if (state == ArenaState.LOBBY)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();
        else if (state == ArenaState.STARTING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();
        else if (state == ArenaState.BUILDING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();
        else if (state == ArenaState.SHOWING_BUILDS)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();
        else if (state == ArenaState.SHOWING_WINNER)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();

        scoreboard_lines.stream()
                .filter(Objects::nonNull)
                .forEach(line -> {
                    line = line
                            .replace("%bb_version%", BuildBattle.getInstance().getVersion())
                            .replace("%theme%", theme)
                            .replace("%state%", state.toString())
                            .replace("%countdown%", String.valueOf(countdown))
                            .replace("%winner%", winner.getDisplayName())
                            .replace("%minutes%", String.valueOf(minutes()))
                            .replace("%seconds%", String.valueOf(seconds()))
                            .replace("%current_plot%", current_plot.owner.getDisplayName())
                            .replace("%player_count%", String.valueOf(arena.players.size()))
                            .replace("%arena%", arena.getName());
                    lines.add(line);
                });
        return lines;
    }

    public int seconds() {
        return countdown % 60;
    }

    public int minutes() {
        return countdown / 60;
    }
}
