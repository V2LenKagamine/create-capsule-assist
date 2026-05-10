package fr.emattera.capsulecorp.registry;

import java.util.function.Supplier;

import fr.emattera.capsulecorp.CapsuleCorp;
import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.item.CapsuleItem;
import fr.emattera.capsulecorp.item.CapsuleWalletItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CapsuleCorp.MOD_ID);

    public static final Supplier<Item> CAPSULE_WALLET = ITEMS.register(
            "capsule_wallet",
            () -> new CapsuleWalletItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> COPPER_CAPSULE = ITEMS.register(
            "copper_capsule",
            () -> new CapsuleItem(capsuleProperties(), CapsuleCorpConfig::copperCapacity));

    public static final Supplier<Item> IRON_CAPSULE = ITEMS.register(
            "iron_capsule",
            () -> new CapsuleItem(capsuleProperties(), CapsuleCorpConfig::ironCapacity));

    public static final Supplier<Item> GOLD_CAPSULE = ITEMS.register(
            "gold_capsule",
            () -> new CapsuleItem(capsuleProperties(), CapsuleCorpConfig::goldCapacity));

    public static final Supplier<Item> DIAMOND_CAPSULE = ITEMS.register(
            "diamond_capsule",
            () -> new CapsuleItem(capsuleProperties(), CapsuleCorpConfig::diamondCapacity));

    public static final Supplier<Item> NETHERITE_CAPSULE = ITEMS.register(
            "netherite_capsule",
            () -> new CapsuleItem(capsuleProperties(), CapsuleCorpConfig::netheriteCapacity));

    public static final Supplier<Item> INCOMPLETE_COPPER_CAPSULE = ITEMS.register(
            "incomplete_copper_capsule",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> INCOMPLETE_IRON_CAPSULE = ITEMS.register(
            "incomplete_iron_capsule",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> INCOMPLETE_GOLD_CAPSULE = ITEMS.register(
            "incomplete_gold_capsule",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> INCOMPLETE_DIAMOND_CAPSULE = ITEMS.register(
            "incomplete_diamond_capsule",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> INCOMPLETE_NETHERITE_CAPSULE = ITEMS.register(
            "incomplete_netherite_capsule",
            () -> new Item(new Item.Properties()));

    private ModItems() {
    }

    private static Item.Properties capsuleProperties() {
        return new Item.Properties().stacksTo(16);
    }
}