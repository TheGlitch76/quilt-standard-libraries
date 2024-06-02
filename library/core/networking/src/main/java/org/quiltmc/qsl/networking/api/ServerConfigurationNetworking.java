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

package org.quiltmc.qsl.networking.api;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;
import org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor;

/**
 * Offers access to configuration stage server-side networking functionalities.
 * <p>
 * Server-side networking functionalities include receiving server-bound packets, sending client-bound packets,
 * and events related to server-side network handlers.
 * <p>
 * This class should be only used for the logical server.
 *
 * @see ServerLoginNetworking
 * @see ServerPlayNetworking
 * @see ClientConfigurationNetworking
 */
public final class ServerConfigurationNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id)} to unregister the existing handler.
	 *
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel, otherwise {@code true}
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(CustomPayload.Id)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id, CustomChannelReceiver)
	 */
	public static <T extends CustomPayload> boolean registerGlobalReceiver(CustomPayload.Id<T> channelName, CustomChannelReceiver<T> channelHandler) {
		return ServerNetworkingImpl.CONFIGURATION.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 * <p>
	 * The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(CustomPayload.Id, CustomChannelReceiver)
	 * @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id)
	 */
	@Nullable
	public static ServerConfigurationNetworking.CustomChannelReceiver<?> unregisterGlobalReceiver(CustomPayload.Id<?> channelName) {
		return ServerNetworkingImpl.CONFIGURATION.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for
	 */
	public static Set<CustomPayload.Id<?>> getGlobalReceivers() {
		return ServerNetworkingImpl.CONFIGURATION.getChannels();
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(CustomPayload.Id, CustomChannelReceiver)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationNetworkHandler}.
	 * <p>
	 * For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(CustomPayload.Id, ServerLoginNetworking.QueryResponseReceiver)}
	 * login response has been received, you should use {@link ServerConfigurationConnectionEvents#INIT} to register the channel handler.
	 * <p>
	 * If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, CustomPayload.Id)} to unregister the existing handler.
	 *
	 * @param networkHandler the handler
	 * @param channelName    the identifier of the channel
	 * @param channelHandler the handler
	 * @return {@code false} if a handler is already registered to the channel name, otherwise {@code true}
	 * @see ServerConfigurationConnectionEvents#INIT
	 */
	public static <T extends CustomPayload> boolean registerReceiver(ServerConfigurationNetworkHandler networkHandler, CustomPayload.Id<T> channelName, CustomChannelReceiver<T> channelHandler) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).registerChannel(channelName, channelHandler);
	}

	/**
	 * Removes the handler of a channel.
	 * <p>
	 * The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the identifier of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 */
	@Nullable
	public static ServerConfigurationNetworking.CustomChannelReceiver<?> unregisterReceiver(ServerConfigurationNetworkHandler networkHandler, CustomPayload.Id<?> channelName) {
		Objects.requireNonNull(networkHandler, "Network handler cannot be null");

		return ServerNetworkingImpl.getAddon(networkHandler).unregisterChannel(channelName);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return all the channel names that the server can receive packets on
	 */
	public static Set<CustomPayload.Id<?>> getReceived(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getReceivableChannels();
	}

	/**
	 * Gets all channel names that the connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static Set<CustomPayload.Id<?>> getSendable(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels();
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler     the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel, otherwise {@code false}
	 */
	public static boolean canSend(ServerConfigurationNetworkHandler handler, CustomPayload.Id<?> channelName) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(channelName);
	}


	/**
	 * Creates a packet from a payload which may be sent to a connected client.
	 *
	 * @param payload the payload of the packet
	 * @return a new packet
	 */
	@Contract(value = "_ -> new", pure = true)
	public static Packet<ClientCommonPacketListener> createS2CPacket(@NotNull CustomPayload payload) {
		Objects.requireNonNull(payload, "Payload cannot be null");

		return ServerNetworkingImpl.createS2CPacket(payload);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the client
	 * @return the packet sender
	 */
	public static PacketSender<CustomPayload> getSender(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Server configuration packet handler cannot be null");

		return ServerNetworkingImpl.getAddon(handler);
	}

	/**
	 * Sends a packet to a client.
	 *
	 * @param networkHandler the handler to send the packet to
	 * @param payload to be sent
	 */
	public static void send(ServerConfigurationNetworkHandler networkHandler, CustomPayload payload) {
		Objects.requireNonNull(networkHandler, "Server configuration handler cannot be null");
		Objects.requireNonNull(payload, "Payload cannot be null");

		networkHandler.send(createS2CPacket(payload));
	}

	// Helper methods

	/**
	 * Returns the <i>Minecraft</i> Server of a server configuration packet handler.
	 *
	 * @param handler the server configuration packet handler
	 */
	// TODO: Possible future CHASM extension method.
	public static MinecraftServer getServer(ServerConfigurationNetworkHandler handler) {
		Objects.requireNonNull(handler, "Network handler cannot be null");

		return ((AbstractServerPacketHandlerAccessor) handler).getServer();
	}

	private ServerConfigurationNetworking() {
	}

	@FunctionalInterface
	public interface CustomChannelReceiver<T extends CustomPayload> {
		/**
		 * Receives an incoming packet.
		 * <p>
		 * This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain net.minecraft.util.thread.ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft server instance.
		 * <pre>{@code
		 * ServerConfigurationNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, handler, data, responseSender) -> {
		 * 	boolean fire = data.readBoolean();
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	server.execute(() -> {
		 *
		 *    });
		 * });
		 * }</pre>
		 *
		 * @param server         the server
		 * @param handler        the network handler that received this packet, representing the client who sent the packet
		 * @param payload        the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerConfigurationNetworkHandler handler, T payload, PacketSender<CustomPayload> responseSender);
	}
}
