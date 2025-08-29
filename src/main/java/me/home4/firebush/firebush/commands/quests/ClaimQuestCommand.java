package me.home4.firebush.firebush.commands.quests;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.classes.Task;
import me.home4.firebush.firebush.files.FileManager;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class ClaimQuestCommand implements CommandExecutor {

    private final Firebush plugin;
    private final FileConfiguration config;

    public ClaimQuestCommand(Firebush plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage("You're not allowed to use this command manually!");
            return true;
        }

        System.out.println("Claim Quest Sender Name: " + sender.getName());

        String playerNick = sender.getName();
        String playerStringUUID = Players.getUUIDfromNick(playerNick);
        System.out.println(playerStringUUID);
        Player player = Bukkit.getPlayer(UUID.fromString(playerStringUUID));


        if (playerStringUUID.equals("")) {
            player.sendMessage("You're not participating in the game!");
            return true;
        }

        if (Players.getTask(playerStringUUID).getName().equals("")) {
            player.sendMessage("You don't have a task to redeem!");
            return true;
        }

        FileManager.updateHistoryFile(playerNick + "has claimed their quest, titled " + Players.get().getString(playerStringUUID + ".task"));
        int currentMaxHealth = (int) player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        int newMaxHealth = currentMaxHealth;

        int maxHealth = config.getInt("uhcMaxHearts");
        int heartsReward = config.getInt("heartsReward");

        if (Players.get().getBoolean(playerStringUUID + ".hardtask")) {
            heartsReward *= 2;
        }

        if (Players.getLives(playerStringUUID) == 1) {
            // heartsReward /= 2
        }

        int extraLoot = 0;

        if ((maxHealth - currentMaxHealth) < heartsReward) {
            extraLoot = (heartsReward - (maxHealth - currentMaxHealth)) / 2;
            newMaxHealth = maxHealth;
        } else {
            newMaxHealth += heartsReward;
        }

        if (extraLoot < 2) {
            extraLoot = 2;
        }

        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        player.setHealth(newMaxHealth);

        Location rewardsLocation = new Location(getServer().getWorld("world"), config.getInt("rewardCoords.x"), config.getInt("rewardCoords.y"), config.getInt("rewardCoords.z"));
        ArrayList<String> questRewards = (ArrayList<String>) config.getList("questRewards");
        System.out.println(questRewards);
        System.out.println(questRewards.get(0));

        PlayerInventory inventory = player.getInventory();
        TaskManager.removeTask(playerStringUUID);

        Players.setTask(playerStringUUID, null);

        Collections.shuffle(questRewards);
        int finalExtraLoot = extraLoot;
        World world = rewardsLocation.getWorld();



        // Pre-spawn particle effect: particles converging to the reward spot
        new BukkitRunnable() {
            int countdown = 40; // 2 seconds of particle effects (40 ticks)
            Random random = new Random();

            @Override
            public void run() {
                if (countdown <= 0) {
                    // End the pre-spawn effect and start spawning rewards
                    this.cancel();
                    spawnRewards(player, questRewards, rewardsLocation, finalExtraLoot);
                    return;
                }
                if (countdown == 40) {
                    world.playSound(rewardsLocation, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 1.5f);
                }

                // Generate particles from random points far away and move them closer to the reward spot
                int particleCount = 10; // Number of particles per tick

                // Create directional particles moving toward the reward location
                for (int i = 0; i < 10; i++) { // Emit 10 particles per tick
                    double radius = (countdown * 5) / 40; // Distance from the center where particles originate
                    double angle = random.nextDouble() * 2 * Math.PI;

                    // Random starting point around the target
                    double x = rewardsLocation.getX() + radius * Math.cos(angle);
                    double y = rewardsLocation.getY() + random.nextDouble() * 2; // Random height
                    double z = rewardsLocation.getZ() + radius * Math.sin(angle);

                    Location startLocation = new Location(world, x, y, z);

                    // Calculate directional vector toward the center
                    Vector direction = rewardsLocation.toVector().subtract(startLocation.toVector()).normalize();

                    // Spawn directional particle
                    world.spawnParticle(Particle.LARGE_SMOKE,
                            startLocation, // Starting position
                            0,            // Count: 0 to enable directional behavior
                            direction.getX(), direction.getY(), direction.getZ(), // Directional vector
                            (countdown / 40));         // Speed: Adjust for a more dramatic effect
                }


                countdown--;
            }
        }.runTaskTimer(plugin, 0, 2); // Runs every 2 ticks for smoother effect            // Schedule the spawning process


//        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//        FileManager.updateHistoryFile(timestamp + " " + player.getDisplayName() + " has claimed their target");


        return true;
    }

    private void spawnRewards(Player player, List<String> questRewards, Location rewardsLocation, int finalExtraLoot) {
        World world = rewardsLocation.getWorld();
        List<Item> spawnedItems = new ArrayList<>();
        List<String> questEggRewards = config.getStringList("questEggRewards"); // Load the egg rewards list

        new BukkitRunnable() {
            int rewardIndex = 0;

            @Override
            public void run() {
                if (rewardIndex >= questRewards.size() - 1 || rewardIndex >= finalExtraLoot) {
                    HashMap<String, Task> playerTaskMap = new HashMap<>();
                    Task redTask = new TaskManager().getRandomRedTask();
                    String playerUUID = player.getUniqueId().toString();

                    if (Players.getLives(playerUUID) == 1) {
                        playerTaskMap.put(playerUUID, redTask);
                        TaskManager.givePlayerTask(player, playerTaskMap);
                    }


                    cancel(); // Stop the task once all rewards are spawned
                    return;
                }

                String reward = questRewards.get(rewardIndex);
                String[] parts = reward.split(":");
                String itemName = parts[0];
                int minAmount = 1;
                int maxAmount = 1;

                if (parts.length > 1) {
                    String[] amounts = parts[1].split("-");
                    minAmount = Integer.parseInt(amounts[0]);
                    if (amounts.length > 1) {
                        maxAmount = Integer.parseInt(amounts[1]);
                    }
                }

                int amount = minAmount + (int) (Math.random() * (maxAmount - minAmount + 1));

                ItemStack itemStack;
                if (itemName.equalsIgnoreCase("spawn_egg")) {
                    // Handle spawn eggs
                    List<String> eggRewards = plugin.getConfig().getStringList("questEggRewards");
                    String randomEgg = eggRewards.get(new Random().nextInt(eggRewards.size()));
                    itemStack = new ItemStack(Material.valueOf(randomEgg.toUpperCase() + "_SPAWN_EGG"), amount);

                } else if (itemName.equalsIgnoreCase("enchanted_book")) {
                    // Handle enchanted books
                    List<String> bookRewards = plugin.getConfig().getStringList("questEnchantedBookRewards");
                    String randomEnchantment = bookRewards.get(new Random().nextInt(bookRewards.size()));
                    String[] enchantmentParts = randomEnchantment.split(":");
                    String enchantmentName = enchantmentParts[0];
                    int minLevel = Integer.parseInt(enchantmentParts[1].split("-")[0]);
                    int maxLevel = enchantmentParts[1].contains("-") ? Integer.parseInt(enchantmentParts[1].split("-")[1]) : minLevel;
                    int level = minLevel + (int) (Math.random() * (maxLevel - minLevel + 1));

                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
                    if (enchantment != null) {
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta instanceof EnchantmentStorageMeta) {
                            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
                            storageMeta.addStoredEnchant(enchantment, level, true);
                            itemStack.setItemMeta(storageMeta);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid enchantment: " + enchantmentName);
                        rewardIndex++;
                        return;
                    }
                } else if (itemName.equalsIgnoreCase("armor")) {
                    List<String> armorRewards = plugin.getConfig().getStringList("questArmorRewards");
                    String randomArmor = "diamond_" + armorRewards.get(new Random().nextInt(armorRewards.size()));

                    itemStack = new ItemStack(Material.valueOf(randomArmor.toUpperCase()), amount);
                } else if (itemName.equalsIgnoreCase("tool")) {
                    List<String> armorRewards = plugin.getConfig().getStringList("questToolRewards");
                    String randomArmor = "diamond_" + armorRewards.get(new Random().nextInt(armorRewards.size()));

                    itemStack = new ItemStack(Material.valueOf(randomArmor.toUpperCase()), amount);
                } else {
                    // Handle regular items
                    Material material = Material.getMaterial(itemName.toUpperCase());
                    if (material == null) {
                        player.sendMessage(ChatColor.RED + "Invalid reward item: " + itemName);
                        rewardIndex++;
                        return;
                    }
                    itemStack = new ItemStack(material, amount);
                }

                // Drop the item
                Item droppedItem = world.dropItem(rewardsLocation, itemStack);
                droppedItem.setOwner(player.getUniqueId());
                droppedItem.setPickupDelay(20);
                spawnedItems.add(droppedItem);

                // Cool visual effects and sound for each item
                getServer().getWorld("world").playSound(rewardsLocation, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
                getServer().getWorld("world").spawnParticle(Particle.ELECTRIC_SPARK, rewardsLocation, 20, 0.5, 0.5, 0.5);

                rewardIndex++;
            }
        }.runTaskTimer(plugin, 0, 20); //  1 ticks (0.05 seconds)

        // Ensure only the player can pick up the items
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Item item : spawnedItems) {
                    if (item.isDead()) {
                        spawnedItems.remove(item);
                    } else if (!item.getOwner().equals(player.getUniqueId())) {
                        item.remove();
                    }
                }

                if (spawnedItems.isEmpty()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Check every second to clean up unauthorized pickups

    }
}
