package com.moyskleytech.mc.BuildBattle.utils;

import com.google.common.io.ByteStreams;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.game.LocationDB;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ObsidianUtil {

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

    public static List<ObsidianMaterial> parseMaterialFromConfig(List<String> materialNames) {
        final var materialList = new ArrayList<ObsidianMaterial>();
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = ObsidianMaterial.valueOf(material);
                        materialList.add(mat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        return materialList;
    }

    public static void cancelTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
            Logger.trace("cancelTask {}", task);
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

    public static void reloadPlugin(JavaPlugin plugin) {
        // PlayerWrapperService.getInstance().reload();
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        if (plugin == BuildBattle.getPluginInstance()) {
            // SBAConfig.getInstance().forceReload();
            // LanguageService.getInstance().load(plugin);
        }
        Bukkit.getLogger().info("Plugin reloaded! Keep in mind that restarting the server is safer!");
    }

    public static String capitalizeFirstLetter(String toCap) {
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

        p.sendPluginMessage(BuildBattle.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void removeFromInventory(Material m, int amount, Player player) {
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

    public static Component component(Component text) {
        return text;
    }

    public static Component component(Component text, Player p) {
        return text;
    }

    public static Component component(@NotNull String text, Player p) {
        return LegacyComponentSerializer.legacySection().deserialize(IridiumColorAPI.process(text));
    }

    public static Component component(String text) {
        return component(text, null);
    }

    public static void playSound(Player player, String string) {
        net.kyori.adventure.sound.Sound pling = net.kyori.adventure.sound.Sound.sound(Key.key(string),
                net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f);
        player.playSound(pling);
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

    public static boolean canEnchantItem(Enchantment enchant, ItemStack item) {
        if (item.getType() == Material.BOOK)
            return true;
        if (item.getType() == Material.FISHING_ROD && enchant.getKey().toString().equals("minecraft:power"))
            return true;
        return enchant.canEnchantItem(item);
    }

    public static String name(Location target) {
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

    public static File mainLobbyFile() {
        File folder = BuildBattle.getPluginInstance().getDataFolder();
        File value = new File(folder, "mainLobby.yml");
        return value;
    }

    public static @NotNull Location getMainLobby() {
        Data data = Data.getInstance();
        File mainLobby = mainLobbyFile();
        if (mainLobby.exists())
            return data.load(LocationDB.class, mainLobby).toBukkit();
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public static void setMainLobby(Location loc) {
        Data data = Data.getInstance();
        if (loc == null)
            mainLobbyFile().delete();
        else
            data.save(LocationDB.fromBukkit(loc), mainLobbyFile());
    }

    public static <T> CompletableFuture<Void> future(Collection<CompletableFuture<T>> teleports) {
        if (teleports.size() == 0)
            return CompletableFuture.completedFuture(null);
        else {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            int size = teleports.size();
            AtomicInteger toComplete = new AtomicInteger(size);
            teleports.forEach(
                    teleport -> teleport.thenAccept(teleportValue -> {
                        if (toComplete.decrementAndGet() == 0)
                            completableFuture.complete(null);
                    }));
            return completableFuture;
        }
    }
}
