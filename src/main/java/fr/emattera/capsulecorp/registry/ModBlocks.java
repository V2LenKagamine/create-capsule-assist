package fr.emattera.capsulecorp.registry;

import fr.emattera.capsulecorp.CapsuleCorp;
import fr.emattera.capsulecorp.block.CapsuleReaderBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            Registries.BLOCK,
            CapsuleCorp.MOD_ID);

    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(
            Registries.ITEM,
            CapsuleCorp.MOD_ID);

    public static final DeferredHolder<Block, Block> CAPSULE_READER = BLOCKS.register(
            "capsule_reader",
            () -> new CapsuleReaderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> CAPSULE_READER_ITEM = BLOCK_ITEMS.register(
            "capsule_reader",
            () -> new BlockItem(CAPSULE_READER.get(), new Item.Properties()));

    private ModBlocks() {
    }
}