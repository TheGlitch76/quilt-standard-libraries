package org.quiltmc.qsl.tag.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.ServerRegistryLayer;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.qsl.tag.impl.client.ClientRegistryStatus;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@Mixin(ReloadableRegistries.class)
public abstract class ReloadableRegistriesMixin {

	@Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/ReloadableRegistries$TagAwareLookupWrapper;<init>(Lnet/minecraft/registry/DynamicRegistryManager;)V"))
	private static void onLoad(LayeredRegistryManager<ServerRegistryLayer> registryManager, ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<LayeredRegistryManager<ServerRegistryLayer>>> cir, @Local DynamicRegistryManager.Frozen registry){
		ClientTagRegistryManager.applyAll(registry, ClientRegistryStatus.LOCAL);
	}
}
