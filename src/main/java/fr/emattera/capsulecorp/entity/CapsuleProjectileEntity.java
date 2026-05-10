package fr.emattera.capsulecorp.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.deployment.ContraptionDeploymentHandler;
import fr.emattera.capsulecorp.effect.CapsuleEffects;
import fr.emattera.capsulecorp.registry.ModItems;
import fr.emattera.capsulecorp.registry.ModSounds;
import fr.emattera.capsulecorp.serialization.CapsuleData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CapsuleProjectileEntity extends ThrowableItemProjectile {
    private final List<UUID> placedSableSubLevelUuids = new ArrayList<>();

    private ItemStack sourceCapsuleStack = ItemStack.EMPTY;
    private int ticksUntilDeploy;
    private int stabilizationTicksRemaining = 0;
    private boolean deployed = false;

    public CapsuleProjectileEntity(EntityType<? extends CapsuleProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.ticksUntilDeploy = CapsuleCorpConfig.deployDelayTicks();
    }

    public void setSourceCapsuleStack(ItemStack sourceCapsuleStack) {
        this.sourceCapsuleStack = sourceCapsuleStack.copyWithCount(1);
        setItem(this.sourceCapsuleStack.copy());
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DIAMOND_CAPSULE.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (deployed) {
            stabilizeAfterDeploy();
            return;
        }

        ticksUntilDeploy--;

        if (ticksUntilDeploy <= 0) {
            deployNow(blockPosition());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (level().isClientSide || deployed) {
            return;
        }

        deployNow(getDeployPositionFromHit(result));
    }

    private void deployNow(BlockPos deployPos) {
        if (deployed) {
            return;
        }

        deployed = true;
        hideProjectileDuringStabilization();

        ContraptionDeploymentHandler.DeploymentResult deploymentResult =
                ContraptionDeploymentHandler.deployFromCapsuleWithResult(
                        level(),
                        deployPos,
                        sourceCapsuleStack);

        if (!deploymentResult.success()) {
            dropSourceCapsule();
            notifyOwnerDeploymentFailed();
            discard();
            return;
        }

        placedSableSubLevelUuids.clear();
        placedSableSubLevelUuids.addAll(deploymentResult.placedSubLevelUuids());

        if (placedSableSubLevelUuids.isEmpty()) {
            discard();
            return;
        }

        playDeployFeedback(deployPos);

        stabilizationTicksRemaining = CapsuleCorpConfig.postDeployStabilizationTicks();

        if (stabilizationTicksRemaining <= 0) {
            discard();
        }
    }

    private void stabilizeAfterDeploy() {
        if (stabilizationTicksRemaining <= 0) {
            discard();
            return;
        }

        stabilizationTicksRemaining--;
        setDeltaMovement(Vec3.ZERO);

        if (level() instanceof ServerLevel serverLevel) {
            ContraptionDeploymentHandler.stabilizeSubLevels(
                    serverLevel,
                    placedSableSubLevelUuids);
        }

        if (stabilizationTicksRemaining <= 0) {
            discard();
        }
    }

    private void playDeployFeedback(BlockPos deployPos) {
        level().playSound(
                null,
                deployPos,
                ModSounds.CAPSULE_DEPLOY.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        if (level() instanceof ServerLevel serverLevel) {
            CapsuleEffects.playDeploySmoke(
                    serverLevel,
                    Vec3.atCenterOf(deployPos),
                    CapsuleData.getBlockCount(sourceCapsuleStack));
        }
    }

    private void hideProjectileDuringStabilization() {
        setInvisible(true);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }

    private void dropSourceCapsule() {
        if (sourceCapsuleStack.isEmpty()) {
            return;
        }

        ItemEntity droppedCapsule = new ItemEntity(
                level(),
                getX(),
                getY(),
                getZ(),
                sourceCapsuleStack.copy());

        level().addFreshEntity(droppedCapsule);
    }

    private void notifyOwnerDeploymentFailed() {
        Entity owner = getOwner();

        if (owner instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.capsule.deploy_failed"));
        }
    }

    private BlockPos getDeployPositionFromHit(HitResult result) {
        if (result instanceof BlockHitResult blockHitResult) {
            return blockHitResult
                    .getBlockPos()
                    .relative(blockHitResult.getDirection());
        }

        return BlockPos.containing(result.getLocation());
    }
}