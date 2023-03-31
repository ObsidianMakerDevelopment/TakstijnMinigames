package com.moyskleytech.mc.BuildBattle.commands;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import lombok.Getter;
import lombok.NonNull;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import com.moyskleytech.mc.BuildBattle.game.Arenas;

@Getter
public class CommandManager extends Service {

    public static class Command extends Service {

    }

    public static CommandManager getInstance() {
        return Service.get(CommandManager.class);
    }

    public PaperCommandManager<CommandSender> getManager() {
        return manager;
    }

    private List<Command> commands;
    private PaperCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;

    public CommandManager() {
        super();
        commands = new ArrayList<>();
       
        commands.add(new JoinCommand());
        commands.add(new ForceCommand());
        commands.add(new TestCommand());
        commands.add(new ReloadCommand());
        commands.add(new AdminCommand());

        commands.forEach(arg0 -> {
            try {
                arg0.onPreload();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onUnload() {
        commands.forEach(Service::onUnload);
        super.onUnload();
    }

    @Override
    public void onReload() throws ServiceLoadException {
        super.onReload();
        commands.forEach(arg0 -> {
            try {
                arg0.onReload();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        JavaPlugin plugin = BuildBattle.getInstance();
        if (manager != null)
            return;
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator
                .simpleCoordinator();
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    plugin,
                    executionCoordinatorFunction,
                    mapperFunction,
                    mapperFunction);
        } catch (final Throwable e) {
            Bukkit.getLogger().severe("Failed to initialize the command manager");
            throw new ServiceLoadException(e);
        }

        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            try {
                manager.registerBrigadier();
            } catch (Throwable e) {
                Logger.error("Could not register Brigadier :: \r{}", e.getCause().getCause());
            }
        }

        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple()
                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                .build();

        annotationParser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                commandMetaFunction);
        annotationParser.parse(this);

        Arenas arenas = Service.get(Arenas.class);
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("arenas",
                (commandSenderCommandContext, s) -> {
                    return arenas.names();
                });

        commands.forEach(arg0 -> {
            try {
                arg0.onLoad();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}