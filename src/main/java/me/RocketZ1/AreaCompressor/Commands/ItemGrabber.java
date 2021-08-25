/*
   Copyright 2021 RocketZ1

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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