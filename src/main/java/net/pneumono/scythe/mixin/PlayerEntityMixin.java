package net.pneumono.scythe.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.pneumono.scythe.content.ScytheRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
@SuppressWarnings("unused")
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F"))
    private void applyExtinguishingAttackDamage(Entity target, CallbackInfo info, @Local(ordinal = 1) LocalFloatRef damage) {
        int level = EnchantmentHelper.getLevel(ScytheRegistry.EXTINGUISHING, getMainHandStack());
        if (level > 0 && target.getType().isIn(ScytheRegistry.TAG_NETHER_MOBS)) {
            damage.set(damage.get() + (level * 2.5F));
        }
    }
}
