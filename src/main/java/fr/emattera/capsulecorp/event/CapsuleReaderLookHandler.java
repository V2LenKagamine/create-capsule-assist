package fr.emattera.capsulecorp.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fr.emattera.capsulecorp.CapsuleCorp;
import fr.emattera.capsulecorp.block.CapsuleReaderBlock;
import fr.emattera.capsulecorp.capture.ContraptionCaptureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CapsuleCorp.MOD_ID)
public final class CapsuleReaderLookHandler {
    private static final double LOOK_DISTANCE = 6.0D;
    private static final int INFO_CACHE_DURATION_TICKS = 40;

    private static final Map<ReaderCacheKey, ReaderInfoCache> READER_INFO_CACHE = new HashMap<>();
    private static final Map<UUID, ReaderCacheKey> PLAYER_LAST_READER = new HashMap<>();

    private CapsuleReaderLookHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            clearMessage(player);
            return;
        }

        Optional<BlockPos> readerPos = getLookedAtReaderPos(player, level);

        if (readerPos.isEmpty()) {
            clearMessage(player);
            return;
        }

        showReaderInfo(player, level, readerPos.get());
    }

    private static Optional<BlockPos> getLookedAtReaderPos(ServerPlayer player, ServerLevel level) {
        HitResult hitResult = player.pick(
                LOOK_DISTANCE,
                0.0F,
                false);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return Optional.empty();
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof CapsuleReaderBlock)) {
            return Optional.empty();
        }

        return Optional.of(blockPos);
    }

    private static void showReaderInfo(ServerPlayer player, ServerLevel level, BlockPos readerPos) {
        ReaderCacheKey key = new ReaderCacheKey(level.dimension(), readerPos.immutable());
        PLAYER_LAST_READER.put(player.getUUID(), key);

        ReaderInfoCache cache = READER_INFO_CACHE.get(key);

        if (cache == null || player.tickCount - cache.lastRefreshTick() >= INFO_CACHE_DURATION_TICKS) {
            Optional<ContraptionCaptureHandler.ContraptionInfo> info =
                    ContraptionCaptureHandler.inspectSableContraption(level, readerPos);

            if (info.isEmpty()) {
                clearMessage(player);
                READER_INFO_CACHE.remove(key);
                return;
            }

            Component message = Component.translatable(
                    "message.capsulecorp.reader.look_info",
                    info.get().name(),
                    info.get().blockCount());

            cache = new ReaderInfoCache(message, player.tickCount);
            READER_INFO_CACHE.put(key, cache);
        }

        player.displayClientMessage(cache.message(), true);
    }

    private static void clearMessage(ServerPlayer player) {
        UUID playerId = player.getUUID();
        ReaderCacheKey previousReader = PLAYER_LAST_READER.remove(playerId);

        if (previousReader != null) {
            player.displayClientMessage(Component.empty(), true);
            removeUnusedReaderCache(previousReader);
        }
    }

    private static void removeUnusedReaderCache(ReaderCacheKey key) {
        if (!PLAYER_LAST_READER.containsValue(key)) {
            READER_INFO_CACHE.remove(key);
        }
    }

    private record ReaderCacheKey(
            ResourceKey<Level> dimension,
            BlockPos pos) {
    }

    private record ReaderInfoCache(
            Component message,
            int lastRefreshTick) {
    }
}