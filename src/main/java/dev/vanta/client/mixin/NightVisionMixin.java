package dev.vanta.client.mixin;

import dev.vanta.client.VantaClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class NightVisionMixin {
    @Inject(method = "nightVisionScale", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vanta$cleanNightVision(CallbackInfoReturnable<Float> cir) {
        if (VantaClient.MODULES.on("Night Vision Cleanup")) cir.setReturnValue(0.0F);
    }
}
