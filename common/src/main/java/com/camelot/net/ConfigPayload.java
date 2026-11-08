package com.camelot.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public record ConfigPayload(String namespace, Map<String, String> values) implements CustomPacketPayload {

    public static final Type<ConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("camelot", "config"));

    public static final StreamCodec<FriendlyByteBuf, ConfigPayload> CODEC =
            StreamCodec.of((buf, payload) -> payload.write(buf), ConfigPayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(namespace);
        buf.writeVarInt(values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }

    private static ConfigPayload read(FriendlyByteBuf buf) {
        String namespace = buf.readUtf();
        int size = buf.readVarInt();
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            values.put(buf.readUtf(), buf.readUtf());
        }
        return new ConfigPayload(namespace, values);
    }
}
