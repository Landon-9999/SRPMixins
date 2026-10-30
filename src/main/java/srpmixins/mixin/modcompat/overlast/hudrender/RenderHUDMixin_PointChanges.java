package srpmixins.mixin.modcompat.overlast.hudrender;

import com.llamalad7.mixinextras.sugar.Local;
import com.overlast.gui.RenderHUD;
import com.overlast.gui.StatBar;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import srpmixins.compat.overlast.PointChangeHandler;

@Mixin(RenderHUD.class)
public abstract class RenderHUDMixin_PointChanges {
    @Shadow(remap = false) @Final private static StatBar EVOLUTION_BAR;

    @Inject(
            method = "renderOverlay",
            at = @At(value = "INVOKE", target = "Lcom/overlast/gui/RenderHUD;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER)
    )
    private void srpmixins_renderPointChanges(RenderGameOverlayEvent event, CallbackInfo ci, @Local(name = "bar") StatBar bar, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "fullWidth") int fullWidth){
        //OverLast renders on Pre, Text and Post of the TEXT element
        if(bar != EVOLUTION_BAR || !(event instanceof RenderGameOverlayEvent.Post)) return;
        //Phase circle is at the left end of the bar texture
        PointChangeHandler.PointChangePopups.render(Minecraft.getMinecraft().fontRenderer, x - fullWidth + 5, y + 12, y + 23);
    }
}
