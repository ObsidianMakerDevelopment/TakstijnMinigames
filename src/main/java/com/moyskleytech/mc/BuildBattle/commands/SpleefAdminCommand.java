package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.LocationDB;
import com.moyskleytech.mc.BuildBattle.game.SpleefArena;
import com.moyskleytech.mc.BuildBattle.game.SpleefArenas;
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

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Range;

public class SpleefAdminCommand extends CommandManager.Command {

    private Map<UUID, SpleefArena> editMap = new HashMap<>();

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        editMap.clear();
        super.onLoad();
    }

    public void onPostEnabled() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("spleef admin createarena <arena>")
    @CommandPermission("obsidian.spleef.admin.create")
    private void commandCreate(final Player player,
            final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArena arena = new SpleefArena();
        arena.id = UUID.randomUUID();
        arena.name = map;
        editMap.put(player.getUniqueId(), arena);
        player.sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
    }

    @CommandMethod("spleef admin edit arena <arena>")
    @CommandPermission("obsidian.bb.admin.edit")
    private void commandEdit(final Player player, final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefArena arena = arenas.byName(map);
        editMap.put(player.getUniqueId(), arena);
        if (arena != null)
            player.sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
        else
            player.sendMessage(LanguageConfig.getInstance().error().nonExistingMap(map).component());
    }

    @CommandMethod("spleef admin setmainlobby")
    @CommandPermission("obsidian.spleef.admin.lobby")
    private void commandSetMainLobby(final Player player) {
        ObsidianUtil.setSpleefMainLobby(player.getLocation());
        player.sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>","Lobby").component());
    }

    @CommandMethod("spleef admin removelobby")
    @CommandPermission("obsidian.spleef.admin.lobby")
    private void commandRemoveMainLobby(final CommandSender player) {
        ObsidianUtil.setSpleefMainLobby(null);
        player.sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>","Lobby").component());
    }

    @CommandMethod("spleef admin save")
    @CommandPermission("obsidian.spleef.admin.save")
    private void commandSave(final Player player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
            return;
        }
        if (wip.getLobbyCenter() == null) {
            player.sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        arenas.save(wip);
        player.sendMessage(LanguageConfig.getInstance().editor().saved(wip.name).component());
    }

    @CommandMethod("spleef admin debug")
    @CommandPermission("obsidian.spleef.admin.debug")
    private void commandDebug(final CommandSender player) {
       Logger.setMode(Level.ALL);
       player.sendMessage("[Testing]Now in debug mode");

    }

    @CommandMethod("spleef admin set lobby")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void commandSetLobby(final Player player) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyCenter = LocationDB.fromBukkit(player.getLocation());
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobby").component());
    }

    @CommandMethod("spleef admin set schematic")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void commandSetSchematic(final Player player) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSchematicCenter = LocationDB.fromBukkit(player.getLocation());
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "schematic").component());
    }


    @CommandMethod("spleef admin teleport lobby")
    @CommandPermission("obsidian.spleef.admin.teleport")
    private void commandTeleportLobby(final Player player) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getLobbyCenter() == null) {
            player.sendMessage(LanguageConfig.getInstance().editor().arenaHasNoLobby(wip.getName()).component());
            return;
        }
        player.teleportAsync(wip.getLobbyCenter().toBukkit()).thenAccept(Void -> player
                .sendMessage(LanguageConfig.getInstance().editor().teleportedLobby(wip.getName()).component()));
    }

    @CommandMethod("spleef admin teleport schematic")
    @CommandPermission("obsidian.spleef.admin.teleport")
    private void commandTeleportPlot(final Player player) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
            return;
        }
        player.teleportAsync(wip.getPlotSchematicCenter().toBukkit()).thenAccept(Void -> player
                .sendMessage(LanguageConfig.getInstance().editor().teleportedSchematic(wip.getName()).component()));
    }

    @CommandMethod("spleef admin rename arena <new_arena_name>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void commandRename(final Player player, final @Argument(value = "new_arena_name") String map) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        String oldName = wip.getName();
        wip.name = map;
        player.sendMessage(LanguageConfig.getInstance().editor().renamed(oldName,map).component());
    }

    @CommandMethod("spleef admin set plotSize <plot_size>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void commandPlotSize(final Player player, final @Argument(value = "plot_size") @Range(min="1", max="100") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSize = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotSize").component());
    }

    @CommandMethod("spleef admin set plotHeight <plotHeight>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void commandPlotHeight(final Player player, final @Argument(value = "plotHeight") @Range(min="1", max="300") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotHeight = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotHeight").component());
    }

    @CommandMethod("spleef admin set contourSize <contourSize>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void command_contourSize(final Player player, final @Argument(value = "contourSize") @Range(min="1", max="300") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.contourSize = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "contourSize").component());
    }

    @CommandMethod("spleef admin set lobbySize <lobbySize>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void command_lobbySize(final Player player, final @Argument(value = "lobbySize") @Range(min="1", max="300") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbySize = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbySize").component());
    }

    @CommandMethod("spleef admin set lobbyHeight <lobbyHeight>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void command_lobbyHeight(final Player player, final @Argument(value = "lobbyHeight") @Range(min="1", max="300") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyHeight = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyHeight").component());
    }

    @CommandMethod("spleef admin set duration lobby <lobbyDuration>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void command_lobbyDuration(final Player player, final @Argument(value = "lobbyDuration") @Range(min="1", max="3600") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyDuration = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyDuration").component());
    }

    @CommandMethod("spleef admin set minimumPlayers <minimumPlayers>")
    @CommandPermission("obsidian.spleef.admin.edit")
    private void command_minimumPlayers(final Player player, final @Argument(value = "minimumPlayers") @Range(min="2", max="100") int value) {
        SpleefArena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.minimumPlayers = value;
        player.sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "minimumPlayers").component());
    }
}
