package com.moyskleytech.mc.BuildBattle.commands;

import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.meta.CommandMeta;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.SpleefArenas;
import com.moyskleytech.mc.BuildBattle.game.PillarArenas;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.suggestion.Suggestion;

@Getter
public class CommandManager extends Service {

    public static class Command extends Service {

    }

    public static CommandManager getInstance() {
        return Service.get(CommandManager.class);
    }

    public PaperCommandManager<Source> getManager() {
        return manager;
    }

    private List<Command> commands;
    private PaperCommandManager<Source> manager;

    private AnnotationParser<Source> annotationParser;

    public CommandManager() {
        super();
        commands = new ArrayList<>();

        commands.add(new JoinCommand());
        commands.add(new PillarJoinCommand());
        commands.add(new SpleefJoinCommand());
        commands.add(new ForceCommand());
        commands.add(new TestCommand());
        commands.add(new ReloadCommand());
        commands.add(new AdminCommand());
        commands.add(new SpleefAdminCommand());
        commands.add(new PillarAdminCommand());

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

        try {
            this.manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(plugin);
        } catch (final Throwable e) {
            Bukkit.getLogger().severe("Failed to initialize the command manager");
            throw new ServiceLoadException(e);
        }

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                // manager.registerBrigadier();
            } catch (Throwable e) {
                Logger.error("Could not register Brigadier :: \r{}", e.getCause().getCause());
            }
        }

        if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {

        }
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.builder()
                // .with(CloudKey., p.get(StandardParameters.DESCRIPTION, "No description"))
                .build();

        annotationParser = new AnnotationParser<Source>(
                manager,
                Source.class,
                commandMetaFunction);
        annotationParser.parse(this);

        Arenas arenas = Service.get(Arenas.class);
        manager.parserRegistry().registerSuggestionProvider("arenas",

                (commandSenderCommandContext, s) -> {
                    return CompletableFuture
                            .completedFuture(arenas.names().stream().map(name -> Suggestion.suggestion(name)).toList());
                });
        SpleefArenas spleefArenas = Service.get(SpleefArenas.class);
        manager.parserRegistry().registerSuggestionProvider("spleefarenas",
                (commandSenderCommandContext, s) -> {
                    return CompletableFuture.completedFuture(
                            spleefArenas.names().stream().map(name -> Suggestion.suggestion(name)).toList());
                });
        PillarArenas pillArenas = Service.get(PillarArenas.class);
        manager.parserRegistry().registerSuggestionProvider("Pillararenas",
                (commandSenderCommandContext, s) -> {
                    return CompletableFuture.completedFuture(
                        pillArenas.names().stream().map(name -> Suggestion.suggestion(name)).toList());
                });
        commands.forEach(arg0 -> {
            try {
                arg0.onLoad();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}