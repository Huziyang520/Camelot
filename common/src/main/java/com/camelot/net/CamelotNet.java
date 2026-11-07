package com.camelot.net;

import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Config synchronisation shared by every mod that bundles this library.
 *
 * <p>Server → client: the whole set of syncable values, sent on join, after every
 * accepted update and after the file was re-read. Client → server: the same set,
 * accepted only from game-master permission holders, then persisted and echoed.
 */
public final class CamelotNet {

    private static final Map<String, Consumer<MinecraftServer>> AFTER_APPLY = new ConcurrentHashMap<>();
    private static final Set<String> SYNCED = ConcurrentHashMap.newKeySet();

    private static volatile NetSender sender;

    private CamelotNet() {
    }

    /**
     * Registers a config for synchronisation.
     *
     * @param config     the mod's config
     * @param afterApply run on the server whenever the values changed, e.g. to refresh
     *                   data derived from them (may be null)
     */
    public static void register(CamelotConfig config, Consumer<MinecraftServer> afterApply) {
        CamelotConfigs.register(config);
        if (afterApply != null) {
            AFTER_APPLY.put(config.namespace(), afterApply);
        }
    }

    public static void setSender(NetSender netSender) {
        sender = netSender;
    }

    /** Sends every registered config to one player. */
    public static void syncTo(ServerPlayer player) {
        NetSender net = sender;
        if (net == null || player == null || !net.canReceive(player)) {
            return;
        }
        for (CamelotConfig config : CamelotConfigs.all()) {
            net.toPlayer(player, new ConfigPayload(config.namespace(), config.snapshotSynced()));
        }
    }

    /** Re-sends every registered config to all online players. */
    public static void syncToAll(MinecraftServer server) {
        NetSender net = sender;
        if (net == null || server == null) {
            return;
        }
        for (CamelotConfig config : CamelotConfigs.all()) {
            ConfigPayload payload = new ConfigPayload(config.namespace(), config.snapshotSynced());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (net.canReceive(player)) {
                    net.toPlayer(player, payload);
                }
            }
        }
    }

    /** Pushes local edits to the server (client side only); no-op when not in a world. */
    public static void sendUpdate(CamelotConfig config) {
        NetSender net = sender;
        if (net != null) {
            net.toServer(new ConfigPayload(config.namespace(), config.snapshotSynced()));
        }
    }

    /** Values arrived from the server. */
    public static void onClientPayload(ConfigPayload payload) {
        CamelotConfig config = CamelotConfigs.get(payload.namespace());
        if (config != null) {
            config.applyRemote(payload.values());
            SYNCED.add(payload.namespace());
        }
    }

    /** Whether the server already sent this config's values to this client. */
    public static boolean isSynced(String namespace) {
        return SYNCED.contains(namespace);
    }

    /** Values arrived from a client: validate, persist and echo to everyone. */
    public static void onServerPayload(ConfigPayload payload, ServerPlayer player) {
        if (player == null || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }
        CamelotConfig config = CamelotConfigs.get(payload.namespace());
        if (config == null) {
            return;
        }
        config.applyRemote(payload.values());
        config.save();
        MinecraftServer server = player.level().getServer();
        syncToAll(server);
        notifyApplied(config, server);
    }

    /** The config file changed on disk: push the new values out and refresh derived data. */
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
