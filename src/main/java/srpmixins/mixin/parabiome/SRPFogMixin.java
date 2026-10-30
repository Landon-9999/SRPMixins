package srpmixins.mixin.parabiome;

import com.dhanantry.scapeandrunparasites.util.config.SRPConfigWorld;
import com.dhanantry.scapeandrunparasites.util.handlers.SRPEventHandlerBus;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import srpmixins.client.SRPFog;
import srpmixins.config.SRPMixinsConfigHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SRPEventHandlerBus.class)
public abstract class SRPFogMixin {
    //SRP hard-switches to the parasite fog color above 20% density. Instead, gradually blend from the current fog color (vanilla or other mods) to the parasite color as density rises
    @WrapMethod(method = "onEvent(Lnet/minecraftforge/client/event/EntityViewRenderEvent$FogColors;)V", remap = false)
    private void srpmixins$blendFogColor(EntityViewRenderEvent.FogColors event, Operation<Void> original) {
        if (!SRPFog.terrainFogEnabled()) return;
        if (!SRPMixinsConfigHandler.parabiome.smoothBiomeFog) {
            original.call(event);
            return;
        }
        if (srpmixins$canApplyFog(event)) {
            float weight = SRPFog.Transition.colorWeight(SRPFog.State.density(), SRPConfigWorld.biomeFogDensity);
            event.setRed(event.getRed() + (SRPConfigWorld.biomeFogRed / 255.0F - event.getRed()) * weight);
            event.setGreen(event.getGreen() + (SRPConfigWorld.biomeFogGreen / 255.0F - event.getGreen()) * weight);
            event.setBlue(event.getBlue() + (SRPConfigWorld.biomeFogBlue / 255.0F - event.getBlue()) * weight);
        }
        //don't run original
    }

    @WrapMethod(method = "onEvent(Lnet/minecraftforge/client/event/EntityViewRenderEvent$FogDensity;)V", remap = false)
    private void srpmixins$applySmoothDensity(EntityViewRenderEvent.FogDensity event, Operation<Void> original) {
        if (!SRPFog.terrainFogEnabled()) return;
        if (!SRPMixinsConfigHandler.parabiome.smoothBiomeFog) {
            original.call(event);
            return;
        }
        float density = SRPFog.State.density();
        if (density > 0.0F && !event.isCanceled() && srpmixins$canApplyFog(event)) {
            GlStateManager.setFog(GlStateManager.FogMode.EXP);
            event.setDensity(density);
            event.setCanceled(true);
        }
    }

    @Unique
    private static boolean srpmixins$canApplyFog(EntityViewRenderEvent event) {
        return !event.getState().getMaterial().isLiquid()
                && !(event.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) event.getEntity()).isPotionActive(MobEffects.BLINDNESS));
    }
}
