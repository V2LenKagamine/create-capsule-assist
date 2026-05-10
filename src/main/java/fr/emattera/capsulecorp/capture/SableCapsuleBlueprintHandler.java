package fr.emattera.capsulecorp.capture;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintExporter;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintPlacer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class SableCapsuleBlueprintHandler {
    private static final String DEFAULT_CONTRAPTION_NAME = "Unnamed Contraption";

    private SableCapsuleBlueprintHandler() {
    }

    public static SableBlueprint capture(ServerLevel level, Vec3 origin, double radius) {
        return SableBlueprintExporter.export(level, origin, radius);
    }

    public static SableBlueprintPlacer.Result deploy(ServerLevel level, SableBlueprint blueprint, Vec3 origin) {
        return SableBlueprintPlacer.place(level, blueprint, origin);
    }

    public static String getDisplayName(SableBlueprint blueprint) {
        if (blueprint == null) {
            return DEFAULT_CONTRAPTION_NAME;
        }

        for (SableBlueprint.SubLevelData subLevel : blueprint.subLevels()) {
            String name = subLevel.name();

            if (name != null && !name.isBlank()) {
                return name;
            }
        }

        return DEFAULT_CONTRAPTION_NAME;
    }
}