package steelandhonor.modid.registry;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import steelandhonor.modid.Steel_and_honor;

/**
 * Very simple item registry for 1.21.8:
 * - Knight armor set (iron-tier stats)
 * - Knight sword (iron-ish stats)
 *
 * No data-components, just the classic ArmorItem / SwordItem API so it compiles cleanly.
 */
public class ModItems {

    // Armor
    public static Item KNIGHT_HELMET;
    public static Item KNIGHT_CHESTPLATE;
    public static Item KNIGHT_LEGGINGS;
    public static Item KNIGHT_BOOTS;

    // Weapon
    public static Item KNIGHT_SWORD;

    /**
     * Call this from your main mod init class.
     * It registers all items.
     */
    public static void initialize() {

        // ---------- ARMOR (IRON-LIKE) ----------
        KNIGHT_HELMET = register("knight_helmet",
                new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET,
                        new Item.Settings()));

        KNIGHT_CHESTPLATE = register("knight_chestplate",
                new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE,
                        new Item.Settings()));

        KNIGHT_LEGGINGS = register("knight_leggings",
                new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS,
                        new Item.Settings()));

        KNIGHT_BOOTS = register("knight_boots",
                new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS,
                        new Item.Settings()));

        // ---------- SWORD (IRON-LIKE) ----------
        // Damage / speed are the usual SwordItem constructor values:
        //   new SwordItem(material, attackDamage, attackSpeed, settings)
        KNIGHT_SWORD = register("knight_sword",
                new SwordItem(
                        ToolMaterials.IRON,
                        3,          // extra damage over material base
                        -2.4f,      // attack speed
                        new Item.Settings()
                ));

        Steel_and_honor.LOGGER.info("[Steel_and_honor] Registered knight armor + sword (classic API).");
    }

    private static Item register(String name, Item item) {
        Identifier id = Identifier.of(Steel_and_honor.MOD_ID, name); // 1.21.8 uses factory instead of public ctor
        return Registry.register(Registries.ITEM, id, item);
    }
}
