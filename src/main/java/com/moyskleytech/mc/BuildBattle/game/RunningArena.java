package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.Collections;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import com.moyskleytech.mc.BuildBattle.scoreboard.ScoreboardManager;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Paster;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;
import com.moyskleytech.mc.BuildBattle.ui.VotingUI;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unchecked")
@Getter
public class RunningArena implements Listener {
    World world;
    Arena arena;
    ArenaState state = ArenaState.LOBBY;
    List<Player> players = new ArrayList<>();
    Map<UUID, Plot> plots = new HashMap<>();
    Map<UUID, VotingUI> votingUIs = new HashMap<>();
    List<AtomicInteger> voting = new ArrayList<>();
    List<String> themes = new ArrayList<>();

    boolean blockMovement = false;
    boolean preventBuildDestroy = false;
    private Player winner;
    private int countdown = 0;
    private int voteIndex = 0;
    private Iterator<Plot> plotsToVote;
    private String theme;
    private Plot current_plot;
    CompletableFuture<Void> currentAction = null;
    private boolean stopping;

    public String getName() {
        return arena.getName();
    }

    public RunningArena(Arena arena, World world) {
        this.arena = arena;
        this.world = world;

        setState(ArenaState.LOBBY);

        List<String> tmpList = new ArrayList<>(ObsidianConfig.getInstance().themes());
        Collections.shuffle(tmpList);
        for (int i = 0; i < 5; i++) {
            if (i < tmpList.size()) {
                themes.add(tmpList.get(0));
                voting.add(new AtomicInteger());
            }
        }

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
        Arenas arenas = Service.get(Arenas.class);
        arenas.removeRunning(this);

        Logger.trace("Unpasting arena's lobby");
        unpasteLobby().thenAccept(Void -> {
            currentAction = runLaterOrNow().thenAccept(Void3 -> {
                var plotUnpaste = plots.values().stream().map(plot -> {
                    Logger.trace("Unpasting arena's plot");

                    CompletableFuture<Void> unpastePlot = Service.get(Paster.class).unpaste(plot.center,
                            arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                            arena.plotHeight + arena.contourSize);
                    return unpastePlot;
                }).toList();

                currentAction = runLaterOrNow().thenAccept(Void4 -> {
                    ObsidianUtil.future(plotUnpaste).thenAccept(Void2 -> {
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

    public CompletableFuture<Boolean> join(Player p) {
        if (state == ArenaState.LOBBY) {
            players.add(p);
            Arenas arenas = Service.get(Arenas.class);
            arenas.put(p, this);

            // teleport to lobby
            return p.teleportAsync(world.getSpawnLocation()).thenApply(teleport -> {
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
                createScoreboard(p);

                try {
                    VotingUI vi = new VotingUI(p, themes, voting);
                    votingUIs.values().forEach(ovi -> {
                        vi.attach(ovi);
                        ovi.attach(vi);
                    });
                    votingUIs.put(p.getUniqueId(), vi);

                    p.openInventory(vi.getInventory());
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
        if (state != ArenaState.LOBBY && state != ArenaState.ENDING) {
            if (players.size() == 1) {
                winner = players.get(0);
                setState(ArenaState.SHOWING_WINNER);
            }
        }
        if (players.size() == 0) {
            if (!stopping)
                stop();
        }
        Arenas arenas = Service.get(Arenas.class);
        arenas.put(p, null);

        if (state == ArenaState.LOBBY) {
            VotingUI vi = votingUIs.get(p.getUniqueId());
            vi.removeVote();
            if (vi != null) {
                votingUIs.remove(p.getUniqueId());
                votingUIs.values().forEach(ovi -> {
                    vi.detach(ovi);
                    ovi.detach(vi);
                });
            }
        }

        ScoreboardManager.getInstance().fromCache(p.getUniqueId()).ifPresent(scoreboard -> scoreboard.destroy());

        p.teleport(ObsidianUtil.getMainLobby());
    }

    private void setState(ArenaState state) {

        this.state = state;
        if (state == ArenaState.LOBBY) {
            preventBuildDestroy = true;
            pasteLobby();
            countdown = arena.getLobbyDuration();
        }
        if (state == ArenaState.STARTING) {
            int index = 0, max = 0;
            for (int i = 0; i < voting.size(); i++) {
                if (voting.get(i).intValue() > max) {
                    max = voting.get(i).intValue();
                    index = i;
                }
            }
            theme = themes.get(index);

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
                currentAction = runLaterOrNow()
                        .thenAccept(
                                Void3 -> currentAction = ObsidianUtil.future(teleports).thenAccept(ignored_teleport -> {
                                    Paster paster = Service.get(Paster.class);
                                    currentAction = runLaterOrNow().thenAccept(Void4 -> {
                                        var plotPasting = plots.values().stream().map(plot -> {
                                            return paster.paste(arena.plotSchematicCenter.toBukkit(), plot.center,
                                                    arena.plotSize + arena.contourSize,
                                                    arena.plotSize + arena.contourSize,
                                                    arena.plotHeight);
                                        }).toList();
                                        CompletableFuture<Void> pasterAll = ObsidianUtil
                                                .future(plotPasting);

                                        currentAction = pasterAll.thenAccept(ignored_ -> {
                                            setState(ArenaState.BUILDING);
                                        });
                                    });
                                }));
            });
        }
        if (state == ArenaState.BUILDING) {
            // Start countdown
            preventBuildDestroy = false;
            blockMovement = false;
            countdown = arena.getGameDuration();

            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    });
        }
        if (state == ArenaState.SHOWING_BUILDS) {
            preventBuildDestroy = true;
            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    });
            countdown = arena.getVoteDuration();
            voteIndex = 0;
            plotsToVote = plots.values().iterator();
            showBuildForVote();
        }
        if (state == ArenaState.SHOWING_WINNER) {
            // TODO: Show Score, if present
            // BIG CHAT MESSAGE WITH EVERYONE SCORE

            preventBuildDestroy = true;
            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    });
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
                var plotPasting = plots.values().stream().map(plot -> {
                    return paster.unpaste(plot.center,
                            arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                            arena.plotHeight);
                }).toList();
                CompletableFuture<Void> unpasterAll = ObsidianUtil.future(plotPasting);

                currentAction = unpasterAll.thenAccept(e -> stop());
            });
        }
    }

    private void showBuildForVote() {
        current_plot = plotsToVote.next();
        // Teleport players to plots, 1 by one
        players.forEach(player -> {
            player.teleport(current_plot.center);
            // Put voting items in player hotbars
            player.getInventory().clear();
            for (int i = 0; i < 6; i++)
                player.getInventory().setItem(i, ObsidianConfig.getInstance().getVoteItem(i).forPlayer(player).build());
        });

    }

    public void tick() {
        if (state == ArenaState.LOBBY) {
            if (players.size() >= arena.getMinimumPlayers()) {
                // check for amount of player above minimum to start the vote GUI
                players.forEach(player -> player.getInventory().clear());
                players.forEach(player -> player.openInventory(votingUIs.get(player.getUniqueId()).getInventory()));
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
                    setWinner();
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

    private void setWinner() {
        Plot max = null;
        for (Plot p : plots.values()) {
            if (max == null || max.getScore() < p.getScore())
                max = p;
        }
        if (max != null)
            winner = max.owner;
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
        final var lines = new ArrayList<LanguagePlaceholder>();
        final var arena = Service.get(Arenas.class).getArenaForPlayer(player);

        List<LanguagePlaceholder> scoreboard_lines = List.of();
        if (state == ArenaState.LOBBY)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbyScoreboard();
        else if (state == ArenaState.STARTING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().startingScoreboard();
        else if (state == ArenaState.BUILDING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().buildingScoreboard();
        else if (state == ArenaState.SHOWING_BUILDS)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().votingScoreboard();
        else if (state == ArenaState.SHOWING_WINNER)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().winnerScoreboard();

        scoreboard_lines.stream()
                .filter(Objects::nonNull)
                .forEach(line -> {
                    lines.add(line.with(board.getPlayer())
                            .replace("%bb_version%", BuildBattle.getInstance().getVersion())
                            .replace("%theme%", theme)
                            .replace("%state%", state.toString())
                            .replace("%countdown%", String.valueOf(countdown))
                            .replace("%winner%", winner != null ? winner.displayName() : Component.empty())
                            .replace("%winnerscore%",
                                    winner != null
                                            ? ObsidianUtil.component(
                                                    String.valueOf(plots.get(winner.getUniqueId()).getScore()))
                                            : Component.empty())
                            .replace("%minutes%", String.valueOf(minutes()))
                            .replace("%seconds%", String.valueOf(seconds()))
                            .replace("%current_plot%",
                                    current_plot != null ? current_plot.owner.displayName() : Component.empty())
                            .replace("%player_count%", String.valueOf(arena.players.size()))
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
        return plots.values().stream().anyMatch(plot -> {
            double dx = Math.abs(block.getX() - plot.center.getX());
            double dy = Math.abs(block.getY() - plot.center.getY());
            double dz = Math.abs(block.getZ() - plot.center.getZ());

            return dx <= arena.plotSize && dz <= arena.plotSize && dy <= arena.plotHeight;
        });
    }
}
