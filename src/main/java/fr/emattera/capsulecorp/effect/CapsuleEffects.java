package fr.emattera.capsulecorp.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class CapsuleEffects {
    private static final int SMALL_MAX_BLOCKS = 1024;
    private static final int MEDIUM_MAX_BLOCKS = 4096;

    private CapsuleEffects() {
    }

    public static void playCaptureEffect(ServerLevel level, BlockPos readerPos) {
        Vec3 center = Vec3.atCenterOf(readerPos).add(0.0D, 0.35D, 0.0D);

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                center.x,
                center.y,
                center.z,
                12,
                0.35D,
                0.15D,
                0.35D,
                0.02D);

        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x,
                center.y,
                center.z,
                8,
                0.25D,
                0.1D,
                0.25D,
                0.015D);
    }

    public static void playDeploySmoke(ServerLevel level, Vec3 center, int blockCount) {
        SmokeSize smokeSize = getSmokeSize(blockCount);

        spawnSmokeBurst(
                level,
                center,
                smokeSize.cloudCount(),
                smokeSize.poofCount(),
                smokeSize.cosySmokeCount(),
                smokeSize.signalSmokeCount(),
                smokeSize.radius(),
                smokeSize.height(),
                smokeSize.speed());
    }

    private static void spawnSmokeBurst(
            ServerLevel level,
            Vec3 center,
            int cloudCount,
            int poofCount,
            int cosySmokeCount,
            int signalSmokeCount,
            double radius,
            double height,
            double speed) {
        RandomSource random = level.random;

        spawnRandomizedParticles(
                level,
                random,
                ParticleTypes.POOF,
                center,
                poofCount,
                radius,
                height,
                speed);

        spawnRandomizedParticles(
                level,
                random,
                ParticleTypes.CLOUD,
                center,
                cloudCount,
                radius * 0.95D,
                height,
                speed * 0.8D);

        spawnRandomizedParticles(
                level,
                random,
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center,
                cosySmokeCount,
                radius * 0.8D,
                height * 0.6D,
                speed * 0.25D);

        if (signalSmokeCount > 0) {
            spawnRandomizedParticles(
                    level,
                    random,
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    center,
                    signalSmokeCount,
                    radius * 0.7D,
                    height * 0.5D,
                    speed * 0.2D);
        }
    }

    private static void spawnRandomizedParticles(
            ServerLevel level,
            RandomSource random,
            SimpleParticleType particle,
            Vec3 center,
            int count,
            double radius,
            double height,
            double speed) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = random.nextDouble() * radius;

            double x = center.x + Math.cos(angle) * distance;
            double y = center.y + random.nextDouble() * height;
            double z = center.z + Math.sin(angle) * distance;

            double dx = (random.nextDouble() - 0.5D) * speed;
            double dy = random.nextDouble() * speed;
            double dz = (random.nextDouble() - 0.5D) * speed;

            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    1,
                    dx,
                    dy,
                    dz,
                    speed);
        }
    }

    private static SmokeSize getSmokeSize(int blockCount) {
        if (blockCount <= SMALL_MAX_BLOCKS) {
            return SmokeSize.SMALL;
        }

        if (blockCount <= MEDIUM_MAX_BLOCKS) {
            return SmokeSize.MEDIUM;
        }

        return SmokeSize.LARGE;
    }

    private enum SmokeSize {
        SMALL(
                90,
                120,
                18,
                0,
                4.0D,
                2.2D,
                0.065D),

        MEDIUM(
                220,
                300,
                55,
                8,
                7.5D,
                3.8D,
                0.08D),

        LARGE(
                420,
                560,
                110,
                24,
                12.0D,
                5.5D,
                0.095D);

        private final int cloudCount;
        private final int poofCount;
        private final int cosySmokeCount;
        private final int signalSmokeCount;
        private final double radius;
        private final double height;
        private final double speed;

        SmokeSize(
                int cloudCount,
                int poofCount,
                int cosySmokeCount,
                int signalSmokeCount,
                double radius,
                double height,
                double speed) {
            this.cloudCount = cloudCount;
            this.poofCount = poofCount;
            this.cosySmokeCount = cosySmokeCount;
            this.signalSmokeCount = signalSmokeCount;
            this.radius = radius;
            this.height = height;
            this.speed = speed;
        }

        private int cloudCount() {
            return cloudCount;
        }

        private int poofCount() {
            return poofCount;
        }

        private int cosySmokeCount() {
            return cosySmokeCount;
        }

        private int signalSmokeCount() {
            return signalSmokeCount;
        }

        private double radius() {
            return radius;
        }

        private double height() {
            return height;
        }

        private double speed() {
            return speed;
        }
    }
}