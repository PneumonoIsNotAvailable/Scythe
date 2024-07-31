package net.pneumono.scythe.content;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Vanishable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.pneumono.pneumonocore.enchantment.EnchantableItem;
import net.pneumono.pneumonocore.util.PneumonoMathHelper;

import java.util.List;

public class ScytheItem extends ToolItem implements Vanishable, EnchantableItem {
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public ScytheItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 6, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.4, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        int itemUseTime = this.getMaxUseTime(stack) - remainingUseTicks;
        int launchingLevel = EnchantmentHelper.getLevel(ScytheRegistry.LAUNCHING, stack);

        if (itemUseTime >= (11 - launchingLevel) || itemUseTime >= 10) {
            double velocity = (MathHelper.clamp(itemUseTime - (11 - launchingLevel), 0, (11 - launchingLevel) * launchingLevel) / 20F) + 0.65;

            world.playSoundFromEntity(null, user, ScytheRegistry.ITEM_SCYTHE_SWING, user instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F);
            spawnSweepAttackParticles(world, user, velocity, launchingLevel > 0);

            Box box = new Box(new BlockPos((int) user.getX() - 4, (int) user.getY() - 1, (int) user.getZ() - 4), new BlockPos((int) user.getX() + 4, (int) user.getY() + 2, (int) user.getZ() + 4));
            List<Entity> targetedEntities = world.getOtherEntities(user, box);
            for (Entity entity : targetedEntities) {
                if (entity.distanceTo(user) <= 4 && isNotBehindWall(user, entity)) {
                    // Entity Damage
                    float damageBefore = (getAttackDamage(stack, entity) + (float) user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + 3);
                    float distance = entity.distanceTo(user);
                    float damage = (distance >= 1 ? damageBefore / distance : ((damageBefore * distance) + damageBefore) / 2) / MathHelper.clamp(targetedEntities.size() / 5F, 1, 5);
                    DamageSource source = user instanceof PlayerEntity player ? world.getDamageSources().playerAttack(player) : world.getDamageSources().mobAttack(user);
                    entity.damage(source, damage);

                    // Entity Knockback
                    double resistance = entity instanceof LivingEntity livingEntity ? livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) : 0;
                    double knockbackVelocity = 1.75 * (1.0 - resistance);
                    double xDifference = entity.getX() - user.getX();
                    double zDifference = entity.getZ() - user.getZ();

                    double multiplier = knockbackVelocity / Math.sqrt((xDifference * xDifference) + (zDifference * zDifference));
                    entity.addVelocity(xDifference * multiplier, launchingLevel > 0 ? velocity / 2 : 0.2, zDifference * multiplier);

                    // Item Damage
                    if (!world.isClient) {
                        stack.damage(1, user, (p) -> p.sendToolBreakStatus(user.getActiveHand()));
                    }
                }
            }

            if (launchingLevel > 0) {
                user.addVelocity(0, velocity, 0);
                if (!world.isClient) {
                    stack.damage(2, user, (entity) -> entity.sendToolBreakStatus(user.getActiveHand()));
                }
            }

            if (user instanceof PlayerEntity player) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
            }
        }
    }

    public static boolean isNotBehindWall(Entity watchingEntity, Entity watchedEntity) {
        if (watchingEntity.getWorld() != watchedEntity.getWorld()) {
            return false;
        } else {
            Vec3d vec3d = new Vec3d(watchingEntity.getX(), watchingEntity.getY() + (watchedEntity.getHeight() / 2), watchingEntity.getZ());
            Vec3d vec3d2 = new Vec3d(watchedEntity.getX(), watchedEntity.getY() + (watchedEntity.getHeight() / 2), watchedEntity.getZ());
            if (vec3d2.distanceTo(vec3d) > 128.0) {
                return false;
            } else {
                return watchingEntity.getWorld().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, watchingEntity)).getType() == HitResult.Type.MISS;
            }
        }
    }

    public static float getAttackDamage(ItemStack stack, Entity entity) {
        float g;
        if (entity instanceof LivingEntity livingEntity) {
            g = EnchantmentHelper.getAttackDamage(stack, livingEntity.getGroup());
        } else {
            g = EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
        }
        int extinguishingLevel = EnchantmentHelper.getLevel(ScytheRegistry.EXTINGUISHING, stack);
        if (extinguishingLevel > 0 && entity.getType().isIn(ScytheRegistry.TAG_NETHER_MOBS)) {
            g += (2 * extinguishingLevel);
        }
        return g;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            return TypedActionResult.fail(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, (entity) -> entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getHardness(world, pos) != 0.0F) {
            stack.damage(2, miner, (entity) -> entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    public int getEnchantability() {
        return 1;
    }

    @Override
    public boolean isAcceptableEnchantment(Enchantment enchantment) {
        return
                enchantment == ScytheRegistry.EXTINGUISHING ||
                        enchantment == ScytheRegistry.LAUNCHING ||
                        enchantment == ScytheRegistry.REAPING ||
                        enchantment == Enchantments.UNBREAKING ||
                        enchantment == Enchantments.MENDING ||
                        enchantment == Enchantments.VANISHING_CURSE ||
                        enchantment == Enchantments.SWEEPING ||
                        enchantment == Enchantments.LOOTING;
    }

    private void spawnSweepAttackParticles(World world, LivingEntity player, double velocity, boolean hasLaunching) {
        if (world instanceof ServerWorld serverWorld) {
            for (int layer = 0; layer < (1 + (velocity * (hasLaunching ? 3 : 0))); ++layer) {
                float yaw = player.getYaw();
                double radius = hasLaunching ? 0.5 + (layer / 4F) : 2;

                spawnParticleRing(serverWorld, player, layer, getOffset(true, yaw, 0, radius), getOffset(false, yaw, 0, radius));

                if (layer < 4) {
                    spawnParticleRing(serverWorld, player, layer, getOffset(true, yaw, 45, radius), getOffset(false, yaw, 45, radius));
                } else {
                    spawnParticleRing(serverWorld, player, layer, getOffset(true, yaw, 30, radius), getOffset(false, yaw, 30, radius));
                    spawnParticleRing(serverWorld, player, layer, getOffset(true, yaw, 60, radius), getOffset(false, yaw, 60, radius));
                }
            }
        }
    }

    private double getOffset(boolean a, float yaw, int angleOffset, double radius) {
        double angle = PneumonoMathHelper.toRadians(yaw + angleOffset);
        return a ? radius * -Math.sin(angle) : radius * Math.cos(angle);
    }

    private void spawnParticleRing(ServerWorld serverWorld, LivingEntity player, float layer, double a, double b) {
        spawnParticle(serverWorld, player, layer, a, b);
        spawnParticle(serverWorld, player, layer, -b, a);
        spawnParticle(serverWorld, player, layer, -a, -b);
        spawnParticle(serverWorld, player, layer, b, -a);
    }

    private void spawnParticle(ServerWorld serverWorld, LivingEntity player, float layer, double x, double z) {
        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + x, player.getY() + 1.2 + (layer / 2), player.getZ() + z, 0, 0, 0.0, 0, 0.0);
    }
}
