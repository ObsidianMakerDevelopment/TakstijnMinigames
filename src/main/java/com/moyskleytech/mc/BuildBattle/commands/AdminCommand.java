package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.LocationDB;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Logger.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.ConsoleSource;
import org.incendo.cloud.paper.util.sender.PlayerSource;

public class AdminCommand extends CommandManager.Command {

    private Map<UUID, Arena> editMap = new HashMap<>();

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        editMap.clear();
        super.onLoad();
    }

    public void onPostEnabled() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @Command("bb admin createarena <arena>")
    @Permission("obsidian.bb.admin.create")
    private void commandCreate( final PlayerSource player,
            final @Argument(value = "arena", suggestions = "arenas") String map) {
        Arena arena = new Arena();
        arena.id = UUID.randomUUID();
        arena.name = map;
        editMap.put(player.source().getUniqueId(), arena);
        player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
    }

    @Command("bb admin edit arena <arena>")
    @Permission("obsidian.bb.admin.edit")
    private void commandEdit(final PlayerSource player, final @Argument(value = "arena", suggestions = "arenas") String map) {
        Arenas arenas = Service.get(Arenas.class);
        Arena arena = arenas.byName(map);
        editMap.put(player.source().getUniqueId(), arena);
        if (arena != null)
            player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
        else
            player.source().sendMessage(LanguageConfig.getInstance().error().nonExistingMap(map).component());
    }

    @Command("bb admin setmainlobby")
    @Permission("obsidian.bb.admin.lobby")
    private void commandSetMainLobby(final PlayerSource player) {
        ObsidianUtil.setMainLobby(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>","Lobby").component());
    }

    @Command("bb admin removelobby")
    @Permission("obsidian.bb.admin.lobby")
    private void commandRemoveMainLobby(final ConsoleSource player) {
        ObsidianUtil.setMainLobby(null);
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>","Lobby").component());
    }

    @Command("bb admin save")
    @Permission("obsidian.bb.admin.save")
    private void commandSave(final PlayerSource player) {
        Arenas arenas = Service.get(Arenas.class);
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.source().sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
            return;
        }
        if (wip.getLobbyCenter() == null) {
            player.source().sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        arenas.save(wip);
        player.source().sendMessage(LanguageConfig.getInstance().editor().saved(wip.name).component());
    }

    @Command("bb admin debug")
    @Permission("obsidian.bb.admin.debug")
    private void commandDebug(final ConsoleSource player) {
       Logger.setMode(Level.ALL);
       player.source().sendMessage("[Testing]Now in debug mode");

    }

    @Command("bb admin set lobby")
    @Permission("obsidian.bb.admin.edit")
    private void commandSetLobby(final PlayerSource player) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyCenter = LocationDB.fromBukkit(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobby").component());
    }

    @Command("bb admin set schematic")
    @Permission("obsidian.bb.admin.edit")
    private void commandSetSchematic(final PlayerSource player) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSchematicCenter = LocationDB.fromBukkit(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "schematic").component());
    }


    @Command("bb admin teleport lobby")
    @Permission("obsidian.bb.admin.teleport")
    private void commandTeleportLobby(final PlayerSource player) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getLobbyCenter() == null) {
            player.source().sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        player.source().teleportAsync(wip.getLobbyCenter().toBukkit()).thenAccept(Void -> player.source()
                .sendMessage(LanguageConfig.getInstance().editor().teleportedLobby(wip.getName()).component()));
    }

    @Command("bb admin teleport schematic")
    @Permission("obsidian.bb.admin.teleport")
    private void commandTeleportPlot(final PlayerSource player) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.source().sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
            return;
        }
        player.source().teleportAsync(wip.getPlotSchematicCenter().toBukkit()).thenAccept(Void -> player.source()
                .sendMessage(LanguageConfig.getInstance().editor().teleportedSchematic(wip.getName()).component()));
    }

    @Command("bb admin rename arena <new_arena_name>")
    @Permission("obsidian.bb.admin.edit")
    private void commandRename(final PlayerSource player, final @Argument(value = "new_arena_name") String map) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        String oldName = wip.getName();
        wip.name = map;
        player.source().sendMessage(LanguageConfig.getInstance().editor().renamed(oldName,map).component());
    }

    @Command("bb admin set plotSize <plot_size>")
    @Permission("obsidian.bb.admin.edit")
    private void commandPlotSize(final PlayerSource player, final @Argument(value = "plot_size") @Range(min="1", max="100") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSize = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotSize").component());
    }

    @Command("bb admin set plotHeight <plotHeight>")
    @Permission("obsidian.bb.admin.edit")
    private void commandPlotHeight(final PlayerSource player, final @Argument(value = "plotHeight") @Range(min="1", max="300") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotHeight = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotHeight").component());
    }

    @Command("bb admin set contourSize <contourSize>")
    @Permission("obsidian.bb.admin.edit")
    private void command_contourSize(final PlayerSource player, final @Argument(value = "contourSize") @Range(min="1", max="300") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.contourSize = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "contourSize").component());
    }

    @Command("bb admin set lobbySize <lobbySize>")
    @Permission("obsidian.bb.admin.edit")
    private void command_lobbySize(final PlayerSource player, final @Argument(value = "lobbySize") @Range(min="1", max="300") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbySize = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbySize").component());
    }

    @Command("bb admin set lobbyHeight <lobbyHeight>")
    @Permission("obsidian.bb.admin.edit")
    private void command_lobbyHeight(final PlayerSource player, final @Argument(value = "lobbyHeight") @Range(min="1", max="300") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyHeight = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyHeight").component());
    }

    @Command("bb admin set duration lobby <lobbyDuration>")
    @Permission("obsidian.bb.admin.edit")
    private void command_lobbyDuration(final PlayerSource player, final @Argument(value = "lobbyDuration") @Range(min="1", max="3600") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyDuration = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyDuration").component());
    }

    @Command("bb admin set duration game <gameDuration>")
    @Permission("obsidian.bb.admin.edit")
    private void command_gameDuration(final PlayerSource player, final @Argument(value = "gameDuration") @Range(min="1", max="3600") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.gameDuration = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "gameDuration").component());
    }

    @Command("bb admin set duration vote <voteDuration>")
    @Permission("obsidian.bb.admin.edit")
    private void command_voteDuration(final PlayerSource player, final @Argument(value = "voteDuration") @Range(min="1", max="3600") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.voteDuration = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "voteDuration").component());
    }

    @Command("bb admin set duration winner <winnerDuration>")
    @Permission("obsidian.bb.admin.edit")
    private void command_winnerDuration(final PlayerSource player, final @Argument(value = "winnerDuration") @Range(min="1", max="3600") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.winnerDuration = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "winnerDuration").component());
    }

    @Command("bb admin set minimumPlayers <minimumPlayers>")
    @Permission("obsidian.bb.admin.edit")
    private void command_minimumPlayers(final PlayerSource player, final @Argument(value = "minimumPlayers") @Range(min="2", max="100") int value) {
        Arena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.minimumPlayers = value;
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "minimumPlayers").component());
    }
}
