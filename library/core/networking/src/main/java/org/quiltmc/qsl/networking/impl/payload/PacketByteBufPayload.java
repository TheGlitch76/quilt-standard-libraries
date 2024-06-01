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

package org.quiltmc.qsl.networking.impl.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;

public record PacketByteBufPayload(PacketByteBuf data, Id<?> id) implements CustomPayload {
	public static final PacketCodec<PacketByteBuf, PacketByteBufPayload> CODEC = CustomPayload.create(PacketByteBufPayload::write, PacketByteBufPayload::new);

	public PacketByteBufPayload(PacketByteBuf data){
		this(data, readId(data));
	}

	private static CustomPayload.Id<?> readId(PacketByteBuf data){
		return new Id<>(data.readIdentifier());
	}

	private void write(PacketByteBuf byteBuf) {
		byteBuf.writeBytes(this.data);
		byteBuf.writeIdentifier(id.id());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return id;
	}
}
