package net.pneumono.scythe.content;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.pneumono.scythe.Scythe;

public class ScytheRegistry {
    public static final Item SCYTHE = registerItem("scythe",
            new ScytheItem(new FabricItemSettings().maxCount(1).maxDamage(250)));

    public static Enchantment REAPING = registerEnchantment("reaping", new ReapingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static Enchantment LAUNCHING = registerEnchantment("launching", new LaunchingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static Enchantment EXTINGUISHING = registerEnchantment("extinguishing", new ExtinguishingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static TagKey<EntityType<?>> TAG_NETHER_MOBS = TagKey.of(RegistryKeys.ENTITY_TYPE, Scythe.identifier("nether_mobs"));

    public static SoundEvent ITEM_SCYTHE_SWING = registerScytheSwingSound();

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Scythe.identifier(name), item);
    }

    private static Enchantment registerEnchantment(String name, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, Scythe.identifier(name), enchantment);
    }

    private static SoundEvent registerScytheSwingSound() {
        Identifier id = Scythe.identifier("item_scythe_swing");
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerScytheContent() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> content.addAfter(Items.TRIDENT, SCYTHE));
    }
}
