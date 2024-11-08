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

    @Command("spleef admin createarena <arena>")
    @Permission("obsidian.spleef.admin.create")
    private void commandCreate(final PlayerSource player,
            final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArena arena = new SpleefArena();
        arena.id = UUID.randomUUID();
        arena.name = map;
        editMap.put(player.source().getUniqueId(), arena);
        player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
    }

    @Command("spleef admin edit arena <arena>")
    @Permission("obsidian.bb.admin.edit")
    private void commandEdit(final PlayerSource player,
            final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefArena arena = arenas.byName(map);
        editMap.put(player.source().getUniqueId(), arena);
        if (arena != null)
            player.source().sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
        else
            player.source().sendMessage(LanguageConfig.getInstance().error().nonExistingMap(map).component());
    }

    @Command("spleef admin setmainlobby")
    @Permission("obsidian.spleef.admin.lobby")
    private void commandSetMainLobby(final PlayerSource player) {
        ObsidianUtil.setSpleefMainLobby(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>", "Lobby").component());
    }

    @Command("spleef admin removelobby")
    @Permission("obsidian.spleef.admin.lobby")
    private void commandRemoveMainLobby(final Source player) {
        ObsidianUtil.setSpleefMainLobby(null);
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed("<Plugin>", "Lobby").component());
    }

    @Command("spleef admin save")
    @Permission("obsidian.spleef.admin.save")
    private void commandSave(final PlayerSource player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.source()
                    .sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
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

    @Command("spleef admin debug")
    @Permission("obsidian.spleef.admin.debug")
    private void commandDebug(final Source player) {
        Logger.setMode(Level.ALL);
        player.source().sendMessage("[Testing]Now in debug mode");

    }

    @Command("spleef admin set lobby")
    @Permission("obsidian.spleef.admin.edit")
    private void commandSetLobby(final PlayerSource player) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyCenter = LocationDB.fromBukkit(player.source().getLocation());
        player.source().sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobby").component());
    }

    @Command("spleef admin set schematic")
    @Permission("obsidian.spleef.admin.edit")
    private void commandSetSchematic(final PlayerSource player) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSchematicCenter = LocationDB.fromBukkit(player.source().getLocation());
        wip.spawnOffsets.clear();
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "schematic").component());
    }
    @Command("spleef admin add spawn")
    @Permission("obsidian.spleef.admin.edit")
    private void commandAdd(final PlayerSource player) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.spawnOffsets.add(LocationDB.fromBukkit(player.source().getLocation().subtract(wip.plotSchematicCenter.toBukkit())));
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "schematic").component());
    }

    @Command("spleef admin set tool")
    @Permission("obsidian.spleef.admin.edit")
    private void commandTool(final PlayerSource player) {

        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.tool = new ObsidianItemTemplate(player.source().getInventory().getItemInMainHand());
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "tool").component());
    }


    @Command("spleef admin teleport lobby")
    @Permission("obsidian.spleef.admin.teleport")
    private void commandTeleportLobby(final PlayerSource player) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
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

    @Command("spleef admin teleport schematic")
    @Permission("obsidian.spleef.admin.teleport")
    private void commandTeleportPlot(final PlayerSource player) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        if (wip.getPlotSchematicCenter() == null) {
            player.source()
                    .sendMessage(LanguageConfig.getInstance().editor().arenaHasNoSchematic(wip.getName()).component());
            return;
        }
        player.source().teleportAsync(wip.getPlotSchematicCenter().toBukkit()).thenAccept(Void -> player.source()
                .sendMessage(LanguageConfig.getInstance().editor().teleportedSchematic(wip.getName()).component()));
    }

    @Command("spleef admin rename arena <new_arena_name>")
    @Permission("obsidian.spleef.admin.edit")
    private void commandRename(final PlayerSource player, final @Argument(value = "new_arena_name") String map) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        String oldName = wip.getName();
        wip.name = map;
        player.source().sendMessage(LanguageConfig.getInstance().editor().renamed(oldName, map).component());
    }

    @Command("spleef admin set schematicSize <plotHeight>")
    @Permission("obsidian.spleef.admin.edit")
    private void commandPlotSizet(final PlayerSource player,
            final @Argument(value = "plotSize") @Range(min = "1", max = "300") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotSize = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotSize").component());
    }


    @Command("spleef admin set schematicHeight <plotHeight>")
    @Permission("obsidian.spleef.admin.edit")
    private void commandPlotHeight(final PlayerSource player,
            final @Argument(value = "plotHeight") @Range(min = "1", max = "300") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.plotHeight = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "plotHeight").component());
    }

    @Command("spleef admin set lobbySize <lobbySize>")
    @Permission("obsidian.spleef.admin.edit")
    private void command_lobbySize(final PlayerSource player,
            final @Argument(value = "lobbySize") @Range(min = "1", max = "300") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbySize = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbySize").component());
    }

    @Command("spleef admin set lobbyHeight <lobbyHeight>")
    @Permission("obsidian.spleef.admin.edit")
    private void command_lobbyHeight(final PlayerSource player,
            final @Argument(value = "lobbyHeight") @Range(min = "1", max = "300") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyHeight = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyHeight").component());
    }

    @Command("spleef admin set duration lobby <lobbyDuration>")
    @Permission("obsidian.spleef.admin.edit")
    private void command_lobbyDuration(final PlayerSource player,
            final @Argument(value = "lobbyDuration") @Range(min = "1", max = "3600") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.lobbyDuration = value;
        player.source()
                .sendMessage(LanguageConfig.getInstance().editor().changed(wip.getName(), "lobbyDuration").component());
    }

    @Command("spleef admin set minimumPlayers <minimumPlayers>")
    @Permission("obsidian.spleef.admin.edit")
    private void command_minimumPlayers(final PlayerSource player,
            final @Argument(value = "minimumPlayers") @Range(min = "2", max = "100") int value) {
        SpleefArena wip = editMap.get(player.source().getUniqueId());
        if (wip == null) {
            player.source().sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        wip.minimumPlayers = value;
        player.source().sendMessage(
                LanguageConfig.getInstance().editor().changed(wip.getName(), "minimumPlayers").component());
    }
}
