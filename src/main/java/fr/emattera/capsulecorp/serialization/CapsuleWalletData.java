package fr.emattera.capsulecorp.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CapsuleWalletData {
    private static final String ITEMS = "items";
    private static final String STACK = "stack";

    private static final HolderLookup.Provider EMPTY_REGISTRY_ACCESS = HolderLookup.Provider.create(Stream.empty());

    private CapsuleWalletData() {
    }

    public static boolean addItem(ItemStack walletStack, ItemStack stackToAdd, int maxItems) {
        List<ItemStack> items = loadItems(walletStack);

        if (items.size() >= maxItems) {
            return false;
        }

        items.add(stackToAdd.copyWithCount(1));
        saveItems(walletStack, items);

        return true;
    }

    public static ItemStack removeLastItem(ItemStack walletStack) {
        List<ItemStack> items = loadItems(walletStack);

        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = items.remove(items.size() - 1);
        saveItems(walletStack, items);

        return removed;
    }

    public static boolean isFull(ItemStack walletStack, int maxItems) {
        return countItems(walletStack) >= maxItems;
    }

    public static int countItems(ItemStack walletStack) {
        return loadItems(walletStack).size();
    }

    public static List<ItemStack> getStoredItems(ItemStack walletStack) {
        return List.copyOf(loadItems(walletStack));
    }

    private static List<ItemStack> loadItems(ItemStack walletStack) {
        CompoundTag root = getRootTag(walletStack);
        List<ItemStack> items = new ArrayList<>();

        if (!root.contains(ITEMS, Tag.TAG_LIST)) {
            return items;
        }

        ListTag itemList = root.getList(ITEMS, Tag.TAG_COMPOUND);

        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag entryTag = itemList.getCompound(i);

            if (!entryTag.contains(STACK, Tag.TAG_COMPOUND)) {
                continue;
            }

            ItemStack stack = ItemStack.parseOptional(
                    EMPTY_REGISTRY_ACCESS,
                    entryTag.getCompound(STACK));

            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }

        return items;
    }

    private static void saveItems(ItemStack walletStack, List<ItemStack> items) {
        CompoundTag root = getRootTag(walletStack);
        ListTag itemList = new ListTag();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag entryTag = new CompoundTag();

            entryTag.put(
                    STACK,
                    stack.save(EMPTY_REGISTRY_ACCESS));

            itemList.add(entryTag);
        }

        root.put(ITEMS, itemList);
        setRootTag(walletStack, root);
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag();
    }

    private static void setRootTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}