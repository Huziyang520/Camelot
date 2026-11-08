package com.camelot.net;

import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigScope;
import com.camelot.platform.Perms;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CamelotNet {

    private static final Map<String, Consumer<MinecraftServer>> AFTER_APPLY = new ConcurrentHashMap<>();
    private static final Set<String> SYNCED = ConcurrentHashMap.newKeySet();

    private static volatile NetSender sender;

    private CamelotNet() {
    }

    public static void register(CamelotConfig config, Consumer<MinecraftServer> afterApply) {
        CamelotConfigs.register(config);
        if (afterApply != null) {
            AFTER_APPLY.put(config.namespace(), afterApply);
        }
    }

    public static void setSender(NetSender netSender) {
        sender = netSender;
    }

    public static void syncTo(ServerPlayer player) {
        NetSender net = sender;
        if (net == null || player == null || !net.canReceive(player)) {
            return;
        }
        for (CamelotConfig config : CamelotConfigs.all()) {
            if (config.synced()) {
                net.toPlayer(player, new ConfigPayload(config.namespace(), config.snapshot()));
            }
        }
    }

    public static void syncToAll(MinecraftServer server) {
        NetSender net = sender;
        if (net == null || server == null) {
            return;
        }
        for (CamelotConfig config : CamelotConfigs.all()) {
            if (!config.synced()) {
                continue;
            }
            ConfigPayload payload = new ConfigPayload(config.namespace(), config.snapshot());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (net.canReceive(player)) {
                    net.toPlayer(player, payload);
                }
            }
        }
    }

    public static void sendUpdate(CamelotConfig config) {
        NetSender net = sender;
        if (net != null && config.synced()) {
            net.toServer(new ConfigPayload(config.namespace(), config.snapshot()));
        }
    }

    public static void onClientPayload(ConfigPayload payload) {
        CamelotConfig config = CamelotConfigs.get(payload.namespace(), ConfigScope.COMMON);
        if (config != null) {
            config.applyRemote(payload.values());
            SYNCED.add(payload.namespace());
        }
    }

    public static boolean isSynced(String namespace) {
        return SYNCED.contains(namespace);
    }

    public static void onServerPayload(ConfigPayload payload, ServerPlayer player) {
        if (player == null || !Perms.canEdit(player)) {
            return;
        }
        CamelotConfig config = CamelotConfigs.get(payload.namespace(), ConfigScope.COMMON);
        if (config == null) {
            return;
        }
        config.applyRemote(payload.values());
        config.save();
        MinecraftServer server = player.level().getServer();
        syncToAll(server);
        notifyApplied(config, server);
    }

    public static void onServerReload(MinecraftServer server) {
        syncToAll(server);
        for (CamelotConfig config : CamelotConfigs.all()) {
            notifyApplied(config, server);
        }
    }

    private static void notifyApplied(CamelotConfig config, MinecraftServer server) {
        Consumer<MinecraftServer> action = AFTER_APPLY.get(config.namespace());
        if (action != null) {
            action.accept(server);
        }
    }
}
