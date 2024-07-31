package net.pneumono.scythe.mixin;

import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pneumono.scythe.content.ScytheRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
@SuppressWarnings("unused")
public abstract class PiglinEntityMixin extends AbstractPiglinEntity implements CrossbowUser, InventoryOwner {
    public PiglinEntityMixin(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "makeInitialWeapon", at = @At("HEAD"), cancellable = true)
    private void makeInitialWeaponOrScythe(CallbackInfoReturnable<ItemStack> cir) {
        if (random.nextFloat() > 0.9375) {
            cir.setReturnValue(new ItemStack(ScytheRegistry.SCYTHE));
        }
    }
}
