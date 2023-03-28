package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;

public class ForceCommand extends CommandManager.Command {

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

    @CommandMethod("bb force start")
    @CommandPermission("obsidian.bb.force.start")
    private void commandAutojoin(final  Player player) {
        Arenas arenas = Service.get(Arenas.class);
        RunningArena arena= arenas.getArenaForPlayer(player);
        if(arena ==null)
        {
            player.sendMessage(LanguageConfig.getInstance().error().notPlaying().with(player).component());
        }
    }
}
