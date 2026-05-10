package fr.emattera.capsulecorp.item;

import java.util.List;
import java.util.Optional;

import fr.emattera.capsulecorp.client.tooltip.CapsuleWalletTooltip;
import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.serialization.CapsuleWalletData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CapsuleWalletItem extends Item {
    public CapsuleWalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (CapsuleWalletData.countItems(stack) <= 0) {
            return Optional.empty();
        }

        return Optional.of(new CapsuleWalletTooltip(
                CapsuleWalletData.getStoredItems(stack),
                walletSlotCount()));
    }

    @Override
    public boolean overrideStackedOnOther(
            ItemStack walletStack,
            Slot slot,
            ClickAction action,
            Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            return extractLastCapsuleToSlot(walletStack, slot);
        }

        if (!tryStoreCapsule(walletStack, slotStack)) {
            return false;
        }

        slot.setChanged();
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack walletStack,
            ItemStack carriedStack,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess carriedSlotAccess) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        if (carriedStack.isEmpty()) {
            return extractLastCapsuleToCarriedSlot(
                    walletStack,
                    slot,
                    carriedSlotAccess);
        }

        if (!tryStoreCapsule(walletStack, carriedStack)) {
            return false;
        }

        carriedSlotAccess.set(carriedStack);
        slot.setChanged();
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.capsulecorp.wallet_count",
                CapsuleWalletData.countItems(stack),
                walletSlotCount()));
    }

    private static boolean extractLastCapsuleToSlot(ItemStack walletStack, Slot slot) {
        ItemStack extractedCapsule = CapsuleWalletData.removeLastItem(walletStack);

        if (extractedCapsule.isEmpty()) {
            return false;
        }

        slot.set(extractedCapsule);
        slot.setChanged();
        return true;
    }

    private static boolean extractLastCapsuleToCarriedSlot(
            ItemStack walletStack,
            Slot slot,
            SlotAccess carriedSlotAccess) {
        ItemStack extractedCapsule = CapsuleWalletData.removeLastItem(walletStack);

        if (extractedCapsule.isEmpty()) {
            return false;
        }

        carriedSlotAccess.set(extractedCapsule);
        slot.setChanged();
        return true;
    }

    private static boolean tryStoreCapsule(ItemStack walletStack, ItemStack capsuleStack) {
        if (!(capsuleStack.getItem() instanceof CapsuleItem)) {
            return false;
        }

        if (CapsuleWalletData.isFull(walletStack, walletSlotCount())) {
            return false;
        }

        ItemStack capsuleToStore = capsuleStack.copyWithCount(1);

        if (!CapsuleWalletData.addItem(walletStack, capsuleToStore, walletSlotCount())) {
            return false;
        }

        capsuleStack.shrink(1);
        return true;
    }

    private static int walletSlotCount() {
        return CapsuleCorpConfig.capsuleWalletSlots();
    }
}