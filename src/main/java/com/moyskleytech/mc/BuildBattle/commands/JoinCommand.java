package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.service.Service;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;

public class JoinCommand extends CommandManager.Command {

    static boolean init = false;

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        super.onLoad();
    }
    public void onPostEnabled() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("bb autojoin")
    @CommandPermission("obsidian.bb.autojoin")
    private void commandAutojoin(final  Player player) {
        Arenas arenas = Service.get(Arenas.class);
        arenas.joinRandomly(player);
    }

    @CommandMethod("bb join <arena>")
    @CommandPermission("obsidian.bb.join")
    private void commandJoin(final  Player player, final @Argument("arena") String map) {
        Arenas arenas = Service.get(Arenas.class);
        arenas.join(player, map);
    }

    @CommandMethod("bb joinnew <arena>")
    @CommandPermission("obsidian.bb.joinnew")
    private void commandJoinNew(final  Player player, final @Argument("arena") String map) {
        Arenas arenas = Service.get(Arenas.class);
        arenas.join(player, map,false);
    }
}
