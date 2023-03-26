package com.moyskleytech.mc.obsidiancore.utils;

import com.google.common.io.ByteStreams;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.moyskleytech.mc.obsidiancore.ObsidianCore;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.QueryOptions;
import xyz.haoshoku.nick.api.NickAPI;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import ch.njol.skript.util.EnchantmentType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ObsidianUtil {

    public static class NickAPIWrapper {

        public static void refreshPlayer(Player playerid) {
            try {
                refreshPlayer_(playerid);
            } catch (Throwable t) {

            }
        }

        public static void resetSkin(Player playerid) {
            try {
                resetSkin_(playerid);
            } catch (Throwable t) {

            }
        }

        public static void setSkin(Player playerid, String nickname) {
            try {
                setSkin_(playerid, nickname);
            } catch (Throwable t) {

            }
        }

        public static void nick(Player playerid, String nickname) {
            try {
                nick_(playerid, nickname);
            } catch (Throwable t) {

            }
        }

        public static void resetNick(Player playerid) {
            try {
                resetNick_(playerid);
            } catch (Throwable t) {

            }
        }

        private static void refreshPlayer_(Player playerid) {
            NickAPI.refreshPlayer(playerid);
        }

        private static void resetSkin_(Player playerid) {
            NickAPI.resetSkin(playerid);
        }

        private static void setSkin_(Player playerid, String nickname) {
            NickAPI.setSkin(playerid, nickname);
        }

        private static void nick_(Player playerid, String nickname) {
            NickAPI.nick(playerid, nickname);
        }

        private static void resetNick_(Player playerid) {
            NickAPI.resetNick(playerid);
        }
    }

    public static int getAmountOfSpaceFor(Material m, Player player) {
        var oneStack = new ItemStack(m).getMaxStackSize();
        int space = 0;
        var inventoryContent = player.getInventory().getContents();
        for (int index = 0; index < 36; index++) {
            var itemStack = inventoryContent[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                space += oneStack;
            }
        }

        var inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            space += oneStack - stack.getValue().getAmount();
        }
        return space;
    }

    public static ItemStack getSpawner(String parseString) {
        // parseString = SHEEP_SPAWNER
        parseString = parseString.toUpperCase();
        while (parseString.contains("_SPAWNER")) {
            parseString = parseString.replaceAll("_SPAWNER", "");
        }
        EntityType entity = EntityType.valueOf(parseString);

        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.displayName(ObsidianUtil.component(entity.name() + " Spawner"));
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        creatureSpawner.setSpawnedType(entity);
        blockStateMeta.setBlockState(creatureSpawner);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static void addToInventory(Material m, int withdrawable, Player player) {
        var oneStack = new ItemStack(m).getMaxStackSize();
        var inventoryStock = player.getInventory().all(Material.AIR);

        inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            var space = oneStack - stack.getValue().getAmount();
            var amountToAdd = Math.min(space, withdrawable);
            stack.getValue().setAmount(stack.getValue().getAmount() + amountToAdd);
            withdrawable -= amountToAdd;
        }

        while (withdrawable > 0) {
            var emptySlot = player.getInventory().firstEmpty();
            if (emptySlot >= 0) {
                var amount = Math.min(oneStack, withdrawable);
                var stack_ = new ItemStack(m);
                stack_.setAmount(amount);
                player.getInventory().setItem(emptySlot, stack_);
                withdrawable -= amount;
            }
        }
    }

    public static List<Material> parseMaterialFromConfig(List<String> materialNames) {
        final var materialList = new ArrayList<Material>();
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = ObsidianUtil.material(material.toUpperCase().replace(" ", "_"));
                        materialList.add(mat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        return materialList;
    }

    public static void cancelTask(BukkitTask task) {
        if (task != null) {
            if (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())
                    || Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
                Logger.trace("cancelTask {}", task);
            }
        }
    }

    public static List<String> translateColors(List<String> toTranslate) {
        return toTranslate.stream().map(string -> ChatColor
                .translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static String translateColors(String toTranslate) {
        return ChatColor.translateAlternateColorCodes('&', toTranslate);
    }

    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    

    public static void reloadPlugin(@NonNull JavaPlugin plugin) {
        // PlayerWrapperService.getInstance().reload();
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        if (plugin == ObsidianCore.getPluginInstance()) {
            // SBAConfig.getInstance().forceReload();
            // LanguageService.getInstance().load(plugin);
        }
        Bukkit.getLogger().info("Plugin reloaded! Keep in mind that restarting the server is safer!");
    }

    public static String capitalizeFirstLetter(@NotNull String toCap) {
        return toCap.substring(0, 1).toUpperCase() + toCap.substring(1).toLowerCase();
    }

    private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections)
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();

        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public static int getAmountOfInInventory(Material material, Player player) {
        var inventoryStock = player.getInventory().all(material);
        int inInventory = 0;
        for (var stack : inventoryStock.entrySet()) {
            inInventory += stack.getValue().getAmount();
        }
        return inInventory;
    }

    public static void bungeeMove(Player p, String server) {
        var out = ByteStreams.newDataOutput();

        out.writeUTF("Connect");
        out.writeUTF(server);

        p.sendPluginMessage(ObsidianCore.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void removeFromInventory(Material m, int amount, @NotNull Player player) {
        var inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            var count = stack.getValue().getAmount();
            if (count > amount) {
                stack.getValue().setAmount(count - amount);
                break;
            } else {
                amount -= count;
                player.getInventory().setItem(stack.getKey(), new ItemStack(Material.AIR));
            }
        }
    }

    public static String getNickname(OfflinePlayer playerid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            var prefix = user.getCachedData().getMetaData().getMetaValue("nickname");
            if (prefix == null)
                return playerid.getName();
            prefix = IridiumColorAPI.process(prefix);

            return prefix;
        } catch (NullPointerException ise) {
            return playerid.getName();
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return playerid.getName();
        }
    }

    public static String getNickname(Player playerid) {
        return getNickname((OfflinePlayer) playerid);
    }

    public static String getNickname(CommandSender playerid) {
        if (playerid instanceof OfflinePlayer)
            return getNickname((OfflinePlayer) playerid);
        else
            return "CONSOLE";
    }

    public static String setNickname(Player playerid, String nickname) {
        try {

            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            user.data().clear(n -> n.getKey().startsWith("meta.nickname."));
            api.getUserManager().saveUser(user);
            if (nickname != null) {
                NickAPIWrapper.nick(playerid, nickname);
                user.data().add(MetaNode.builder("nickname", nickname).build());
            } else {
                NickAPIWrapper.resetNick(playerid);

                nickname = playerid.getName();
            }
            NickAPIWrapper.refreshPlayer(playerid);
            api.getUserManager().saveUser(user);
            return nickname;
        } catch (NullPointerException ise) {
            return "";
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return "";
        }
    }

    public static String getSkin(Player playerid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            var prefix = user.getCachedData().getMetaData().getMetaValue("skin");
            if (prefix == null)
                return playerid.getName();
            prefix = IridiumColorAPI.process(prefix);

            return prefix;
        } catch (NullPointerException ise) {
            return playerid.getName();
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return playerid.getName();
        }
    }

    public static String setSkin(Player playerid, String nickname) {
        try {

            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());

            user.data().clear(n -> n.getKey().startsWith("meta.skin."));
            api.getUserManager().saveUser(user);
            if (nickname != null) {
                NickAPIWrapper.setSkin(playerid, nickname);
                user.data().add(MetaNode.builder("skin", nickname).build());
            } else {
                NickAPIWrapper.resetSkin(playerid);

                nickname = playerid.getName();
            }
            NickAPIWrapper.refreshPlayer(playerid);
            api.getUserManager().saveUser(user);
            return nickname;
        } catch (NullPointerException ise) {
            return "";
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return "";
        }
    }

    public static String getPrefix(Player playerid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            user.getCachedData().invalidate();
            var prefix = user.getCachedData().getMetaData().getPrefix();

            prefix = IridiumColorAPI.process(prefix);

            return prefix;
        } catch (NullPointerException ise) {
            return "";
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return "";
        }
    }

    public static boolean hasGroup(Player player, String group) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(player.getUniqueId());
            for (var g : user.getInheritedGroups(QueryOptions.nonContextual())) {
                if (g.getIdentifier().getName().equals(group))
                    return true;
            }
        } catch (NullPointerException ise) {
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
        }
        return false;
    }

    public static void setGroup(Player playerId, String group) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerId.getUniqueId());
            user.setPrimaryGroup(group);
            api.getUserManager().saveUser(user);
        } catch (NullPointerException ise) {
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
        }
    }

    public static List<String> getGroupsWithPrefix(Player playerid) {
        try {
            Map<String, Component> prefixes = getPrefixes(playerid);

            List<String> groupsTxt = new ArrayList<>();
            for (var group : prefixes.keySet()) {
                groupsTxt.add(group);
            }
            return groupsTxt;
        } catch (Throwable ise) {
            ise.printStackTrace();
            return List.of();
        }
    }

    public static Component getGroupPrefix(String group) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var groupObject = api.getGroupManager().getGroup(group);

            return component(groupObject.getCachedData().getMetaData().getPrefix());

        } catch (Throwable ise) {
            ise.printStackTrace();
            return Component.empty();
        }
    }

    public static Map<String, Component> getPrefixes(Player playerid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            var groups = user.getInheritedGroups(QueryOptions.nonContextual());

            Map<String, Component> groupsTxt = new HashMap<>();
            for (var group : groups) {
                try {
                    groupsTxt.put(group.getName(), component(
                            group.getCachedData().getMetaData().getPrefix()));
                } catch (Throwable ise) {

                }
            }

            return groupsTxt;
        } catch (Throwable ise) {
            ise.printStackTrace();
            return Map.of();
        }
    }

    public static String getSuffix(Player playerid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            var user = api.getUserManager().getUser(playerid.getUniqueId());
            var prefix = user.getCachedData().getMetaData().getSuffix();

            prefix = IridiumColorAPI.process(prefix);

            return prefix;
        } catch (NullPointerException ise) {
            return "";
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
            return "";
        }
    }

    public static Component component(Component text) {
        return component(text, null);
    }

    public static Component component(Component text, Player p) {
        return component(LegacyComponentSerializer.legacySection().serialize(text), p);
    }

    public static Component component(String text, Player p) {
        if (ObsidianCore.getInstance().getExp() != null) {
            text = ObsidianCore.getInstance().getExp().process(text, p);
        }
        return LegacyComponentSerializer.legacySection().deserialize(IridiumColorAPI.process(text));
    }

    public static Component component(String text) {
        return component(text, null);
    }

    public static void adminSend(CommandSender sender, Component text) {
        sender.sendMessage(text);
        var adminMessage = Component.text("[" + getNickname(sender) + ":").color(NamedTextColor.GRAY).append(text)
                .append(Component.text("]").color(NamedTextColor.GRAY));

        for (var player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(sender))
                if (player.hasPermission("minecraft.admin.command_feedback")) {

                    player.sendMessage(adminMessage);
                }
        }
    }

    public static void adminSend(Player sender, Component text, Component adminMessage) {
        sender.sendMessage(text);

        for (var player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(sender))
                if (player.hasPermission("minecraft.admin.command_feedback")) {

                    player.sendMessage(adminMessage);
                }
        }
    }

    public static void playSound(@NotNull Player player, String string) {
        net.kyori.adventure.sound.Sound pling = net.kyori.adventure.sound.Sound.sound(Key.key(string),
                net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f);
        player.playSound(pling);
    }

    public static Enchantment enchantment(String s) {
        for (var enchant : Enchantment.values()) {
            if (enchant.getKey().value().equals(s))
                return enchant;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            return SkriptEnchantmentType.parseEnchantment(s);
        }
        return null;
    }

    private class SkriptEnchantmentType {
        public static Enchantment parseEnchantment(String s) {
            return EnchantmentType.parseEnchantment(s);
        }

        public static String toString(Enchantment e) {
            return EnchantmentType.toString(e);
        }
    }

    public static String name(Enchantment e) {
        if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            return SkriptEnchantmentType.toString(e);
        }
        return e.toString();
    }

    public static ItemStack skItem(String s) {
        if (s == null)
            return null;
        if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            String name = null;
            if (s.contains("named")) {
                String[] parts = s.split(" named ");
                s = parts[0];
                name = parts[1].trim();
            }
            var itemStack = SkriptAliases.parseItemType(s);
            if (name != null)
                itemStack.setDisplayName(name);
            return itemStack;
        }
        return null;
    }

    private class SkriptAliases {
        public static ItemStack parseItemType(String s) {
            var itemType = ch.njol.skript.aliases.Aliases.parseItemType(s);
            if (itemType == null)
                return null;
            return itemType.getRandom();
        }
    }

    public static Material material(String material) {
        Material m = Material.matchMaterial(material);
        if (m != null) {
            return m;
        } else {
            var stack = skItem(material);
            if (stack != null)
                stack.getType();
        }
        return null;
    }

    public static ItemStack item(String materialName) {
        int materialAmount = 1;
        String name = null;
        if (materialName.contains("named")) {
            String[] parts = materialName.split(" named ");
            materialName = parts[0];
            name = parts[1].trim();
        }

        if (materialName.contains(" ")) {
            String[] spl = materialName.split(" ");
            if (spl[0].matches("[0-9]+[\\.]?[0-9]*")) {
                materialAmount = Integer.parseInt(spl[0]);
                materialName = materialName.substring(materialName.indexOf(" ") + 1);
            }
        }
        Logger.trace("Parsing material '{}' '{}'", materialAmount, materialName);
        Material m = Material.matchMaterial(materialName);
        ItemStack stack = null;
        if (m != null) {
            stack = new ItemStack(m);
        } else {
            stack = skItem(materialName);
        }
        if (stack == null)
            return null;
        stack.setAmount(materialAmount);
        if (name != null)
            stack.setDisplayName(name);
        return stack;
    }

    public static List<Player> retrievePlayers(String s,
            @NonNull CommandSender sender) {
        if (sender instanceof Player) {
            if (List.of("@s", "@p").contains(s))
                return List.of((Player) sender);
        }
        if ("@r".contains(s)) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(players);
            return List.of(players.get(0));
        }
        return retrievePlayers(s);
    }

    public static OfflinePlayer retrievePlayerOffline(String s,
            @NonNull CommandSender sender) {
        if (sender instanceof Player) {
            if (List.of("@s", "@p").contains(s))
                return (Player) sender;
        }
        if ("@r".contains(s)) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(players);
            return players.get(0);
        }
        return retrievePlayerOffline(s);
    }

    public static Player retrievePlayer(String s,
            @NonNull CommandSender sender) {
        if (sender instanceof Player) {
            if (List.of("@s", "@p").contains(s))
                return (Player) sender;
        }
        if ("@r".contains(s)) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(players);
            return players.get(0);
        }
        return retrievePlayer(s);
    }

    public static List<Player> retrievePlayers(String s) {
        if (s.equals("@a"))
            return Bukkit.getOnlinePlayers().stream().collect(Collectors.toList());
        ArrayList<Player> lst = new ArrayList<>();
        for (var player : Bukkit.getOnlinePlayers()) {
            if (getNickname(player).equals(s))
                lst.add(player);
        }
        return lst;
    }

    public static List<OfflinePlayer> retrievePlayersOffline(String s) {
        if (s.equals("@a"))
            return Arrays.asList(Bukkit.getOfflinePlayers());
        ArrayList<OfflinePlayer> lst = new ArrayList<>();
        for (var player : Bukkit.getOfflinePlayers()) {
            if (getNickname(player).equals(s))
                lst.add(player);
        }
        return lst;
    }

    public static Player retrievePlayer(String s) {
        for (var player : Bukkit.getOnlinePlayers()) {
            if (getNickname(player).equals(s))
                return player;
        }
        return null;
    }

    public static OfflinePlayer retrievePlayerOffline(String s) {
        for (var player : Bukkit.getOfflinePlayers()) {
            if (getNickname(player).equals(s))
                return player;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static String name(ItemStack s) {
        if (s == null)
            return "<EMPTY>";
        return s.getAmount() + " " + s.getI18NDisplayName();
    }
    @SuppressWarnings("deprecation")
    public static String name(Material s) {
        return new ItemStack(s).getI18NDisplayName();
    }

    public static boolean canEnchantItem(Enchantment enchant, @NotNull ItemStack item) {
        if (item.getType() == Material.BOOK)
            return true;
        if (item.getType() == Material.FISHING_ROD && enchant.getKey().toString().equals("minecraft:power"))
            return true;
        return enchant.canEnchantItem(item);
    }

    public static @NotNull String name(Location target) {
        return "X:" + target.getX() + ",Y:" + target.getY() + ",Z:" + target.getZ();
    }

    public static String name(GameMode gm) {
        switch (gm) {
            case ADVENTURE:
                return "adventure";
            case CREATIVE:
                return "creative";
            case SPECTATOR:
                return "spectator";
            case SURVIVAL:
                return "survival";
            default:
                return gm.toString();
        }
    }
    @SuppressWarnings("removal")
    public static PluginCommand registerCommand(String cmd, String permission) {

        try {
            Logger.info("Registering command /{} with permission {}", cmd, permission);
            final Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            final PluginCommand bukkitCommand = c.newInstance(cmd, ObsidianCore.getPluginInstance());
            bukkitCommand.setAliases(List.of());
            bukkitCommand.setDescription("");
            bukkitCommand.setLabel(cmd);
            bukkitCommand.setPermission(permission);
            // We can only set the message if it's simple (doesn't contains expressions)
            // if (permissionMessage.isSimple())
            // bukkitCommand.setPermissionMessage(permissionMessage.toString(null));
            bukkitCommand.setUsage(cmd);

            Field commandMapField;
            try {
                commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                var commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
                commandMap.register("obsidiancore", bukkitCommand);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return bukkitCommand;
        } catch (final Exception e) {
            return null;
        }
    }

    public static Object unregisterCommand(PluginCommand c) {
        return null;
    }
}
