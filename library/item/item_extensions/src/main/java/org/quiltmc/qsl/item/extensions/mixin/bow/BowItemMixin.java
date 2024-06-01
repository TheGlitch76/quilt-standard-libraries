package org.quiltmc.qsl.item.extensions.mixin.bow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;

@Mixin(BowItem.class)
public abstract class BowItemMixin implements BowExtensions {

	// Modifies the pull progress if a custom bow is used
	@Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
	private float redirectPullProgress(int useTicks, ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks) {
		return this.getCustomPullProgress(useTicks, bowStack);
	}
}
