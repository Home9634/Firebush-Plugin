package me.home4.firebush.firebush.listeners;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.FileManager;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.w3c.dom.events.Event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class AllListeners implements Listener {

    private final Firebush plugin;
    private final FileConfiguration config;

    private final Random random = new Random();


    public AllListeners(Firebush plugin) {
        this.plugin = plugin;
        this.config  = plugin.getConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // event.setJoinMessage("Welcome to Element 79~");
        Player player = event.getPlayer();

        String playerUUID = player.getUniqueId().toString();
        if (!Players.get().contains(playerUUID)) {
            Players.definePlayer(player);
        } else {
            Players.get().set(playerUUID + ".nick", player.getName());
        }

       // player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);

        Players.save();

        Players.setLifeColor(playerUUID);
        //ActionBar.setActionBarMessage(player.getUniqueId(), "Tree");

        Players.loadInventory(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Players.saveInventory(player);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        String playerUUID = player.getUniqueId().toString();

        if (Players.get().getBoolean(playerUUID + ".excluded")) {
            return;
        }

        if (plugin.getConfig().getBoolean(("uhc"))) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(plugin.getConfig().getInt("uhcMaxHearts"));
        }

        Players.modifyLives(playerUUID, -1);
        Players.setLifeColor(playerUUID);

        Player attacker = (Player) player.getKiller();

        //System.out.println(attacker);
        
        if (attacker != null) {
            String attackerUUID = attacker.getUniqueId().toString();
            //System.out.println(attackerUUID);
            //System.out.println(playerUUID);
            if (Objects.equals(Players.get().getString(attackerUUID + ".target"), playerUUID)) {
                //System.out.println("Successfully killed target.");
                Bukkit.broadcastMessage(attacker.getDisplayName() + ChatColor.LIGHT_PURPLE + "" + ChatColor.LIGHT_PURPLE + " has successfully killed " + player.getDisplayName());
                Players.get().set(attackerUUID + ".extraLives", Players.get().getInt(attackerUUID + ".extraLives") + 1);
                Players.get().set(attackerUUID + ".target", "");
                ActionBar.setActionBarMessage(UUID.fromString(attackerUUID), "");
                logDeathLog(player.getName(), attacker.getName() + " kills " + player.getName() + " as their target.");
                Players.save();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                }
            }
        }


        if (Players.getLives(playerUUID) == 0) {
            player.setGameMode(GameMode.SPECTATOR);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            }
            Location location = player.getLocation();
            World world = location.getWorld();
            world.strikeLightningEffect(location);

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        logDeathLog(event.getEntity().getName(), event.getDeathMessage());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
// Get the entity that died
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        // Check if the spawn egg exists for this entity
        Material spawnEggMaterial = getSpawnEggMaterial(entityType);
        if (spawnEggMaterial == null) return; // Skip if no spawn egg exists

        // 5% chance to drop the spawn egg
        if (random.nextInt(100) < 5) { // Random number between 0-99
            ItemStack spawnEgg = new ItemStack(spawnEggMaterial);
            event.getDrops().add(spawnEgg);
        }
    }

    private Material getSpawnEggMaterial(EntityType entityType) {
        try {
            // Construct the spawn egg material name
            String materialName = entityType.name() + "_SPAWN_EGG";
            Material material = Material.valueOf(materialName);
            // Validate it is actually a spawn egg
            if (material.isItem() && material.name().endsWith("_SPAWN_EGG")) {
                return material;
            }
        } catch (IllegalArgumentException e) {
            // Material does not exist
        }
        return null;
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.ROSE_BUSH) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.ROSE_BUSH) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Block blockAbove = block.getWorld().getBlockAt(location.add(0, 1, 0));
        Block blockBelow = block.getWorld().getBlockAt(location.add(0, -2, 0));

        System.out.println(blockBelow.getType());
        System.out.println(isFirebush(blockBelow));

        if (isFirebush(block) || isFirebush(blockAbove) || isFirebush(blockBelow)) {
            System.out.println("CANCEL BLOCK BREAK");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        // Check if the ignition source is a block being ignited
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            // Get the block being ignited
            Block ignitingBlock = event.getIgnitingBlock();
            Block lowerBlock = ignitingBlock.getWorld().getBlockAt(ignitingBlock.getLocation().add(0, -1, 0));

            // Check if the ignited block is a rose bush with fire on top
            if (lowerBlock.getType() == Material.ROSE_BUSH) {
                // Cancel the block ignite event to prevent nearby blocks from getting burnt
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        Block blockBelow = block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0));
        if (isFirebush(block) || isFirebush(blockBelow)) {
            // Cancel the event to prevent the entity (fire) from being extinguished
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlocks().get(0);
        if (isFirebush(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        // Check if the shooter is a player
//        if (event.isCancelled()) {
//            return;
//        }
//        if (event.getEntity() instanceof Player) {
//            Player player = (Player) event.getEntity();
//
//            // Give the player an arrow
//            ItemStack arrow = new ItemStack(Material.ARROW, 1);
//            player.getInventory().addItem(arrow);
//
//            player.getInventory().removeItem(arrow);
//        };
    }

    @EventHandler
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCrafting(PrepareItemCraftEvent event) {

        ItemStack result = event.getRecipe().getResult();
        if (result != null && result.getType() == Material.ENCHANTING_TABLE && plugin.getConfig().getBoolean("singularenchanter")) {
            // Cancel the crafting event
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        System.out.println(event.getEntityType());
        System.out.println(event.getEntity() instanceof Player);
        if (event.getEntity() instanceof Player && plugin.getConfig().getBoolean("uhc")) {
            Player player = (Player) event.getEntity();
            int minHearts = plugin.getConfig().getInt("uhcMinHearts");
            //int damage = (int) Math.round(event.getFinalDamage() * 2);
            int currentHearts = (int) Math.round(player.getHealth() - event.getFinalDamage());
            if (currentHearts >  minHearts) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentHearts);
            } else {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(minHearts);
            }

        }
    }

    private boolean isFirebush(Block block) {
        Block blockAbove = block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
        Block blockAbove2 = block.getWorld().getBlockAt(block.getLocation().add(0, 2, 0));

        if (block.getType() == Material.ROSE_BUSH && (blockAbove.getType() == Material.FIRE || blockAbove2.getType() == Material.FIRE)) {
            return true;
        };

        return false;
    }

    private void logDeathLog(String playerName, String deathMessage) {
        // Get the current timestamp

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = String.format(timestamp+  " " + playerName + ": " + deathMessage);

        System.out.println(deathMessage);

        FileManager.updateHistoryFile(logEntry);
    }


}
