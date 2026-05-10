package fr.emattera.capsulecorp.capture;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ContraptionCaptureHandler {
    private static final int SOURCE_REMOVAL_FLAGS = Block.UPDATE_CLIENTS
            | Block.UPDATE_SUPPRESS_DROPS
            | Block.UPDATE_KNOWN_SHAPE;

    private ContraptionCaptureHandler() {
    }

    public record ContraptionInfo(String name, int blockCount) {
    }

    public static Optional<ContraptionInfo> inspectSableContraption(ServerLevel level, BlockPos readerPos) {
        SableBlueprint blueprint = captureSableBlueprint(level, readerPos);

        if (blueprint == null
                || blueprint.isEmpty()
                || blueprint.blockCount() <= 0
                || !hasSingleExplicitName(blueprint)
                || !capturesOnlyReaderSubLevel(level, readerPos, blueprint)) {
            return Optional.empty();
        }

        return Optional.of(new ContraptionInfo(
                SableCapsuleBlueprintHandler.getDisplayName(blueprint),
                blueprint.blockCount()));
    }

    public static SableBlueprint captureSableBlueprint(ServerLevel level, BlockPos readerPos) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, readerPos);

        if (!(subLevel instanceof ServerSubLevel serverSubLevel)) {
            return captureEmptyAt(level, readerPos);
        }

        BoundingBox3ic plotBounds = serverSubLevel.getPlot().getBoundingBox();

        if (!isValidBounds(plotBounds)) {
            return captureEmptyAt(level, readerPos);
        }

        Vec3 logicalOrigin = getLogicalOrigin(serverSubLevel);
        double radius = calculateSableLogicalCaptureRadius(plotBounds);

        return SableCapsuleBlueprintHandler.capture(
                level,
                logicalOrigin,
                radius);
    }

    public static boolean hasExplicitName(SableBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return false;
        }

        for (SableBlueprint.SubLevelData subLevel : blueprint.subLevels()) {
            if (hasName(subLevel)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasSingleExplicitName(SableBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return false;
        }

        String expectedName = null;

        for (SableBlueprint.SubLevelData subLevel : blueprint.subLevels()) {
            String name = subLevel.name();

            if (name == null || name.isBlank()) {
                return false;
            }

            if (expectedName == null) {
                expectedName = name;
                continue;
            }

            if (!expectedName.equals(name)) {
                return false;
            }
        }

        return expectedName != null;
    }

    public static boolean capturesOnlyReaderSubLevel(
            ServerLevel level,
            BlockPos readerPos,
            SableBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return false;
        }

        SubLevel readerSubLevel = Sable.HELPER.getContaining(level, readerPos);

        if (!(readerSubLevel instanceof ServerSubLevel)) {
            return false;
        }

        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null) {
            return false;
        }

        boolean foundCapturedSubLevel = false;

        for (SableBlueprint.SubLevelData subLevelData : blueprint.subLevels()) {
            SubLevel capturedSourceSubLevel = container.getSubLevel(subLevelData.sourceUuid());

            if (capturedSourceSubLevel == null) {
                continue;
            }

            foundCapturedSubLevel = true;

            if (capturedSourceSubLevel != readerSubLevel) {
                return false;
            }
        }

        return foundCapturedSubLevel;
    }

    public static void removeCapturedSableBlueprintSource(ServerLevel level, SableBlueprint blueprint) {
        if (blueprint == null) {
            return;
        }

        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null) {
            return;
        }

        Set<UUID> sourceUuids = new LinkedHashSet<>();

        for (SableBlueprint.SubLevelData subLevelData : blueprint.subLevels()) {
            SubLevel sourceSubLevel = container.getSubLevel(subLevelData.sourceUuid());

            if (!(sourceSubLevel instanceof ServerSubLevel serverSubLevel)) {
                continue;
            }

            BoundingBox3ic bounds = serverSubLevel.getPlot().getBoundingBox();

            if (!isValidBounds(bounds)) {
                continue;
            }

            sourceUuids.add(subLevelData.sourceUuid());

            serverSubLevel.deleteAllEntities();
            removeBlockEntitiesInBounds(level, bounds);
            removeBlocksInBounds(level, bounds);
            removeDroppedItemsInBounds(level, bounds);
        }

        removeSourceSubLevels(container, sourceUuids);
    }

    private static Vec3 getLogicalOrigin(ServerSubLevel serverSubLevel) {
        org.joml.Vector3dc logicalPosition = serverSubLevel.logicalPose().position();

        return new Vec3(
                logicalPosition.x(),
                logicalPosition.y(),
                logicalPosition.z());
    }

    private static void removeSourceSubLevels(ServerSubLevelContainer container, Set<UUID> sourceUuids) {
        for (UUID sourceUuid : sourceUuids) {
            SubLevel sourceSubLevel = container.getSubLevel(sourceUuid);

            if (sourceSubLevel instanceof ServerSubLevel serverSubLevel) {
                container.removeSubLevel(
                        serverSubLevel,
                        SubLevelRemovalReason.REMOVED);
            }
        }
    }

    private static SableBlueprint captureEmptyAt(ServerLevel level, BlockPos pos) {
        return SableCapsuleBlueprintHandler.capture(
                level,
                Vec3.atCenterOf(pos),
                0.0D);
    }

    private static void removeBlockEntitiesInBounds(ServerLevel level, BoundingBox3ic bounds) {
        forEachPositionInBounds(bounds, pos -> {
            if (level.getBlockEntity(pos) != null) {
                level.removeBlockEntity(pos);
            }
        });
    }

    private static void removeBlocksInBounds(ServerLevel level, BoundingBox3ic bounds) {
        forEachPositionInBounds(bounds, pos -> {
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(
                        pos,
                        Blocks.AIR.defaultBlockState(),
                        SOURCE_REMOVAL_FLAGS);
            }
        });
    }

    private static void forEachPositionInBounds(BoundingBox3ic bounds, java.util.function.Consumer<BlockPos> action) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    action.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    private static void removeDroppedItemsInBounds(ServerLevel level, BoundingBox3ic bounds) {
        AABB area = new AABB(
                bounds.minX(),
                bounds.minY(),
                bounds.minZ(),
                bounds.maxX() + 1.0D,
                bounds.maxY() + 1.0D,
                bounds.maxZ() + 1.0D);

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area)) {
            itemEntity.discard();
        }
    }

    private static double calculateSableLogicalCaptureRadius(BoundingBox3ic bounds) {
        if (!isValidBounds(bounds)) {
            return 8.0D;
        }

        int sizeX = Math.max(1, bounds.maxX() - bounds.minX() + 1);
        int sizeY = Math.max(1, bounds.maxY() - bounds.minY() + 1);
        int sizeZ = Math.max(1, bounds.maxZ() - bounds.minZ() + 1);

        double diagonal = Math.sqrt(
                sizeX * sizeX
                        + sizeY * sizeY
                        + sizeZ * sizeZ);

        return diagonal / 2.0D;
    }

    private static boolean hasName(SableBlueprint.SubLevelData subLevel) {
        String name = subLevel.name();
        return name != null && !name.isBlank();
    }

    private static boolean isValidBounds(BoundingBox3ic bounds) {
        return bounds != null && bounds.volume() > 0;
    }
}