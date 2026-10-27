package srpmixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import srpmixins.capability.adaptation.CapabilityAdaptationHandler;

@Mixin(Item.class)
public abstract class SyncAdaptation {
	/**
	 * Attaches the adaptation capability to a copy of the share tag to sync it to the client
	 */
	@ModifyReturnValue(
			method = "getNBTShareTag",
			at = @At("RETURN"),
			remap = false
	)
	private NBTTagCompound srpmixins_sendAdaptation(NBTTagCompound original, ItemStack stack) {
		return CapabilityAdaptationHandler.writeSyncTag(stack, original);
	}

	/**
	 * Reads the adaptation capability from the share tag and strips it before it gets set as the stack's tag
	 */
	@ModifyVariable(
			method = "readNBTShareTag",
			at = @At("HEAD"),
			argsOnly = true,
			remap = false
	)
	private NBTTagCompound srpmixins_readAdaptation(NBTTagCompound nbt, ItemStack stack) {
		return CapabilityAdaptationHandler.readSyncTag(stack, nbt);
	}
}
