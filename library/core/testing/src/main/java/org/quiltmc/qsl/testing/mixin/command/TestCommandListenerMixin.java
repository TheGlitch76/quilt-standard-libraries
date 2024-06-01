package org.quiltmc.qsl.testing.mixin.command;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.server.command.dev.TestCommand;

@Mixin(TestCommand.Listener.class)
public class TestCommandListenerMixin {
	@ModifyConstant(
		method = "method_56304",
		constant = @Constant(stringValue = "All required tests passed :)")
	)
	private static String quiltGameTest$replaceSuccessMessage(String original) {
		// You may ask why, it's simple.
		// The original emoticon is a bit... weird.
		// And QSL members expressed some kind of interest into replacing it.
		// So here it is. I assure you this is a really necessary injection.
		return "All required tests passed :3c";
	}
}
