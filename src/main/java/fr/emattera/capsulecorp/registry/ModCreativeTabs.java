package fr.emattera.capsulecorp.registry;

import fr.emattera.capsulecorp.CapsuleCorp;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            CapsuleCorp.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CAPSULE_CORP_TAB = CREATIVE_TABS.register(
            "capsule_corp",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.capsulecorp"))
                    .icon(() -> new ItemStack(ModItems.DIAMOND_CAPSULE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.CAPSULE_READER_ITEM.get());
                        output.accept(ModItems.CAPSULE_WALLET.get());

                        output.accept(ModItems.COPPER_CAPSULE.get());
                        output.accept(ModItems.IRON_CAPSULE.get());
                        output.accept(ModItems.GOLD_CAPSULE.get());
                        output.accept(ModItems.DIAMOND_CAPSULE.get());
                        output.accept(ModItems.NETHERITE_CAPSULE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}