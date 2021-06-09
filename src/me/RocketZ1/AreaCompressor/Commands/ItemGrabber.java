package me.RocketZ1.AreaCompressor.Commands;

import org.bukkit.*;
import org.bukkit.inventory.*;

import java.util.*;

public class ItemGrabber {

    private final List<Inventory> inventories;

    public ItemGrabber() {
        inventories = new ArrayList<>();
        inventories.add(Bukkit.createInventory(null, 54));
    }

    public void addItem(ItemStack itemStack) {
        if (itemStack == null) return;
        int index = inventories.size() - 1;
        Inventory inv = inventories.get(index);
        HashMap<Integer, ItemStack> map = inv.addItem(itemStack);
        inventories.set(index, inv);
        if (map.isEmpty()) return;
        inventories.add(Bukkit.createInventory(null, 54));
        map.values().forEach(this::addItem);
    }

    public void addItems(ItemStack... items) {
        Arrays.stream(items).forEach(this::addItem);
    }

    public List<ItemStack> getItems() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Inventory inv : inventories) {
            for (ItemStack item : inv.getContents()) {
                if (item != null) itemStacks.add(item);
            }
        }
        return itemStacks;
    }
}