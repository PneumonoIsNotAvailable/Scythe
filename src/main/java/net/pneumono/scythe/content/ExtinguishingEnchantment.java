package net.pneumono.scythe.content;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.pneumono.pneumonocore.enchantment.ModEnchantment;
import org.jetbrains.annotations.NotNull;

public class ExtinguishingEnchantment extends ModEnchantment {
    protected ExtinguishingEnchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, EnchantmentTarget.BREAKABLE, slotTypes);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinPower(int level) {
        return 2 + (level - 1) * 8;
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
    public float getAttackDamage(int level, EntityGroup group) {
        return super.getAttackDamage(level, group);
    }
}
