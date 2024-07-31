package net.pneumono.scythe.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pneumono.scythe.Scythe;
import net.pneumono.scythe.content.ScytheRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
@SuppressWarnings("unused")
public abstract class ScytheHudMixin {
    private static final Identifier SCYTHE_HUD_TEXTURES = new Identifier(Scythe.MOD_ID, "textures/gui/hud.png");

    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;
    @Shadow
    private MinecraftClient client;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;)V", ordinal = 0))
    private void renderScytheBar(DrawContext context, CallbackInfo info) {
        int launchingLevel = EnchantmentHelper.getLevel(ScytheRegistry.LAUNCHING, Objects.requireNonNull(client.player).getActiveItem());
        int itemUseTime = client.player.getItemUseTime();

        if (itemUseTime > 0 && launchingLevel > 0) {
            int width = MathHelper.clamp(Math.round(itemUseTime / (((11F - launchingLevel) * launchingLevel) + (11F - launchingLevel)) * 19), 0, 19);

            int textureHeight = 5 * (launchingLevel - 1);
            if (launchingLevel > 3) {
                textureHeight = 0;
            }

            int x = scaledWidth / 2 - 10;
            int y = scaledHeight / 2 - 7 + 12;
            context.drawTexture(SCYTHE_HUD_TEXTURES, x, y, 0, textureHeight, 19, 5);
            context.drawTexture(SCYTHE_HUD_TEXTURES, x, y, 19, textureHeight, width, 5);
        }
    }
}
