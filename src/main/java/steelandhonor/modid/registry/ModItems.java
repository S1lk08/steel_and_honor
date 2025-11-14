package steelandhonor.modid.registry;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ArmorComponent;
import net.minecraft.component.type.ItemAttributeModifiersComponent;
import net.minecraft.component.type.ItemAttributeModifiersComponent.Slot;
import net.minecraft.component.type.ToolComponent;

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

        // ARMOR COMPONENT (1.21.8)
        ArmorComponent knightArmor = new ArmorComponent(
                15,
                new int[]{2, 5, 4, 1},
                12,
                SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                0.0f,
                0.0f
        );

        KNIGHT_HELMET = register("knight_helmet",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ARMOR, knightArmor)
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_HELMET.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        KNIGHT_CHESTPLATE = register("knight_chestplate",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ARMOR, knightArmor)
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_CHESTPLATE.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        KNIGHT_LEGGINGS = register("knight_leggings",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ARMOR, knightArmor)
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_LEGGINGS.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        KNIGHT_BOOTS = register("knight_boots",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ARMOR, knightArmor)
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_BOOTS.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        // SWORD ATTRIBUTES (1.21.8)
        ItemAttributeModifiersComponent swordAttributes = ItemAttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                UUID.randomUUID(),
                                5.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        Slot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(
                                UUID.randomUUID(),
                                -2.4,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        Slot.MAINHAND
                )
                .build();

        ToolComponent swordTool = new ToolComponent(
                0,
                1.0f,
                0
        );

        KNIGHT_SWORD = register("knight_sword",
                new Item(new Item.Settings()
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, swordAttributes)
                        .component(DataComponentTypes.TOOL, swordTool)
                        .component(DataComponentTypes.EQUIPMENT_SLOT,
                                Items.IRON_SWORD.getDefaultStack().get(DataComponentTypes.EQUIPMENT_SLOT))
                ));

        Steel_and_honor.LOGGER.info("[Steel_and_honor] Items registered for 1.21.8.");
    }

    private static Item register(String name, Item item) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(Steel_and_honor.MOD_ID, name),
                item
        );
    }
}
