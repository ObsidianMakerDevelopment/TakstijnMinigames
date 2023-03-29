package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.service.Service;

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

    @CommandMethod("bb admin createarena <arena>")
    @CommandPermission("obsidian.bb.admin.create")
    private void commandCreate(final Player player, final @Argument(value="arena",suggestions="arenas") String map) {
        Arena arena = new Arena();
        arena.id = UUID.randomUUID();
        arena.name = map;
        editMap.put(player.getUniqueId(), arena);
        player.sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
    }

    @CommandMethod("bb admin edit arena <arena>")
    @CommandPermission("obsidian.bb.admin.edit")
    private void commandEdit(final Player player, final @Argument(value="arena",suggestions="arenas") String map) {
        Arenas arenas = Service.get(Arenas.class);
        Arena arena = arenas.byName(map);
        editMap.put(player.getUniqueId(), arena);
        if (arena != null)
            player.sendMessage(LanguageConfig.getInstance().editor().nowInEdition(map).component());
        else
            player.sendMessage(LanguageConfig.getInstance().error().nonExistingMap(map).component());
    }

    @CommandMethod("bb admin save")
    @CommandPermission("obsidian.bb.admin.save")
    private void commandSave(final Player player) {
        Arenas arenas = Service.get(Arenas.class);
        Arena wip = editMap.get(player.getUniqueId());
        if (wip == null) {
            player.sendMessage(LanguageConfig.getInstance().error().nothingToSave().component());
            return;
        }
        arenas.save(wip);
        player.sendMessage(LanguageConfig.getInstance().editor().saved(wip.name).component());
    }
}
