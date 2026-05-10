package fr.emattera.capsulecorp.item;

import java.util.List;
import java.util.function.IntSupplier;

import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.entity.CapsuleProjectileEntity;
import fr.emattera.capsulecorp.registry.ModEntities;
import fr.emattera.capsulecorp.serialization.CapsuleData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CapsuleItem extends Item {
    private final IntSupplier blockCapacitySupplier;

    public CapsuleItem(Properties properties, IntSupplier blockCapacitySupplier) {
        super(properties);
        this.blockCapacitySupplier = blockCapacitySupplier;
    }

    public int getBlockCapacity() {
        return blockCapacitySupplier.getAsInt();
    }

    @Override
    public Component getName(ItemStack stack) {
        if (CapsuleData.isStored(stack)) {
            return Component.literal(CapsuleData.getContraptionName(stack));
        }

        return Component.translatable(
                "item.capsulecorp.empty_capsule",
                Component.translatable(getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.capsulecorp.capacity", getBlockCapacity()));

        if (CapsuleData.isStored(stack)) {
            tooltip.add(Component.translatable(
                    "tooltip.capsulecorp.currently_contains",
                    CapsuleData.getContraptionName(stack)));
            tooltip.add(Component.translatable(
                    "tooltip.capsulecorp.block_count",
                    CapsuleData.getBlockCount(stack)));
            return;
        }

        tooltip.add(Component.translatable("tooltip.capsulecorp.empty"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!CapsuleData.isStored(stack) || !CapsuleData.hasSableBlueprint(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        spawnCapsuleProjectile(level, player, stack);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

    private static void spawnCapsuleProjectile(Level level, Player player, ItemStack capsuleStack) {
        CapsuleProjectileEntity projectile = new CapsuleProjectileEntity(
                ModEntities.CAPSULE_PROJECTILE.get(),
                level);

        projectile.setOwner(player);
        projectile.setSourceCapsuleStack(capsuleStack.copyWithCount(1));

        projectile.setPos(
                player.getX(),
                player.getEyeY() - 0.1D,
                player.getZ());

        projectile.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                CapsuleCorpConfig.capsuleThrowVelocity(),
                1.0F);

        level.addFreshEntity(projectile);
    }
}