/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.networking.mixin;

import java.util.Map;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.payload.DiscardedCustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;

// TODO cleanup. What's still needed?
@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {

	@Dynamic("method_56460: CustomPayload.create in <clinit>")
	@WrapOperation(method = {"method_56460", "method_56461"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/payload/DiscardedCustomPayload;createCodec(Lnet/minecraft/util/Identifier;I)Lnet/minecraft/network/codec/PacketCodec;"))
	private static PacketCodec<PacketByteBuf, ? extends CustomPayload> addCustomTypes(Identifier identifier, int sizeLimit, Operation<PacketCodec<PacketByteBuf, DiscardedCustomPayload>> original) {
		Optional<PacketCodec<PacketByteBuf, ? extends CustomPayload>> optional = CustomPayloads.S2C_TYPES.keySet()
			.stream().filter(id -> id.id().equals(identifier))
			.findAny().map(CustomPayloads.S2C_TYPES::get);
		if (optional.isPresent()){
			return optional.get();
		}
		NetworkingImpl.LOGGER.info("[S2C] Did not find custom payload for "+identifier);
		return original.call(identifier, sizeLimit);
	}

	/*@Inject(method = "readPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;readUnknownPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/payload/DiscardedCustomPayload;"), cancellable = true)
	private static void inject(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<PacketByteBufPayload> cir) {
		PacketByteBuf copied = PacketByteBufs.copy(buf);
		cir.setReturnValue(new PacketByteBufPayload(id, copied));
		buf.skipBytes(buf.readableBytes());
	}

	@ModifyArg(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/listener/ClientCommonPacketListener;onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V"))
	public CustomPayloadS2CPacket reserialize(CustomPayloadS2CPacket packet) {
		if (NetworkingImpl.RESERIALIZE_CUSTOM_PAYLOADS) {
			PacketByteBuf buf = PacketByteBufs.create();
			packet.write(buf);
			return new CustomPayloadS2CPacket(buf);
		}

		return packet;
	}*/
}
