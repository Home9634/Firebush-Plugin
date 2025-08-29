package me.home4.firebush.firebush.files.amongus;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WeaponManager {

    private static final String SHARPNESS_AXE_NAME = "Murder Weapon";

    public static ItemStack getSharpnessAxe() {
        // Create a wooden axe
        ItemStack axe = new ItemStack(Material.WOODEN_AXE);

        // Set its meta data (name, enchantments, etc.)
        ItemMeta meta = axe.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SHARPNESS_AXE_NAME); // Set a custom name
            meta.addEnchant(Enchantment.SHARPNESS, 1000, true); // Add Sharpness 1k
            meta.setUnbreakable(true); // Make it unbreakable
            axe.setItemMeta(meta);
        }

        return axe;
    }
}