package net.pneumono.scythe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.pneumono.scythe.content.ScytheRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
@SuppressWarnings("unused")
public abstract class LivingEntityMixin {
    @Shadow
    public abstract LivingEntity getAttacker();

    @ModifyExpressionValue(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getXpToDrop()I"))
    private int getXpToDropWithReaping(int original) {
        LivingEntity attacker = getAttacker();
        if (attacker != null) {
            int reapingLevel = EnchantmentHelper.getLevel(ScytheRegistry.REAPING, attacker.getMainHandStack());
            if (reapingLevel > 0) {
                return original * ((reapingLevel / 3) + 1);
            }
        }
        return original;
    }
}
