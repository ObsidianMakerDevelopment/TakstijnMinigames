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

import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.PlayerSource;


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

    @Command("spleef autojoin")
    @Permission("obsidian.spleef.autojoin")
    private void commandAutojoin(final PlayerSource player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        ActionResult result = arenas.joinRandomly(player.source());
        if (!result.isSuccess()) {
            player.source().sendMessage(LanguageConfig.getInstance().getString(result.getErrorKey()));
        }
    }

    @Command("spleef leave")
    private void commandLeave(final PlayerSource player) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        SpleefRunningArena arena = arenas.getArenaForPlayer(player.source());
        if (arena == null)
            player.source().sendMessage(LanguageConfig.getInstance().error().notPlaying().with(player.source()).component());
        else
            arena.leave(player.source());
    }

    @Command("spleef join <arena>")
    @Permission("obsidian.spleef.join")
    private void commandJoin(final PlayerSource player, final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.join(player.source(), map);
    }

    @Command("spleef joinnew <arena>")
    @Permission("obsidian.spleef.joinnew")
    private void commandJoinNew(final PlayerSource player,
            final @Argument(value = "arena", suggestions = "spleefarenas") String map) {
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.join(player.source(), map, false);
    }

}
