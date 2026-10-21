package srpmixins.mixin.forgottenconfigs;

import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.EntityUnvo;
import com.dhanantry.scapeandrunparasites.util.config.SRPConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// fixed in SRP 1.10.x
@Mixin(EntityUnvo.class)
public class SentryXPValue {
    @ModifyExpressionValue(
            method = "<init>",
            at = @At(value = "FIELD", target = "Lcom/dhanantry/scapeandrunparasites/util/SRPAttributes;XP_ADAPTED:I"),
            remap = false
    )
    private int srpmixins_usePreeminentRemainValue(int original){
        return SRPConfig.turretXPValue;
    }
}
