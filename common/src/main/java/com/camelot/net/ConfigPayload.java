package com.camelot.net;

import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;

public record ConfigPayload(String namespace, Map<String, String> values) {

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(namespace);
        buf.writeVarInt(values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }

    public static ConfigPayload read(FriendlyByteBuf buf) {
        String namespace = buf.readUtf();
        int size = buf.readVarInt();
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            values.put(buf.readUtf(), buf.readUtf());
        }
        return new ConfigPayload(namespace, values);
    }
}
