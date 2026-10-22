package srpmixins.mixin.parabiome;

import com.dhanantry.scapeandrunparasites.network.SRPPacketFog;
import com.dhanantry.scapeandrunparasites.util.handlers.SRPEventHandlerBus;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import srpmixins.client.SRPFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SRPPacketFog.Handler.class)
public abstract class SRPFogPacketMixin {
    //When the client receives SRPs fog density packet, also pass the new target density to SrpFogState, which fades towards it smoothly
    @Inject(
            method = "handle",
            at = @At(value = "TAIL"),
            remap = false
    )
    private void srpmixins$receiveFog(SRPPacketFog message, MessageContext ctx, CallbackInfo ci) {
        SRPFog.State.receive(SRPEventHandlerBus.fog);
    }
}
