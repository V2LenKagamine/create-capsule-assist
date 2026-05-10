package fr.emattera.capsulecorp.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintPlacer;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import fr.emattera.capsulecorp.capture.SableCapsuleBlueprintHandler;
import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.serialization.CapsuleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ContraptionDeploymentHandler {
    private ContraptionDeploymentHandler() {
    }

    public record DeploymentResult(boolean success, List<UUID> placedSubLevelUuids) {
        public DeploymentResult {
            placedSubLevelUuids = List.copyOf(placedSubLevelUuids);
        }

        public static DeploymentResult failure() {
            return new DeploymentResult(false, List.of());
        }
    }

    public static DeploymentResult deployFromCapsuleWithResult(
            Level level,
            BlockPos deployPos,
            ItemStack sourceCapsuleStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return DeploymentResult.failure();
        }

        if (sourceCapsuleStack == null
                || sourceCapsuleStack.isEmpty()
                || !CapsuleData.hasSableBlueprint(sourceCapsuleStack)) {
            return DeploymentResult.failure();
        }

        return deploySableBlueprintWithResult(
                serverLevel,
                deployPos,
                sourceCapsuleStack);
    }

    public static void stabilizeSubLevels(ServerLevel level, Collection<UUID> subLevelUuids) {
        if (subLevelUuids == null || subLevelUuids.isEmpty()) {
            return;
        }

        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null) {
            return;
        }

        PhysicsPipeline pipeline = container.physicsSystem().getPipeline();

        for (UUID uuid : subLevelUuids) {
            SubLevel subLevel = container.getSubLevel(uuid);

            if (!(subLevel instanceof ServerSubLevel serverSubLevel)) {
                continue;
            }

            pipeline.resetVelocity(serverSubLevel);

            serverSubLevel.latestLinearVelocity.zero();
            serverSubLevel.latestAngularVelocity.zero();
            serverSubLevel.setLastNetworkedStopped(true);
            serverSubLevel.updateLastPose();
        }
    }

    private static DeploymentResult deploySableBlueprintWithResult(
            ServerLevel level,
            BlockPos deployPos,
            ItemStack sourceCapsuleStack) {
        try {
            SableBlueprint blueprint = CapsuleData.getSableBlueprint(sourceCapsuleStack);

            if (blueprint == null || blueprint.isEmpty()) {
                return DeploymentResult.failure();
            }

            BlockPos adjustedDeployPos = adjustDeployPositionAboveGround(deployPos, blueprint);

            BlockPos validDeployPos = findValidDeployPosition(
                    level,
                    adjustedDeployPos,
                    blueprint,
                    CapsuleCorpConfig.deploySearchRadius());

            if (validDeployPos == null) {
                return DeploymentResult.failure();
            }

            Vec3 origin = Vec3.atCenterOf(validDeployPos);

            SableBlueprintPlacer.Result result = SableCapsuleBlueprintHandler.deploy(
                    level,
                    blueprint,
                    origin);

            List<UUID> placedUuids = new ArrayList<>(result.subLevelUuidMap().values());

            stabilizeSubLevels(level, placedUuids);

            return new DeploymentResult(
                    result.placedSubLevels() > 0,
                    placedUuids);
        } catch (RuntimeException exception) {
            return DeploymentResult.failure();
        }
    }

    private static BlockPos findValidDeployPosition(
            ServerLevel level,
            BlockPos origin,
            SableBlueprint blueprint,
            int maxRadius) {
        if (canDeployBlueprintAt(level, origin, blueprint)) {
            return origin;
        }

        for (int radius = 1; radius <= maxRadius; radius++) {
            BlockPos validPosition = findValidDeployPositionAtRadius(
                    level,
                    origin,
                    blueprint,
                    radius);

            if (validPosition != null) {
                return validPosition;
            }
        }

        return null;
    }

    private static BlockPos findValidDeployPositionAtRadius(
            ServerLevel level,
            BlockPos origin,
            SableBlueprint blueprint,
            int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z)) != radius) {
                        continue;
                    }

                    BlockPos candidate = origin.offset(x, y, z);

                    if (canDeployBlueprintAt(level, candidate, blueprint)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static boolean canDeployBlueprintAt(
            ServerLevel level,
            BlockPos deployPos,
            SableBlueprint blueprint) {
        for (SableBlueprint.SubLevelData subLevel : blueprint.subLevels()) {
            if (!hasValidBounds(subLevel.localBounds())) {
                continue;
            }

            BlockPos localCenter = getLocalBoundsCenter(subLevel.localBounds());

            for (SableBlueprint.BlockData blockData : subLevel.blocks()) {
                BlockPos localPos = blockData.localPos();

                BlockPos worldPos = deployPos.offset(
                        localPos.getX() - localCenter.getX(),
                        localPos.getY() - localCenter.getY(),
                        localPos.getZ() - localCenter.getZ());

                if (!level.getBlockState(worldPos).canBeReplaced()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static BlockPos adjustDeployPositionAboveGround(BlockPos deployPos, SableBlueprint blueprint) {
        return deployPos.above(calculateDeployLift(blueprint));
    }

    private static int calculateDeployLift(SableBlueprint blueprint) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (SableBlueprint.SubLevelData subLevel : blueprint.subLevels()) {
            BoundingBox3ic bounds = subLevel.localBounds();

            if (!hasValidBounds(bounds)) {
                continue;
            }

            minY = Math.min(minY, bounds.minY());
            maxY = Math.max(maxY, bounds.maxY());
        }

        if (minY == Integer.MAX_VALUE || maxY == Integer.MIN_VALUE) {
            return 1;
        }

        int height = Math.max(1, maxY - minY + 1);

        return Math.max(1, (int) Math.ceil(height / 2.0D));
    }

    private static BlockPos getLocalBoundsCenter(BoundingBox3ic bounds) {
        int centerX = (bounds.minX() + bounds.maxX()) / 2;
        int centerY = (bounds.minY() + bounds.maxY()) / 2;
        int centerZ = (bounds.minZ() + bounds.maxZ()) / 2;

        return new BlockPos(centerX, centerY, centerZ);
    }

    private static boolean hasValidBounds(BoundingBox3ic bounds) {
        return bounds != null && bounds.volume() > 0;
    }
}