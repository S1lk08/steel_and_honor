package steelandhonor.modid.registry;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ArmorComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.component.type.ItemAttributeModifiersComponent;
import net.minecraft.component.type.ItemAttributeModifiersComponent.Slot;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

import steelandhonor.modid.Steel_and_honor;

import java.util.UUID;


public class ModItems {

    public static Item KNIGHT_HELMET;
    public static Item KNIGHT_CHESTPLATE;
    public static Item KNIGHT_LEGGINGS;
    public static Item KNIGHT_BOOTS;
    public static Item KNIGHT_SWORD;

    public static void initialize() {

        // =======================
        //    ARMOR STATS (1.21.8)
        // =======================

        int[] protection = {2, 5, 4, 1}; // helmet, chestplate, leggings, boots
        float toughness = 0.0f;
        float knockback = 0.0f;

        // -----------------------
        // Helmet
        // -----------------------
        KNIGHT_HELMET = register("knight_helmet",
                armorItem(Items.IRON_HELMET, protection[0], toughness, knockback)
        );

        // -----------------------
        // Chestplate
        // -----------------------
        KNIGHT_CHESTPLATE = register("knight_chestplate",
                armorItem(Items.IRON_CHESTPLATE, protection[1], toughness, knockback)
        );

        // -----------------------
        // Leggings
        // -----------------------
        KNIGHT_LEGGINGS = register("knight_leggings",
                armorItem(Items.IRON_LEGGINGS, protection[2], toughness, knockback)
        );

        // -----------------------
        // Boots
        // -----------------------
        KNIGHT_BOOTS = register("knight_boots",
                armorItem(Items.IRON_BOOTS, protection[3], toughness, knockback)
        );

        // =======================
        //       SWORD (1.21.8)
        // =======================

        ItemAttributeModifiers swordAttributes = ItemAttributeModifiers.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(UUID.randomUUID(), 5.0, EntityAttributeModifier.Operation.ADD_VALUE),
                        Slot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(UUID.randomUUID(), -2.4, EntityAttributeModifier.Operation.ADD_VALUE),
                        Slot.MAINHAND
                )
                .build();

        KNIGHT_SWORD = register("knight_sword",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, swordAttributes)
                        .component(DataComponentTypes.MAX_DAMAGE, 500) // durability
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_SWORD.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        Steel_and_honor.LOGGER.info("Registered knight items for 1.21.8");
    }

    private static Item armorItem(Item vanillaArmor,
                                  int protection,
                                  float toughness,
                                  float knockback) {

        ItemAttributeModifiers armorAttributes = ItemAttributeModifiers.builder()
                .add(
                        EntityAttributes.GENERIC_ARMOR,
                        new EntityAttributeModifier(UUID.randomUUID(), protection, EntityAttributeModifier.Operation.ADD_VALUE),
                        Slot.ARMOR
                )
                .add(
                        EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(UUID.randomUUID(), toughness, EntityAttributeModifier.Operation.ADD_VALUE),
                        Slot.ARMOR
                )
                .add(
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(UUID.randomUUID(), knockback, EntityAttributeModifier.Operation.ADD_VALUE),
                        Slot.ARMOR
                )
                .build();

        return new Item(new Item.Settings()
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, armorAttributes)
                .component(DataComponentTypes.EQUIPMENT_SLOT,
                        vanillaArmor.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                .component(DataComponentTypes.MAX_DAMAGE,
                        vanillaArmor.getDefaultStack().get(DataComponentTypes.MAX_DAMAGE))
        );
    }

    private static Item register(String name, Item item) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(Steel_and_honor.MOD_ID, name),
                item
        );
    }
}
