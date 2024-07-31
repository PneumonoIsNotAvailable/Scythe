package net.pneumono.scythe.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.pneumono.scythe.content.ScytheItem;
import net.pneumono.scythe.content.ScytheRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
@SuppressWarnings("unused")
public abstract class ScytheMovementMixin extends LivingEntity {
    protected ScytheMovementMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0))
    private boolean preventScytheSlowdown(ClientPlayerEntity player, Operation<Boolean> original) {
        return !(player.getActiveItem().getItem() instanceof ScytheItem && EnchantmentHelper.getLevel(ScytheRegistry.LAUNCHING, player.getActiveItem()) > 0) && original.call(player);
    }
}
