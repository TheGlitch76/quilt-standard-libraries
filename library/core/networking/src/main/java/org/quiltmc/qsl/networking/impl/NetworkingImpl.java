/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.networking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.login.payload.CustomQueryPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.ServerLoginNetworkHandlerAccessor;

@ApiStatus.Internal
public final class NetworkingImpl {
	public static final String MOD_ID = "quilt_networking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	/**
	 * Identifier of packet used to register supported channels.
	 */
	public static final CustomPayload.Id<ChannelPayload.RegisterChannelPayload> REGISTER_CHANNEL = CustomPayload.create("register");
	/**
	 * Identifier of packet used to unregister supported channels.
	 */
	public static final CustomPayload.Id<ChannelPayload.UnregisterChannelPayload> UNREGISTER_CHANNEL = CustomPayload.create("unregister");
	/**
	 * Identifier of the packet used to declare all currently supported channels.
	 * Dynamic registration of supported channels is still allowed using {@link NetworkingImpl#REGISTER_CHANNEL} and {@link NetworkingImpl#UNREGISTER_CHANNEL}.
	 */
	public static final CustomPayload.Id<?> EARLY_REGISTRATION_CHANNEL = new CustomPayload.Id<>(new Identifier(MOD_ID, "early_registration"));
	/**
	 * Identifier of the packet used to declare all currently supported channels.
	 * Dynamic registration of supported channels is still allowed using {@link NetworkingImpl#REGISTER_CHANNEL} and {@link NetworkingImpl#UNREGISTER_CHANNEL}.
	 *
	 * <p>Since our early registration packet does not differ from fabric's, we can support both.
	 */
	public static final CustomPayload.Id<?> EARLY_REGISTRATION_CHANNEL_FABRIC = new CustomPayload.Id<>(new Identifier("fabric-networking-api-v1", "early_registration"));

	/**
	 * Forces reserialization of packets.
	 */
	// TODO: Remove for 1.20.5. This is done there already.
	public static final boolean RESERIALIZE_CUSTOM_PAYLOADS = Boolean.parseBoolean(System.getProperty("quilt.networking.reserialize_custom_payloads", "false"));

	public static void init(ModContainer mod) {
		LOGGER.info("quilt.networking.reserialize_custom_payloads set to {}", RESERIALIZE_CUSTOM_PAYLOADS);

		// Login setup
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			// Send early registration packet
			PacketByteBuf buf = PacketByteBufs.create();
			Collection<CustomPayload.Id<?>> channelsNames = ServerPlayNetworking.getGlobalReceivers();
			buf.writeVarInt(channelsNames.size());

			for (CustomPayload.Id<?> id : channelsNames) {
				buf.writeIdentifier(id.id());
			}

			sender.sendPacket(EARLY_REGISTRATION_CHANNEL, buf);
			sender.sendPacket(EARLY_REGISTRATION_CHANNEL_FABRIC, buf);
			NetworkingImpl.LOGGER.debug("Sent accepted channels to the client for \"{}\"", handler.getConnectionInfo());
		});

		ServerLoginNetworking.registerGlobalReceiver(EARLY_REGISTRATION_CHANNEL, NetworkingImpl::receiveEarlyRegistration);
		ServerLoginNetworking.registerGlobalReceiver(EARLY_REGISTRATION_CHANNEL_FABRIC, NetworkingImpl::receiveEarlyRegistration);

		CustomPayloads.registerS2CPayload(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		CustomPayloads.registerS2CPayload(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
		CustomPayloads.registerC2SPayload(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		CustomPayloads.registerC2SPayload(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
	}

	public static boolean isReservedCommonChannel(CustomPayload.Id<?> channelName) {
		return channelName.equals(REGISTER_CHANNEL) || channelName.equals(UNREGISTER_CHANNEL);
	}

	private static void receiveEarlyRegistration(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender<CustomQueryPayload> sender) {
		if (!understood) {
			// The client is likely a vanilla client.
			return;
		}

		int n = buf.readVarInt();
		List<CustomPayload.Id<?>> ids = new ArrayList<>(n);

		for (int i = 0; i < n; i++) {
			ids.add(new CustomPayload.Id<>(buf.readIdentifier()));
		}

		((ChannelInfoHolder) ((ServerLoginNetworkHandlerAccessor) handler).getConnection()).getPendingChannelsNames(NetworkState.LOGIN).addAll(ids);
		NetworkingImpl.LOGGER.debug("Received accepted channels from the client for \"{}\"", handler.getConnectionInfo());
	}
}
