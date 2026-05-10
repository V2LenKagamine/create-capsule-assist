package fr.emattera.capsulecorp.serialization;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CapsuleData {
    private static final String STORED = "stored";
    private static final String CONTRAPTION_NAME = "contraption_name";
    private static final String BLOCK_COUNT = "block_count";
    private static final String SABLE_BLUEPRINT_DATA = "sable_blueprint_data";

    private static final String DEFAULT_CONTRAPTION_NAME = "Unnamed Contraption";

    private CapsuleData() {
    }

    public static boolean isStored(ItemStack stack) {
        return getRootTag(stack).getBoolean(STORED);
    }

    public static String getContraptionName(ItemStack stack) {
        return normalizeContraptionName(getRootTag(stack).getString(CONTRAPTION_NAME));
    }

    public static int getBlockCount(ItemStack stack) {
        return getRootTag(stack).getInt(BLOCK_COUNT);
    }

    public static void storeSableBlueprint(ItemStack stack, String name, SableBlueprint blueprint) {
        if (blueprint == null) {
            throw new IllegalArgumentException("Cannot store a null Sable blueprint");
        }

        CompoundTag tag = new CompoundTag();

        tag.putBoolean(STORED, true);
        tag.putString(CONTRAPTION_NAME, normalizeContraptionName(name));
        tag.putInt(BLOCK_COUNT, blueprint.blockCount());
        tag.put(SABLE_BLUEPRINT_DATA, blueprint.save());

        setRootTag(stack, tag);
    }

    public static boolean hasSableBlueprint(ItemStack stack) {
        return getRootTag(stack).contains(SABLE_BLUEPRINT_DATA, Tag.TAG_COMPOUND);
    }

    public static SableBlueprint getSableBlueprint(ItemStack stack) {
        CompoundTag root = getRootTag(stack);

        if (!root.contains(SABLE_BLUEPRINT_DATA, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Capsule does not contain a Sable blueprint");
        }

        return SableBlueprint.load(root.getCompound(SABLE_BLUEPRINT_DATA));
    }

    private static String normalizeContraptionName(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_CONTRAPTION_NAME;
        }

        return name;
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag();
    }

    private static void setRootTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}