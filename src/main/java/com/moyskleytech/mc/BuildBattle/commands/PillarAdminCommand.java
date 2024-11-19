package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.LocationDB;
import com.moyskleytech.mc.BuildBattle.game.PillarArena;
import com.moyskleytech.mc.BuildBattle.game.PillarArenas;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Logger.Level;
import com.moyskleytech.obsidian.material.ObsidianItemTemplate;

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
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

public class PillarAdminCommand extends CommandManager.Command {

    private Map<UUID, PillarArena> editMap = new HashMap<>();

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        editMap.clear();
        super.onLoad();
    }

    public void onPostEnabled() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @Command("rpillar admin createarena <arena>")
    @Permission("obsidian.pillar.admin.create")
    private void commandCreate(final PlayerSource player,
            final @Argument(value = "arena", suggestions = "Pillararenas") String map) {
        PillarArena arena = new PillarArena();
        arena.id = UUID.randomUUID();
        arena.name = map;
        editMap.put(player.source().getUniqueId(), arena);
        player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
    }

    @Command("rpillar admin edit arena <arena>")
    @Permission("obsidian.bb.admin.edit")
    private void commandEdit(final PlayerSource player,
            final @Argument(value = "arena", suggestions = "Pillararenas") String map) {
        PillarArenas arenas = Service.get(PillarArenas.class);
        PillarArena arena = arenas.byName(map);
        editMap.put(player.source().getUniqueId(), arena);
        if (arena != null)
            player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
        else
            player.source().sendMessage(LanguageConfig.getInstance().error().nonExistingMap(map).component());
    }

    @Command("rpillar admin setmainlobby")
    @Permission("obsidian.pillar.admin.lobby")
    private void commandSetMainLobby(final PlayerSource player) {
        ObsidianUtil.setPillarMainLobby(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>", "Lobby").component());
    }

    @Command("rpillar admin removelobby")
    @Permission("obsidian.pillar.admin.lobby")
    private void commandRemoveMainLobby(final Source player) {
        ObsidianUtil.setPillarMainLobby(null);
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>", "Lobby").component());
    }

    @Command("rpillar admin save")
    @Permission("obsidian.pillar.admin.save")
    private void commandSave(final PlayerSource player) {
        PillarArenas arenas = Service.get(PillarArenas.class);
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
       
        if (wip.getLobbyCenter() == null) {
            player.source()
                    .sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        arenas.save(wip);
        player.source().sendMessage(LanguageConfig.getInstance().editor().saved(wip.name).component());
    }

    @Command("rpillar admin debug")
    @Permission("obsidian.pillar.admin.debug")
    private void commandDebug(final Source player) {
        Logger.setMode(Level.ALL);
        player.source().sendMessage("[Testing]Now in debug mode");

    }

    @Command("rpillar admin set lobby")
    @Permission("obsidian.pillar.admin.edit")
    private void commandSetLobby(final PlayerSource player) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyCenter = LocationDB.fromBukkit(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobby").component());
    }

    @Command("rpillar admin teleport lobby")
    @Permission("obsidian.pillar.admin.teleport")
    private void commandTeleportLobby(final PlayerSource player) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getLobbyCenter() == null) {
            player.source()
                    .sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        player.source().teleportAsync(wip.getLobbyCenter().toBukkit()).thenAccept(Void -> player.source()
                .sendMessage(LanguageConfig.getInstance().editor().teleportedLobby(wip.getName()).component()));
    }

    @Command("rpillar admin rename arena <new_arena_name>")
    @Permission("obsidian.pillar.admin.edit")
    private void commandRename(final PlayerSource player, final @Argument(value = "new_arena_name") String map) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        String oldName = wip.getName();
        wip.name = map;
        player.source().sendMessage(LanguageConfig.getInstance().editor().renamed(oldName, map).component());
    }

    @Command("rpillar admin set lobbySize <lobbySize>")
    @Permission("obsidian.pillar.admin.edit")
    private void command_lobbySize(final PlayerSource player,
            final @Argument(value = "lobbySize") @Range(min = "1", max = "300") int value) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbySize = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbySize").component());
    }

    @Command("rpillar admin set lobbyHeight <lobbyHeight>")
    @Permission("obsidian.pillar.admin.edit")
    private void command_lobbyHeight(final PlayerSource player,
            final @Argument(value = "lobbyHeight") @Range(min = "1", max = "300") int value) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyHeight = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyHeight").component());
    }

    @Command("rpillar admin set duration lobby <lobbyDuration>")
    @Permission("obsidian.pillar.admin.edit")
    private void command_lobbyDuration(final PlayerSource player,
            final @Argument(value = "lobbyDuration") @Range(min = "1", max = "3600") int value) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyDuration = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyDuration").component());
    }

    @Command("rpillar admin set minimumPlayers <minimumPlayers>")
    @Permission("obsidian.pillar.admin.edit")
    private void command_minimumPlayers(final PlayerSource player,
            final @Argument(value = "minimumPlayers") @Range(min = "2", max = "100") int value) {
        PillarArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.minimumPlayers = value;
        player.source().sendMessage(
                LanguageConfig.getInstance().editor().changed(wip.getName(), "minimumPlayers").component());
    }
}
