package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;

public class SudoCommand extends CommandManager.Command {

    static boolean init = false;

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        super.onLoad();
    }
    public void onPostEnabled() {
        if (init)
            return;
        if (!ObsidianConfig.getInstance().essentials().sudo()) {
            return;
        }
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("make <player> <command>")
    @CommandPermission("obsidian.make")
    private void commandMake(final  CommandSender player,
            final @Argument(value = "player", parserName = "player_parser") Player target,
            final @Greedy @Argument("command") String command) {
        target.performCommand(command);
    }

    @CommandMethod("make everyone <command>")
    @CommandPermission("obsidian.make")
    private void commandMakeEveryone(final  CommandSender player, final @Greedy @Argument("command") String command) {
        for (Player target : Bukkit.getServer().getOnlinePlayers()) {
            target.performCommand(command);
        }
    }

    @CommandMethod("sudo <command>")
    @CommandPermission("obsidian.sudo")
    private void commandSudo(final  CommandSender player, final @Greedy @Argument("command") String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }
}
