package srpmixins.mixin.paradmgfix;

import com.dhanantry.scapeandrunparasites.entity.projectile.EntityProjectileSpineball;
import com.dhanantry.scapeandrunparasites.entity.projectile.EntitySRPProjectile;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// fixed in SRP 1.10.x
@Mixin(EntityProjectileSpineball.class)
public abstract class SpineballArmorDamageFix extends EntitySRPProjectile {
    public SpineballArmorDamageFix(World worldIn) {
        super(worldIn);
    }

    @Shadow(remap = false) private double item;
    @Shadow(remap = false) private void damageArmor(EntityLivingBase target, double percentage) {
        throw new AssertionError("Failed to shadow EntityProjectileSpineball.damageArmor");
    }

    // fix spineballs doing durability damage even if hits failed to hurt
    @WrapWithCondition(
            method = "onImpact",
            at = @At(value = "INVOKE", target = "Lcom/dhanantry/scapeandrunparasites/entity/projectile/EntityProjectileSpineball;damageArmor(Lnet/minecraft/entity/EntityLivingBase;D)V", remap = false)
    )
    private boolean srpmixins_preventArmorDamageAtOriginalLocation(EntityProjectileSpineball instance, EntityLivingBase entity, double damage) {
        return false;
    }

    @ModifyExpressionValue(
            method = "onImpact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z")
    )
    private boolean srpmixins_damageArmorOnlyIfHitSucceeded(boolean hitSucceeded, @Local(name = "target") EntityLivingBase target) {
        if (hitSucceeded)
            this.damageArmor(target, this.item);
        return false;
    }
}
