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
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import com.moyskleytech.obsidian.material.ObsidianItemTemplate;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

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
    private List<Location> spawnLoc = new ArrayList<>();

    Location pos1, pos2, center;
    double borderSize = 0;
    WorldBorder worldBorder;
    int secondsTillNextItem = 0;
    private List<ObsidianItemTemplate> possibleBlocks = new ArrayList<>();

    public String getName() {
        return arena.getName();
    }

    public PillarRunningArena(PillarArena arena, World world) {
        this.arena = arena;
        this.world = world;
        this.secondsTillNextItem = arena.getTimeBetweenItems();

        // create the list of possible blocks
        for (Material m : Material.values()) {
            if (m.isBlock() && !m.isInteractable() && !m.isAir()) {
                possibleBlocks.add(new ObsidianItemTemplate(ObsidianMaterial.wrap(m)));
            }
            possibleBlocks.addAll(arena.getAdditionnalItems());
        }

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

                // unpaste according to created size
                CompletableFuture<Void> unpastePlot = Service.get(Paster.class).unpaste(pos1, pos2, null);

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

            currentAction = runLaterOrNow()
                    .thenAccept(
                            Void3 -> {
                                Paster paster = Service.get(Paster.class);

                                // TODO: Create pillars and set pos1/pos2 of arena for unpasting later

                                double wx = world.getSpawnLocation().getBlockX();
                                double wy = world.getSpawnLocation().getBlockY();
                                double wz = world.getSpawnLocation().getBlockZ();

                                double sq = Math.ceil(Math.sqrt(players.size()));
                                for (int s = 0, i = 0, z = 0; i < sq; i++) {
                                    if (s == sq) {
                                        s = 0;
                                        z++;
                                    }

                                    spawnLoc.add(new Location(world, wx + 50 + s * arena.getDistance(), wy,
                                            wz + arena.getDistance() * z));
                                }
                                for (Location l : spawnLoc) {
                                    int y = l.getBlockY();
                                    while (y > world.getMinHeight()) {
                                        Block b = world.getBlockAt(l.getBlockX(), y, l.getBlockZ());
                                        BlockState bstate = b.getState();
                                        bstate.setType(Material.BEDROCK);
                                        bstate.update(true, false);
                                    }
                                }

                                pos1 = new Location(world, wx + 50 - arena.getDistance(), wy, wz - arena.getDistance());
                                pos2 = new Location(world, wx + 50 + (sq + 1) * arena.getDistance(), wy,
                                        wz + arena.getDistance() * (sq + 1));

                                center = pos1.add(pos2).multiply(0.5);

                                worldBorder = Bukkit.getServer().createWorldBorder();
                                worldBorder.setDamageAmount(0);
                                worldBorder.setDamageBuffer(0);
                                worldBorder.setCenter(center);
                                worldBorder.setSize((sq / 2 + 2) * arena.getDistance());

                                AtomicInteger ai = new AtomicInteger();
                                List<CompletableFuture<Boolean>> teleports = players.stream().map(
                                        player -> {
                                            var pos = ai.getAndIncrement();
                                            if (pos < spawnLoc.size()) {
                                                // Teleport players to plots
                                                player.setGameMode(GameMode.SURVIVAL);
                                                player.setAllowFlight(true);

                                                player.setWorldBorder(worldBorder);

                                                // player.setWorldBorder(Bukkit.getServer().createWorldBorder().;

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
                            });
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
                // Plot plot = playerPlot;

                // var plotPasting = paster.unpaste(plot.center,
                // arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                // arena.plotHeight + arena.contourSize);

                // CompletableFuture<Void> unpasterAll = plotPasting;

                stop();
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
        if (state == PillarArenaState.BATTLE) {
            for (Player player : players)
                player.setWorldBorder(worldBorder);

            secondsTillNextItem--;
            if (secondsTillNextItem <= 0) {
                secondsTillNextItem = arena.getTimeBetweenItems();

                // give a random item to the player
                Collections.shuffle(possibleBlocks);
                int index = 0;
                for (Player player : players) {
                    if (player.getGameMode() == GameMode.SURVIVAL)
                        player.getInventory().addItem(possibleBlocks.get(index++).build());
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

    @EventHandler
    public void playerDeath(PlayerDeathEvent death) {
        if (players.contains(death.getPlayer())) {
            Player pl = death.getPlayer();
            pl.setGameMode(GameMode.ADVENTURE);
            pl.getInventory().clear();
            pl.teleport(pl.getWorld().getSpawnLocation());

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
