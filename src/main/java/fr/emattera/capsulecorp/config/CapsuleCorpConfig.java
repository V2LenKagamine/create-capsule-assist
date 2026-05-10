package fr.emattera.capsulecorp.config;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CapsuleCorpConfig {
    private static final int DEFAULT_COPPER_CAPACITY = 512;
    private static final int DEFAULT_IRON_CAPACITY = 1024;
    private static final int DEFAULT_GOLD_CAPACITY = 2048;
    private static final int DEFAULT_DIAMOND_CAPACITY = 4096;
    private static final int DEFAULT_NETHERITE_CAPACITY = 8192;

    private static final int DEFAULT_WALLET_SLOTS = 6;

    private static final double DEFAULT_THROW_VELOCITY = 1.0D;
    private static final int DEFAULT_DEPLOY_DELAY_TICKS = 15;
    private static final int DEFAULT_STABILIZATION_TICKS = 20;
    private static final int DEFAULT_DEPLOY_SEARCH_RADIUS = 3;

    public static final ModConfigSpec SPEC;
    public static final Values VALUES;

    static {
        Pair<Values, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Values::new);
        VALUES = pair.getLeft();
        SPEC = pair.getRight();
    }

    private CapsuleCorpConfig() {
    }

    public static int copperCapacity() {
        return VALUES.copperCapacity.get();
    }

    public static int ironCapacity() {
        return VALUES.ironCapacity.get();
    }

    public static int goldCapacity() {
        return VALUES.goldCapacity.get();
    }

    public static int diamondCapacity() {
        return VALUES.diamondCapacity.get();
    }

    public static int netheriteCapacity() {
        return VALUES.netheriteCapacity.get();
    }

    public static int capsuleWalletSlots() {
        return VALUES.capsuleWalletSlots.get();
    }

    public static float capsuleThrowVelocity() {
        return VALUES.capsuleThrowVelocity.get().floatValue();
    }

    public static int deployDelayTicks() {
        return VALUES.deployDelayTicks.get();
    }

    public static int postDeployStabilizationTicks() {
        return VALUES.postDeployStabilizationTicks.get();
    }

    public static int deploySearchRadius() {
        return VALUES.deploySearchRadius.get();
    }

    public static final class Values {
        private final ModConfigSpec.IntValue copperCapacity;
        private final ModConfigSpec.IntValue ironCapacity;
        private final ModConfigSpec.IntValue goldCapacity;
        private final ModConfigSpec.IntValue diamondCapacity;
        private final ModConfigSpec.IntValue netheriteCapacity;

        private final ModConfigSpec.IntValue capsuleWalletSlots;

        private final ModConfigSpec.DoubleValue capsuleThrowVelocity;
        private final ModConfigSpec.IntValue deployDelayTicks;
        private final ModConfigSpec.IntValue postDeployStabilizationTicks;
        private final ModConfigSpec.IntValue deploySearchRadius;

        private Values(ModConfigSpec.Builder builder) {
            builder.push("capsules");

            copperCapacity = builder
                    .comment("Maximum number of blocks a Copper Capsule can store.")
                    .defineInRange("copperCapacity", DEFAULT_COPPER_CAPACITY, 1, 8192);

            ironCapacity = builder
                    .comment("Maximum number of blocks an Iron Capsule can store.")
                    .defineInRange("ironCapacity", DEFAULT_IRON_CAPACITY, 1, 8192);

            goldCapacity = builder
                    .comment("Maximum number of blocks a Gold Capsule can store.")
                    .defineInRange("goldCapacity", DEFAULT_GOLD_CAPACITY, 1, 8192);

            diamondCapacity = builder
                    .comment("Maximum number of blocks a Diamond Capsule can store.")
                    .defineInRange("diamondCapacity", DEFAULT_DIAMOND_CAPACITY, 1, 8192);

            netheriteCapacity = builder
                    .comment("Maximum number of blocks a Netherite Capsule can store.")
                    .defineInRange("netheriteCapacity", DEFAULT_NETHERITE_CAPACITY, 1, 8192);

            builder.pop();

            builder.push("wallet");

            capsuleWalletSlots = builder
                    .comment("Maximum number of capsules a Capsule Wallet can store.")
                    .defineInRange("capsuleWalletSlots", DEFAULT_WALLET_SLOTS, 1, 27);

            builder.pop();

            builder.push("deployment");

            capsuleThrowVelocity = builder
                    .comment("Initial velocity of a thrown capsule.")
                    .defineInRange("capsuleThrowVelocity", DEFAULT_THROW_VELOCITY, 0.1D, 5.0D);

            deployDelayTicks = builder
                    .comment("Ticks before a thrown capsule deploys automatically while flying.")
                    .defineInRange("deployDelayTicks", DEFAULT_DEPLOY_DELAY_TICKS, 1, 200);

            postDeployStabilizationTicks = builder
                    .comment("Ticks during which newly deployed Sable sub-levels are stabilized.")
                    .defineInRange("postDeployStabilizationTicks", DEFAULT_STABILIZATION_TICKS, 0, 200);

            deploySearchRadius = builder
                    .comment(
                            "Maximum radius, in blocks, used to find a nearby valid deployment position",
                            "when the initial position is blocked.")
                    .defineInRange("deploySearchRadius", DEFAULT_DEPLOY_SEARCH_RADIUS, 0, 12);

            builder.pop();
        }
    }
}