package fr.emattera.capsulecorp.block;

import com.mojang.serialization.MapCodec;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import fr.emattera.capsulecorp.capture.ContraptionCaptureHandler;
import fr.emattera.capsulecorp.capture.SableCapsuleBlueprintHandler;
import fr.emattera.capsulecorp.effect.CapsuleEffects;
import fr.emattera.capsulecorp.item.CapsuleItem;
import fr.emattera.capsulecorp.registry.ModSounds;
import fr.emattera.capsulecorp.serialization.CapsuleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CapsuleReaderBlock extends DirectionalBlock {
    private static final MapCodec<CapsuleReaderBlock> MAP_CODEC = simpleCodec(CapsuleReaderBlock::new);

    private static final double THICKNESS = 5.0D;

    private static final VoxelShape UP_SHAPE = box(
            0.0D, 0.0D, 0.0D,
            16.0D, THICKNESS, 16.0D);

    private static final VoxelShape DOWN_SHAPE = box(
            0.0D, 16.0D - THICKNESS, 0.0D,
            16.0D, 16.0D, 16.0D);

    private static final VoxelShape NORTH_SHAPE = box(
            0.0D, 0.0D, 16.0D - THICKNESS,
            16.0D, 16.0D, 16.0D);

    private static final VoxelShape SOUTH_SHAPE = box(
            0.0D, 0.0D, 0.0D,
            16.0D, 16.0D, THICKNESS);

    private static final VoxelShape WEST_SHAPE = box(
            16.0D - THICKNESS, 0.0D, 0.0D,
            16.0D, 16.0D, 16.0D);

    private static final VoxelShape EAST_SHAPE = box(
            0.0D, 0.0D, 0.0D,
            THICKNESS, 16.0D, 16.0D);

    public CapsuleReaderBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return MAP_CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            case UP -> UP_SHAPE;
        };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());

        return level.getBlockState(supportPos).isFaceSturdy(
                level,
                supportPos,
                facing,
                SupportType.FULL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.insert_capsule"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!(stack.getItem() instanceof CapsuleItem capsuleItem)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.not_capsule"));
            return ItemInteractionResult.SUCCESS;
        }

        if (CapsuleData.isStored(stack)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.already_filled"));
            return ItemInteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.server_only"));
            return ItemInteractionResult.SUCCESS;
        }

        SableBlueprint blueprint = ContraptionCaptureHandler.captureSableBlueprint(serverLevel, pos);

        if (blueprint == null || blueprint.isEmpty() || blueprint.blockCount() <= 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.no_contraption"));
            return ItemInteractionResult.SUCCESS;
        }

        if (!ContraptionCaptureHandler.hasExplicitName(blueprint)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.no_name"));
            return ItemInteractionResult.SUCCESS;
        }

        if (!ContraptionCaptureHandler.capturesOnlyReaderSubLevel(serverLevel, pos, blueprint)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.multiple_contraptions"));
            return ItemInteractionResult.SUCCESS;
        }

        if (!ContraptionCaptureHandler.hasSingleExplicitName(blueprint)) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.multiple_contraptions"));
            return ItemInteractionResult.SUCCESS;
        }

        int capturedBlockCount = blueprint.blockCount();

        if (capturedBlockCount > capsuleItem.getBlockCapacity()) {
            player.sendSystemMessage(Component.translatable(
                    "message.capsulecorp.reader.capacity_exceeded",
                    capturedBlockCount,
                    capsuleItem.getBlockCapacity()));
            return ItemInteractionResult.SUCCESS;
        }

        String contraptionName = SableCapsuleBlueprintHandler.getDisplayName(blueprint);
        ItemStack filledCapsule = stack.getCount() == 1
                ? stack
                : stack.copyWithCount(1);

        CapsuleData.storeSableBlueprint(
                filledCapsule,
                contraptionName,
                blueprint);

        if (stack.getCount() > 1) {
            stack.shrink(1);

            if (!player.getInventory().add(filledCapsule)) {
                player.drop(filledCapsule, false);
            }
        }

        serverLevel.playSound(
                null,
                pos,
                ModSounds.CAPSULE_CAPTURE.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);

        CapsuleEffects.playCaptureEffect(
                serverLevel,
                pos);

        ContraptionCaptureHandler.removeCapturedSableBlueprintSource(
                serverLevel,
                blueprint);

        player.sendSystemMessage(Component.translatable(
                "message.capsulecorp.reader.captured_named",
                contraptionName,
                capturedBlockCount));

        return ItemInteractionResult.SUCCESS;
    }
}