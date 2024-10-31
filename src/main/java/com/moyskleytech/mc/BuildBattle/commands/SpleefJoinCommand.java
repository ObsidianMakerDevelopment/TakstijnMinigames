package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.ActionResult;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.game.SpleefArenas;
import com.moyskleytech.mc.BuildBattle.game.SpleefRunningArena;
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

public class SpleefJoinCommand extends CommandManager.Command {

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

    @CommandMethod("spleef autojoin")
    @CommandPermission("obsidian.spleef.autojoin")
    private void commandAutojoin(final Player player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        ActionResult result = arenas.joinRandomly(player);
        if (!result.isSuccess()) {
            player.sendMessage(LanguageConfig.getInstance().getString(result.getErrorKey()));
        }
    }

    @CommandMethod("spleef leave")
    private void commandLeave(final Player player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefRunningArena arena = arenas.getArenaForPlayer(player);
        if (arena == null)
            player.sendMessage(LanguageConfig.getInstance().error().notPlaying().with(player).component());
        else
            arena.leave(player);
    }

    @CommandMethod("spleef join <arena>")
    @CommandPermission("obsidian.spleef.join")
    private void commandJoin(final Player player, final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.join(player, map);
    }

    @CommandMethod("spleef joinnew <arena>")
    @CommandPermission("obsidian.spleef.joinnew")
    private void commandJoinNew(final Player player,
            final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.join(player, map, false);
    }

}
