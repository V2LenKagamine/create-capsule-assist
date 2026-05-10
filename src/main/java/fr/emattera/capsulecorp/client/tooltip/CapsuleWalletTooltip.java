package fr.emattera.capsulecorp.client.tooltip;

import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record CapsuleWalletTooltip(List<ItemStack> items, int maxItems) implements TooltipComponent {
    public CapsuleWalletTooltip {
        items = List.copyOf(items);
    }
}