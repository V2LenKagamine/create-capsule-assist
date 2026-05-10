package fr.emattera.capsulecorp.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ClientCapsuleWalletTooltip implements ClientTooltipComponent {
    private static final int SLOT_SIZE = 18;
    private static final int ICON_OFFSET = 1;
    private static final int HEIGHT = 20;

    private static final int BORDER_COLOR = 0xFF2A2A2A;
    private static final int INNER_BORDER_COLOR = 0xFF555555;
    private static final int BACKGROUND_COLOR = 0xFF1F1F1F;

    private final CapsuleWalletTooltip tooltip;

    public ClientCapsuleWalletTooltip(CapsuleWalletTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return tooltip.maxItems() * SLOT_SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (int slot = 0; slot < tooltip.maxItems(); slot++) {
            int slotX = x + slot * SLOT_SIZE;

            renderSlotBackground(guiGraphics, slotX, y);

            if (slot >= tooltip.items().size()) {
                continue;
            }

            ItemStack stack = tooltip.items().get(slot);

            if (stack.isEmpty()) {
                continue;
            }

            guiGraphics.renderItem(
                    stack,
                    slotX + ICON_OFFSET,
                    y + ICON_OFFSET);

            guiGraphics.renderItemDecorations(
                    font,
                    stack,
                    slotX + ICON_OFFSET,
                    y + ICON_OFFSET);
        }
    }

    private static void renderSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, BORDER_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, INNER_BORDER_COLOR);
        guiGraphics.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, BACKGROUND_COLOR);
    }
}