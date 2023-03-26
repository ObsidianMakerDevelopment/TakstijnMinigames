package com.moyskleytech.mc.obsidiancore.commands;

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

import com.moyskleytech.mc.obsidiancore.ObsidianCore;
import com.moyskleytech.mc.obsidiancore.service.Service;
import com.moyskleytech.mc.obsidiancore.utils.Logger;
import com.moyskleytech.mc.obsidiancore.utils.ObsidianUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

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
       
        commands.add(new BanCommand());
        commands.add(new BanIPCommand());
        commands.add(new BankingCommand());
        commands.add(new ClaimCommand());
        commands.add(new CLICommand());
        commands.add(new CraftCommand());
        commands.add(new GameModeCommand());
        commands.add(new GUICommand());
        commands.add(new HubCommand());
        commands.add(new IpCommand());
        commands.add(new ReloadCommand());
        commands.add(new KickCommand());
        commands.add(new MangroveCommand());
        commands.add(new MusicCommand());
        commands.add(new NicknameCommand());
        commands.add(new OfflineTeleportCommand());
        commands.add(new RankChooserCommand());
        commands.add(new RankChooserCommand());
        commands.add(new SetSpawnCommand());
        commands.add(new SkEnchant());
        commands.add(new SkItemCommand());
        commands.add(new SpawnerCommand());
        commands.add(new SudoCommand());
        commands.add(new TeleportCommand());
        commands.add(new TpaCommand());
        commands.add(new UnbanCommand());

        commands.forEach(arg0 -> {
            try {
                arg0.onPreload();
            } catch (ServiceLoadException e) {
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
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        JavaPlugin plugin = ObsidianCore.getInstance();
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
            } catch (Exception e) {
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

        manager.getParserRegistry().registerSuggestionProvider("player",
                (commandSenderCommandContext, s) -> {
                    ArrayList<String> suggestions = new ArrayList<>();
                    for (var player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(ObsidianUtil.getNickname(player));
                    }
                    return suggestions;
                });
        manager.getParserRegistry().registerSuggestionProvider("players",
                (commandSenderCommandContext, s) -> {
                    ArrayList<String> suggestions = new ArrayList<>();
                    for (var player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(ObsidianUtil.getNickname(player));
                    }
                    suggestions.add("@a");
                    suggestions.add("@p");
                    suggestions.add("@r");
                    suggestions.add("@s");
                    return suggestions;
                });

        manager.getParserRegistry().registerNamedParserSupplier("player_parser",
                (p) -> {
                    return new ArgumentParser<CommandSender, Player>() {

                        @Override
                        public @NonNull ArgumentParseResult<@NonNull Player> parse(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull Queue<@NonNull String> inputQueue) {

                            String s = inputQueue.remove();
                            Player p = ObsidianUtil.retrievePlayer(s, commandContext.getSender());
                            if (p != null)
                                return ArgumentParseResult.success(p);
                            else
                                return ArgumentParseResult.failure(new Throwable("player not found"));
                        }

                        @Override
                        public @NonNull List<String> suggestions(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull String inputQueue) {
                            ArrayList<String> suggestions = new ArrayList<>();
                            for (var player : Bukkit.getOnlinePlayers()) {

                                suggestions.add(ObsidianUtil.getNickname(player));
                            }
                            suggestions.add("@p");
                            suggestions.add("@r");
                            suggestions.add("@s");
                            return suggestions;
                        }

                    };
                });
        manager.getParserRegistry().registerNamedParserSupplier("offline_parser",
                (p) -> {
                    return new ArgumentParser<CommandSender, OfflinePlayer>() {

                        @Override
                        public @NonNull ArgumentParseResult<@NonNull OfflinePlayer> parse(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull Queue<@NonNull String> inputQueue) {

                            String s = inputQueue.remove();
                            OfflinePlayer p = ObsidianUtil.retrievePlayerOffline(s, commandContext.getSender());

                            if (p != null)
                                return ArgumentParseResult.success(p);
                            else
                                return ArgumentParseResult.failure(new Throwable("player not found"));
                        }

                        @Override
                        public @NonNull List<String> suggestions(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull String inputQueue) {
                            ArrayList<String> suggestions = new ArrayList<>();
                            for (var player : Bukkit.getOfflinePlayers()) {

                                suggestions.add(ObsidianUtil.getNickname(player));
                            }
                            suggestions.add("@p");
                            suggestions.add("@r");
                            suggestions.add("@s");
                            return suggestions;
                        }

                    };
                });
        manager.getParserRegistry().registerNamedParserSupplier("players_parser",
                (p) -> {
                    return new ArgumentParser<CommandSender, List<Player>>() {

                        @Override
                        public @NonNull ArgumentParseResult<@NonNull List<Player>> parse(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull Queue<@NonNull String> inputQueue) {

                            String s = inputQueue.remove();
                            var p = ObsidianUtil.retrievePlayers(s, commandContext.getSender());

                            return ArgumentParseResult.success(p);
                        }

                        @Override
                        public @NonNull List<String> suggestions(
                                @NonNull CommandContext<@NonNull CommandSender> commandContext,
                                @NonNull String inputQueue) {
                            ArrayList<String> suggestions = new ArrayList<>();
                            for (var player : Bukkit.getOnlinePlayers()) {

                                suggestions.add(ObsidianUtil.getNickname(player));
                            }
                            suggestions.add("@a");
                            suggestions.add("@p");
                            suggestions.add("@r");
                            suggestions.add("@s");
                            return suggestions;
                        }
                    };
                });
        commands.forEach(arg0 -> {
            try {
                arg0.onLoad();
            } catch (ServiceLoadException e) {
                e.printStackTrace();
            }
        });
    }

    @CommandMethod("obsidian help [query]")
    @CommandDescription("Help menu")
    private void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query) {
        sender.sendMessage("commandHelp");
        minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }

}