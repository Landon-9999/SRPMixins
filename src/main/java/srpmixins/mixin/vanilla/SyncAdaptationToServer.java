package srpmixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.PacketUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import srpmixins.capability.adaptation.CapabilityAdaptationHandler;

@Mixin(PacketUtil.class)
public abstract class SyncAdaptationToServer {
	/**
	 * Client to server item packets (creative inventory, window clicks) send the raw tag instead of the share tag.
	 * Attach the adaptation capability so creative doesn't wipe it and clicked stacks match the server's share tag
	 */
	@WrapOperation(
			method = "writeItemStackFromClientToServer",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;", remap = true),
			remap = false
	)
	private static NBTTagCompound srpmixins_sendAdaptationToServer(ItemStack stack, Operation<NBTTagCompound> original) {
		return CapabilityAdaptationHandler.writeSyncTag(stack, original.call(stack));
	}
}
