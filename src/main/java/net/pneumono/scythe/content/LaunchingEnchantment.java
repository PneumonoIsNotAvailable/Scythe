package net.pneumono.scythe.content;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.pneumono.pneumonocore.enchantment.ModEnchantment;
import org.jetbrains.annotations.NotNull;

public class LaunchingEnchantment extends ModEnchantment {
    protected LaunchingEnchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, EnchantmentTarget.BREAKABLE, slotTypes);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinPower(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxPower(int level) {
        return level * 10;
    }

    @Override
    public boolean isAcceptableItem(@NotNull ItemStack stack) {
        return stack.isOf(ScytheRegistry.SCYTHE);
    }

    @Override
    public Enchantment[] getMutuallyExclusiveEnchantments() {
        return new Enchantment[] {ScytheRegistry.REAPING, Enchantments.LOOTING};
    }
}
