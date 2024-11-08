package com.moyskleytech.mc.BuildBattle.utils;

import com.google.common.collect.Lists;
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
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;

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

    public static File mainSpleefLobbyFile() {
        File folder = BuildBattle.getPluginInstance().getDataFolder();
        File value = new File(folder, "mainSpleefLobby.yml");
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

    public static @NotNull Location getSpleefMainLobby() {
        Data data = Data.getInstance();
        File mainLobby = mainSpleefLobbyFile();
        if (mainLobby.exists())
            return data.load(LocationDB.class, mainLobby).toBukkit();
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public static void setSpleefMainLobby(Location loc) {
        Data data = Data.getInstance();
        if (loc == null)
            mainSpleefLobbyFile().delete();
        else
            data.save(LocationDB.fromBukkit(loc), mainSpleefLobbyFile());
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

    public static List<VillagerTrade> getAllVillagersRecipes() {
        List<VillagerTrade> Traders = new ArrayList<>();
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.COAL, 15),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.COAL, 20),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.EMERALD, 5),
                new ItemStack(Material.IRON_HELMET, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.EMERALD, 9),
                new ItemStack(Material.IRON_CHESTPLATE, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.EMERALD, 7),
                new ItemStack(Material.IRON_LEGGINGS, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Novice, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.IRON_BOOTS, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Apprentice, new ItemStack(Material.IRON_INGOT, 4 / 3),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Apprentice, new ItemStack(Material.EMERALD, 36),
                new ItemStack(Material.BELL, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Apprentice, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.CHAINMAIL_LEGGINGS, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.CHAINMAIL_BOOTS, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Journeyman, new ItemStack(Material.LAVA_BUCKET, 1),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Journeyman, new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.CHAINMAIL_HELMET, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Journeyman, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1)));
        Traders.add(new VillagerTrade(Profession.ARMORER, Level.Journeyman, new ItemStack(Material.EMERALD, 5),
                new ItemStack(Material.SHIELD, 1)));
        // Traders.add(new VillagerTrade(Profession.ARMORER, Level.Expert, new
        // ItemStack(Material.EMERALD, 33),
        // new ItemStack(Material.ENCHANTED_DIAMOND_LEGGINGS, 1)));
        // Traders.add(new VillagerTrade(Profession.ARMORER, Level.Expert, new
        // ItemStack(Material.EMERALD, 27),
        // new ItemStack(Material.ENCHANTED_DIAMOND_BOOTS, 1)));
        // Traders.add(new VillagerTrade(Profession.ARMORER, Level.Master, new
        // ItemStack(Material.EMERALD, 27),
        // new ItemStack(Material.ENCHANTED_DIAMOND_HELMET, 1)));
        // Traders.add(new VillagerTrade(Profession.ARMORER, Level.Master, new
        // ItemStack(Material.EMERALD, 35),
        // new ItemStack(Material.ENCHANTED_DIAMOND_CHESTPLATE, 1)));

        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Novice, new ItemStack(Material.CHICKEN, 14),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Novice, new ItemStack(Material.PORKCHOP, 7),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Novice, new ItemStack(Material.RABBIT, 6),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.RABBIT_STEW, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Apprentice, new ItemStack(Material.COAL, 16),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.COOKED_PORKCHOP, 5)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.COOKED_CHICKEN, 8)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Journeyman, new ItemStack(Material.MUTTON, 7),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Journeyman, new ItemStack(Material.BEEF, 10),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Expert, new ItemStack(Material.DRIED_KELP_BLOCK, 10),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.BUTCHER, Level.Master, new ItemStack(Material.SWEET_BERRIES, 10),
                new ItemStack(Material.EMERALD, 1)));

        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Novice, new ItemStack(Material.PAPER, 24),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Novice, new ItemStack(Material.EMERALD, 7),
                new ItemStack(Material.MAP, 1)));
        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Apprentice,
                new ItemStack(Material.GLASS_PANE, 11), new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Apprentice, new
        // ItemStack(Material.EMERALD_AND_COMPASS,1),new
        // ItemStack(Material.OCEAN_EXPLORER_MAP,1)));
        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Journeyman, new ItemStack(Material.COMPASS, 1),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Journeyman, new
        // ItemStack(Material.EMERALD_AND_COMPASS,14 and 1),new
        // ItemStack(Material.WOODLAND_EXPLORER_MAP,1)));
        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Expert, new ItemStack(Material.EMERALD, 7),
                new ItemStack(Material.ITEM_FRAME, 1)));

        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("_banner")
                        && !x.name().toLowerCase().contains("wall_banner"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Expert, new ItemStack(Material.EMERALD, 3),
                    new ItemStack(bannerMaterial, 1)));
        }

        Traders.add(new VillagerTrade(Profession.CARTOGRAPHER, Level.Master, new ItemStack(Material.EMERALD, 8),
                new ItemStack(Material.GLOBE_BANNER_PATTERN, 1)));

        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Novice, new ItemStack(Material.ROTTEN_FLESH, 32),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.REDSTONE, 2)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Apprentice, new ItemStack(Material.GOLD_INGOT, 3),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.LAPIS_LAZULI, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Journeyman, new ItemStack(Material.RABBIT_FOOT, 2),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Journeyman, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.GLOWSTONE, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Expert, new ItemStack(Material.TURTLE_SCUTE, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Expert, new ItemStack(Material.ARMADILLO_SCUTE, 4),
                new ItemStack(Material.EMERALD, 1)));

        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Expert, new ItemStack(Material.GLASS_BOTTLE, 9),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Expert, new ItemStack(Material.EMERALD, 5),
                new ItemStack(Material.ENDER_PEARL, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Master, new ItemStack(Material.NETHER_WART, 22),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.CLERIC, Level.Master, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.EXPERIENCE_BOTTLE, 1)));

        Traders.add(new VillagerTrade(Profession.FARMER, Level.Novice, new ItemStack(Material.WHEAT, 20),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Novice, new ItemStack(Material.POTATO, 26),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Novice, new ItemStack(Material.CARROT, 22),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Novice, new ItemStack(Material.BEETROOT, 15),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.BREAD, 6)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Apprentice, new ItemStack(Material.PUMPKIN, 6),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.PUMPKIN_PIE, 4)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.APPLE, 4)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Journeyman, new ItemStack(Material.MELON, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Journeyman, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.COOKIE, 18)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Expert, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.CAKE, 1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(NIGHT_VISION),1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(POISON),1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(JUMP_BOOST),1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(WEAKNESS),1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(BLINDNESS),1)));
        // Traders.add(new VillagerTrade(Profession.FARMER,Level.Expert, new
        // ItemStack(Material.EMERALD,1),new
        // ItemStack(Material.SUSPICIOUS_STEW_(SATURATION),1)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Master, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.GOLDEN_CARROT, 3)));
        Traders.add(new VillagerTrade(Profession.FARMER, Level.Master, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.GLISTERING_MELON_SLICE, 3)));

        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Novice, new ItemStack(Material.STRING, 20),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Novice, new ItemStack(Material.COAL, 10),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.COD, 6), new ItemStack(Material.COOKED_COD, 6)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Novice, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.COD_BUCKET, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Apprentice, new ItemStack(Material.COD, 15),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.SALMON, 6), new ItemStack(Material.COOKED_SALMON, 6)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Apprentice, new ItemStack(Material.EMERALD, 2),
                new ItemStack(Material.CAMPFIRE, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Journeyman, new ItemStack(Material.SALMON, 13),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Journeyman, new
        // ItemStack(Material.EMERALD, 8 - 22),
        // new ItemStack(Material.ENCHANTED_FISHING_ROD, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Expert, new ItemStack(Material.TROPICAL_FISH, 6),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Master, new ItemStack(Material.PUFFERFISH, 4),
                new ItemStack(Material.EMERALD, 1)));

        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("boat") || x.name().toLowerCase().contains("raft"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.FISHERMAN, Level.Master, new ItemStack(bannerMaterial, 1),
                    new ItemStack(Material.EMERALD, 1)));
        }

        Traders.add(
                new VillagerTrade(Profession.FLETCHER, Level.Novice, new ItemStack(Material.STICK, 32),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(
                new VillagerTrade(Profession.FLETCHER, Level.Novice, new ItemStack(Material.EMERALD, 1),
                        new ItemStack(Material.ARROW, 16)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.GRAVEL, 10), new ItemStack(Material.FLINT, 10)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Apprentice, new ItemStack(Material.FLINT, 26),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Apprentice, new ItemStack(Material.EMERALD, 2),
                new ItemStack(Material.BOW, 1)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Journeyman, new ItemStack(Material.STRING, 14),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Journeyman, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.CROSSBOW, 1)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Expert, new ItemStack(Material.FEATHER, 24),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Expert, new
        // ItemStack(Material.EMERALD,7-21),new ItemStack(Material.ENCHANTED_BOW,1)));
        Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Master, new ItemStack(Material.TRIPWIRE_HOOK, 8),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Master, new
        // ItemStack(Material.EMERALD,8-22),new
        // ItemStack(Material.ENCHANTED_CROSSBOW,1)));
        // Traders.add(new VillagerTrade(Profession.FLETCHER, Level.Master, new
        // ItemStack(Material.EMERALD_AND_ARROW,2 and 5),new
        // ItemStack(Material.TIPPED_ARROW,5)));

        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Novice, new ItemStack(Material.LEATHER, 6),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Novice, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.LEATHER_LEGGINGS, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Novice, new ItemStack(Material.EMERALD, 7),
                new ItemStack(Material.LEATHER_CHESTPLATE, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Apprentice, new ItemStack(Material.FLINT, 26),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Apprentice, new ItemStack(Material.EMERALD, 5),
                new ItemStack(Material.LEATHER_HELMET, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Apprentice, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.LEATHER_BOOTS, 1)));
        Traders.add(
                new VillagerTrade(Profession.LEATHERWORKER, Level.Journeyman, new ItemStack(Material.RABBIT_HIDE, 9),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Journeyman, new ItemStack(Material.EMERALD, 7),
                new ItemStack(Material.LEATHER_CHESTPLATE, 1)));

        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Expert, new ItemStack(Material.TURTLE_SCUTE, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(
                new VillagerTrade(Profession.LEATHERWORKER, Level.Expert, new ItemStack(Material.ARMADILLO_SCUTE, 4),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Expert, new ItemStack(Material.EMERALD, 6),
                new ItemStack(Material.LEATHER_HORSE_ARMOR, 1)));
        Traders.add(
                new VillagerTrade(Profession.LEATHERWORKER, Level.Master, new ItemStack(Material.EMERALD, 6),
                        new ItemStack(Material.SADDLE, 1)));
        Traders.add(new VillagerTrade(Profession.LEATHERWORKER, Level.Master, new ItemStack(Material.EMERALD, 5),
                new ItemStack(Material.LEATHER_HELMET, 1)));

        Traders.add(
                new VillagerTrade(Profession.LIBRARIAN, Level.Novice, new ItemStack(Material.PAPER, 24),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Novice, new ItemStack(Material.EMERALD, 9),
                new ItemStack(Material.BOOKSHELF, 1)));
        // Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Novice, new
        // ItemStack(Material.EMERALD_AND_BOOK,5-64 and 1),new
        // ItemStack(Material.ENCHANTED_BOOK,1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Apprentice, new ItemStack(Material.BOOK, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.LANTERN, 1)));
        // Traders.add(new VillagerTrade(Level.Apprentice, new
        // ItemStack(Material.EMERALD_AND_BOOK,5-64 and 1),new
        // ItemStack(Material.ENCHANTED_BOOK,1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Journeyman, new ItemStack(Material.INK_SAC, 5),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.GLASS, 4)));
        // Traders.add(new VillagerTrade(Level.Journeyman, new
        // ItemStack(Material.EMERALD_AND_BOOK,5-64 and 1),new
        // ItemStack(Material.ENCHANTED_BOOK,1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Expert, new ItemStack(Material.WRITABLE_BOOK, 2),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Expert, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.COMPASS, 1)));
        Traders.add(
                new VillagerTrade(Profession.LIBRARIAN, Level.Expert, new ItemStack(Material.EMERALD, 5),
                        new ItemStack(Material.CLOCK, 1)));
        // Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Expert, new
        // ItemStack(Material.EMERALD_AND_BOOK,5-64 and 1),new
        // ItemStack(Material.ENCHANTED_BOOK,1)));
        Traders.add(new VillagerTrade(Profession.LIBRARIAN, Level.Master, new ItemStack(Material.EMERALD, 20),
                new ItemStack(Material.NAME_TAG, 1)));

        Traders.add(new VillagerTrade(Profession.MASON, Level.Novice, new ItemStack(Material.CLAY_BALL, 10),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(
                new VillagerTrade(Profession.MASON, Level.Novice, new ItemStack(Material.EMERALD, 1),
                        new ItemStack(Material.BRICK, 10)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Apprentice, new ItemStack(Material.STONE, 20),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Apprentice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.CHISELED_STONE_BRICKS, 4)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.GRANITE, 16),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.ANDESITE, 16),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.DIORITE, 16),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.POLISHED_ANDESITE, 4)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.POLISHED_GRANITE, 4)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.POLISHED_DIORITE, 4)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.DRIPSTONE_BLOCK, 4)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Expert, new ItemStack(Material.QUARTZ, 12),
                new ItemStack(Material.EMERALD, 1)));

        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("terracotta"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.MASON, Level.Expert,
                    new ItemStack(Material.EMERALD, 1), new ItemStack(bannerMaterial, 1)));
        }

        Traders.add(new VillagerTrade(Profession.MASON, Level.Master, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.QUARTZ_PILLAR, 1)));
        Traders.add(new VillagerTrade(Profession.MASON, Level.Master, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.QUARTZ_BLOCK, 1)));

        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Novice, new ItemStack(Material.WHITE_WOOL, 18),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Novice, new ItemStack(Material.BROWN_WOOL, 18),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Novice, new ItemStack(Material.BLACK_WOOL, 18),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Novice, new ItemStack(Material.GRAY_WOOL, 18),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(
                new VillagerTrade(Profession.SHEPHERD, Level.Novice, new ItemStack(Material.EMERALD, 2),
                        new ItemStack(Material.SHEARS, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Apprentice, new ItemStack(Material.BLACK_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Apprentice, new ItemStack(Material.GRAY_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Apprentice, new ItemStack(Material.LIME_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Apprentice, new ItemStack(Material.LIGHT_BLUE_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Apprentice, new ItemStack(Material.WHITE_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));

        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("_wool"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman,
                    new ItemStack(Material.EMERALD, 1), new ItemStack(bannerMaterial, 1)));
        }
        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("_carpet"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman,
                    new ItemStack(Material.EMERALD, 1), new ItemStack(bannerMaterial, 4)));
        }

        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Journeyman, new ItemStack(Material.RED_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Journeyman, new ItemStack(Material.LIGHT_GRAY_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Journeyman, new ItemStack(Material.PINK_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Journeyman, new ItemStack(Material.YELLOW_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Journeyman, new ItemStack(Material.ORANGE_DYE, 12),
                new ItemStack(Material.EMERALD, 1)));

        for (Material bannerMaterial : Arrays.stream(Material.values())
                .filter(x -> x.name().toLowerCase().contains("_bed"))
                .toList()) {
            Traders.add(new VillagerTrade(Profession.MASON, Level.Journeyman,
                    new ItemStack(Material.EMERALD, 3), new ItemStack(bannerMaterial, 4)));
        }

        // Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Expert, new
        // ItemStack(Material.GREEN,_BROWN,_BLUE,_PURPLE,_CYAN,_AND_MAGENTA_DYES,12),new
        // ItemStack(Material.EMERALD,1)));
        // Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Expert, new
        // ItemStack(Material.EMERALD,3),new ItemStack(Material.ANY_COLOR_BANNER,1)));
        Traders.add(new VillagerTrade(Profession.SHEPHERD, Level.Master, new ItemStack(Material.EMERALD, 2),
                new ItemStack(Material.PAINTING, 3)));

        Traders.add(
                new VillagerTrade(Profession.TOOLSMITH, Level.Novice, new ItemStack(Material.COAL, 15),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.STONE_AXE, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.STONE_SHOVEL, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.STONE_PICKAXE, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Novice, new ItemStack(Material.EMERALD, 1),
                new ItemStack(Material.STONE_HOE, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Apprentice, new ItemStack(Material.IRON_INGOT, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Apprentice, new ItemStack(Material.EMERALD, 36),
                new ItemStack(Material.BELL, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Journeyman, new ItemStack(Material.FLINT, 30),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Journeyman, new
        // ItemStack(Material.EMERALD,20),new
        // ItemStack(Material.ENCHANTED_IRON_AXE,1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Journeyman, new
        // ItemStack(Material.EMERALD,21),new
        // ItemStack(Material.ENCHANTED_IRON_SHOVEL,1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Journeyman, new
        // ItemStack(Material.EMERALD,22),new
        // ItemStack(Material.ENCHANTED_IRON_PICKAXE,1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Journeyman, new ItemStack(Material.EMERALD, 4),
                new ItemStack(Material.DIAMOND_HOE, 1)));
        Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Expert, new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Expert, new
        // ItemStack(Material.EMERALD,31),new
        // ItemStack(Material.ENCHANTED_DIAMOND_AXE,1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Expert, new
        // ItemStack(Material.EMERALD,24),new
        // ItemStack(Material.ENCHANTED_DIAMOND_SHOVEL,1)));
        // Traders.add(new VillagerTrade(Profession.TOOLSMITH, Level.Master, new
        // ItemStack(Material.EMERALD,32),new
        // ItemStack(Material.ENCHANTED_DIAMOND_PICKAXE,1)));

        Traders.add(
                new VillagerTrade(Profession.WEAPONSMITH, Level.Novice, new ItemStack(Material.COAL, 15),
                        new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Novice, new ItemStack(Material.EMERALD, 3),
                new ItemStack(Material.IRON_AXE, 1)));
        // Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Novice, new
        // ItemStack(Material.EMERALD,7-21),new
        // ItemStack(Material.ENCHANTED_IRON_SWORD,1)));
        Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Apprentice, new ItemStack(Material.IRON_INGOT, 4),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Apprentice, new ItemStack(Material.EMERALD, 36),
                new ItemStack(Material.BELL, 1)));
        Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Journeyman, new ItemStack(Material.FLINT, 24),
                new ItemStack(Material.EMERALD, 1)));
        Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Expert, new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.EMERALD, 1)));
        // Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Expert, new
        // ItemStack(Material.EMERALD,17-31),new
        // ItemStack(Material.ENCHANTED_DIAMOND_AXE,1)));
        // Traders.add(new VillagerTrade(Profession.WEAPONSMITH, Level.Master, new
        // ItemStack(Material.EMERALD,17-31),new
        // ItemStack(Material.ENCHANTED_DIAMOND_SWORD,1)))

        return Traders;
    }
}
