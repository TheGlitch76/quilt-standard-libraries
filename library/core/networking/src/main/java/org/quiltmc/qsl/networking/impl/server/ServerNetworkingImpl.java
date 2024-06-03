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

package org.quiltmc.qsl.networking.impl.server;

import net.minecraft.network.NetworkSide;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.GlobalReceiverRegistry;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.PayloadTypeRegistryImpl;

@ApiStatus.Internal
public final class ServerNetworkingImpl {
	public static final GlobalReceiverRegistry<ServerLoginNetworking.QueryResponseReceiver> LOGIN = new GlobalReceiverRegistry<>(NetworkSide.C2S, NetworkState.LOGIN, null);
	public static final GlobalReceiverRegistry<ServerConfigurationNetworking.CustomChannelReceiver<?>> CONFIGURATION = new GlobalReceiverRegistry<>(NetworkSide.C2S, NetworkState.CONFIGURATION, PayloadTypeRegistryImpl.CONFIGURATION_C2S);
	public static final GlobalReceiverRegistry<ServerPlayNetworking.CustomChannelReceiver<?>> PLAY = new GlobalReceiverRegistry<>(NetworkSide.C2S, NetworkState.PLAY, PayloadTypeRegistryImpl.PLAY_C2S);

	public static ServerPlayNetworkAddon getAddon(ServerPlayNetworkHandler handler) {
		return (ServerPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ServerConfigurationNetworkAddon getAddon(ServerConfigurationNetworkHandler handler) {
		return (ServerConfigurationNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ServerLoginNetworkAddon getAddon(ServerLoginNetworkHandler handler) {
		return (ServerLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ClientCommonPacketListener> createS2CPacket(CustomPayload payload) {
		return new CustomPayloadS2CPacket(payload);
	}
}
