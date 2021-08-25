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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import me.RocketZ1.AreaCompressor.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaCompressorCmd implements CommandExecutor, TabCompleter {

    private Main plugin;

    public AreaCompressorCmd(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("areacompressor").setExecutor(this);
        plugin.getCommand("ac").setExecutor(this);
        plugin.getCommand("areacompressor").setTabCompleter(this);
        plugin.getCommand("ac").setTabCompleter(this);
    }

    List<String> arguments1 = new ArrayList<String>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player p = (Player) sender;
        if (!sender.hasPermission("areacompressor.use")) return null;
        if (args.length == 1) {
            if (arguments1.isEmpty()) {
                arguments1.add("compress");
                arguments1.add("undo");
                if(p.hasPermission("areacompressor.reload")) arguments1.add("reload");
            }
            List<String> result = new ArrayList<String>();
            for (String a : arguments1) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.format("&cOnly players can execute this command!"));
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("areacompressor.use")) {
            p.sendMessage(plugin.format("&cYou do not have permission to execute this command!"));
            return true;
        }
        if (args.length != 1) {
            p.sendMessage(plugin.format("&cInvalid Command! Try /areacompressor [compress/undo/reload]"));
            return true;
        }
        if(args[0].equalsIgnoreCase("compress")) {
            if (!plugin.pos1.containsKey(p.getUniqueId())) {
                p.sendMessage(plugin.format("&cError: Please create a region before running this command!"));
                return true;
            }
            if (!plugin.pos2.containsKey(p.getUniqueId())) {
                p.sendMessage(plugin.format("&cError: Please create a region before running this command!"));
                return true;
            }
            Location loc1 = plugin.pos1.get(p.getUniqueId());
            Location loc2 = plugin.pos2.get(p.getUniqueId());
            if (plugin.confirmTimer.contains(p.getUniqueId())) {
                if(undoBlockState.containsKey(p)) undoBlockState.remove(p);
                if(undoFrames.containsKey(p)) undoFrames.remove(p);
                getArea(p, loc1, loc2);
                plugin.confirmTimer.remove(p.getUniqueId());
            } else {
                isChunkLoadedCheck(p, loc1, loc2);
            }
        } else if (args[0].equalsIgnoreCase("undo")) {
            if(!undoBlockState.containsKey(p)){
                p.sendMessage(plugin.format("&cYou have no compressed area to undo!"));
                return true;
            }
            ArrayList<BlockState> blockState = undoBlockState.get(p);
            ArrayList<ItemFrame> frameList = new ArrayList<>();
            if(undoFrames.containsKey(p)){
                frameList = undoFrames.get(p);
            }
            undoCompressing(p, blockState, frameList);
        } else if (args[0].equalsIgnoreCase("reload")) {
            if(!p.hasPermission("areacompressor.reload")){
                p.sendMessage(plugin.format("&cYou do not have permission to execute this command!"));
                return true;
            }
            plugin.config.reloadConfig();
            plugin.setupConfig();
            p.sendMessage(plugin.format("&8[Console]: &aAreaCompressor config successfully reloaded!"));
        }else{
            p.sendMessage(plugin.format("&cInvalid Command! Try /areacompressor [compress/undo/reload]"));
            return true;
        }
        //compressarea (cords 1) (cords 2)
        return false;
    }

    public void isChunkLoadedCheck(Player p, Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            p.sendMessage(plugin.format("&cError: Please make both loc1 & loc2 in the same world!"));
            return;
        }
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY()) + 1;
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        int blockCount = 0;
        for (int x1 = x; x1 < maxX; x1++) {
            for (int y1 = y; y1 < maxY; y1++) {
                for (int z1 = z; z1 < maxZ; z1++) {
                    Location loc = new Location(p.getWorld(), x1, y1, z1);
                    if(plugin.worldGuardPlugin != null){
                        if(plugin.checkRegion(p, BukkitAdapter.adapt(loc))){
                            p.sendMessage(plugin.format("&cError: You cannot compress this area because you do not have access to this region!"));
                            return;
                        }
                    }
                    if(plugin.griefPrevention != null) {
                        if (!plugin.checkClaimAccess(p, loc)) {
                            p.sendMessage(plugin.format("&cError: You cannot compress this area because you do not have access to this claim!"));
                            return;
                        }
                    }
                    blockCount++;
                    if (!loc.getChunk().isLoaded()) {
                        p.sendMessage(plugin.format("&cError: Cannot compress area if a chunk in the area is unloaded!"));
                        return;
                    }
                }
            }
        }
        if(plugin.maxBlocksCompressed != -1){
            if(blockCount > plugin.maxBlocksCompressed){
                p.sendMessage(plugin.format("&cError: Your trying to compress a region " + blockCount+" blocks big! You cannot compress a region more than " + plugin.maxBlocksCompressed + " at once!"));
                return;
            }
        }
        if(plugin.compressShulkers && blockCount > 10000){
            p.sendMessage(plugin.format("&c&lWARNING! Compressing any region with a lot of data (Items, Blocks, etc) THEN picking up the compressed shulker MAY cause you to be kicked from the server until the compressed shulker is removed from your inventory! Pickup at your own risk!"));
        }
        p.sendMessage(plugin.format("&aAre you sure you want to compress this area ("+blockCount+" blocks in this region!)? Type the command again within 10 seconds to confirm!"));
        plugin.confirmTimer.add(p.getUniqueId());
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(plugin.confirmTimer.contains(p.getUniqueId())){
                    plugin.confirmTimer.remove(p.getUniqueId());
                }
                cancel();
            }
        };runnable.runTaskLater(plugin, 20 * 10);
    }

    private Map<Player, ArrayList<BlockState>> undoBlockState = new HashMap<>();
    private Map<Player, ArrayList<ItemFrame>> undoFrames = new HashMap<>();

    public void getArea(Player p, Location loc1, Location loc2) {
        ArrayList<Location> itemFrameLoc = new ArrayList<>();
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY()) + 1;
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        ItemGrabber grabber = new ItemGrabber();
        ArrayList<BlockState> blockState = new ArrayList<>();
        ArrayList<ItemFrame> frameList = new ArrayList<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            int x1 = x;
            int amtCompressed = 0;

            @Override
            public void run() {
                if (x1 < maxX) {
                    for (int y1 = maxY; y1 >=y; y1--) { //Test to see if this works
                        for (int z1 = z; z1 < maxZ; z1++) {
                            Location loc = new Location(p.getWorld(), x1, y1, z1);
                            //Add BlockState to list for possible rollback later
                            blockState.add(loc.getBlock().getState());
                            //Check if compressor should skip this block
                            if(plugin.blacklistedBlocks.contains(loc.getBlock().getType())) continue;
                            amtCompressed++;
                            //Add Container Items to Grabber
                            if (loc.getBlock().getState() instanceof Container) {
                                Inventory inventory = ((Container) loc.getBlock().getState()).getInventory();
                                grabber.addItems(inventory.getContents());
                                inventory.clear();
                            }
                            if(!loc.getBlock().isLiquid()){
                                ItemStack item = new ItemStack(loc.getBlock().getType());
                                grabber.addItem(item);
                            }
                            loc.getBlock().setType(Material.AIR);
                            for (Entity e : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
                                if (e instanceof ItemFrame) {
                                    ItemFrame itemFrame = (ItemFrame) e;
                                    if (itemFrameLoc.contains(itemFrame.getLocation())) continue;
                                    frameList.add(itemFrame);
                                    grabber.addItem(itemFrame.getItem());
                                    grabber.addItem(new ItemStack(Material.ITEM_FRAME));
                                    itemFrame.remove();
                                    itemFrameLoc.add(itemFrame.getLocation());
                                }
                            }
                        }
                    }
                    x1++;
                }else{
                    undoBlockState.put(p, blockState);
                    undoFrames.put(p, frameList);
                    compressItems(p, grabber, loc1, loc2, amtCompressed, blockState, frameList);
                    this.cancel();
                }
            }
        };runnable.runTaskTimer(plugin, 0L, 1L);
    }

    private void compressItems(Player p, ItemGrabber grabber, Location loc1, Location loc2, Integer amtCompressed, ArrayList<BlockState> blockState, ArrayList<ItemFrame> itemFrames){
        undoBlockState.put(p, blockState);
        undoFrames.put(p, itemFrames);
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY()) + 1;
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        List<ItemStack> ItemsList = new ArrayList<>();
        ItemsList = grabber.getItems();
        ArrayList<Location> compressShulkerLocs = new ArrayList<>();
        p.sendMessage(plugin.format("&aArea Compressed!\nCompressed " + amtCompressed + " blocks!"));
        for (int x1 = x; x1 < maxX; x1++) {
            for (int y1 = y; y1 < maxY; y1++) {
                for (int z1 = z; z1 < maxZ; z1++) {
                    Location loc = new Location(loc1.getWorld(), x1, y1, z1);
                    if(!loc.getBlock().getType().isAir()) continue;
                    while (!ItemsList.isEmpty()) {
                        if(plugin.compressShulkers && !compressShulkerLocs.contains(loc)){
                            compressShulkerLocs.add(loc);
                        }
                        setShulker(loc);
                        if (loc.getBlock().getState() instanceof ShulkerBox) {
                            Inventory inventory = ((ShulkerBox) loc.getBlock().getState()).getInventory();
                            if (!isRoomInShulker(inventory)) break;
                            inventory.addItem(ItemsList.get(0));
                            ItemsList.remove(0);
                            if(ItemsList.isEmpty()) break;
                        }
                    }
                }
                if(ItemsList.isEmpty()) break;
            }
            if(ItemsList.isEmpty()) break;
        }
        if(plugin.compressShulkers){
            ArrayList<ItemStack> shulkerItemStacks = new ArrayList<>();
            compressShulkersInShulkers(p, compressShulkerLocs, shulkerItemStacks, compressShulkerLocs);
        }
    }

    private void compressShulkersInShulkers(Player p, ArrayList<Location> shulkerLocs, ArrayList<ItemStack> shulkerItemStacks, ArrayList<Location> setShulkerLocs) {
        ArrayList<Location> newShulkerLocs = new ArrayList<>();
        ItemStack shulkerItem = new ItemStack(Material.SHULKER_BOX);
        while (!shulkerLocs.isEmpty()) {
            Location loc = shulkerLocs.get(0);
            ShulkerBox shulkerBlock = (ShulkerBox) loc.getBlock().getState();
            BlockStateMeta blockStateMeta = (BlockStateMeta) shulkerItem.getItemMeta();
            ShulkerBox box = (ShulkerBox) blockStateMeta.getBlockState();
            if (box.getInventory().firstEmpty() == -1) {
                shulkerItemStacks.add(shulkerItem);
                shulkerItem = new ItemStack(Material.SHULKER_BOX);
            } else {
                ItemStack newShulker = new ItemStack(Material.SHULKER_BOX);
                BlockStateMeta bsm = (BlockStateMeta) newShulker.getItemMeta();
                ShulkerBox newBox = (ShulkerBox) bsm.getBlockState();
                newBox.getInventory().setContents(shulkerBlock.getInventory().getContents());
                newBox.update();
                bsm.setBlockState(newBox);
                newShulker.setItemMeta(bsm);
                box.getInventory().addItem(newShulker);
                box.update();
                blockStateMeta.setBlockState(box);
                shulkerItem.setItemMeta(blockStateMeta);
                newShulkerLocs.add(shulkerLocs.get(0));
                shulkerLocs.remove(0);
                loc.getBlock().setType(Material.AIR);
                if(shulkerLocs.isEmpty()){
                    shulkerItemStacks.add(shulkerItem);
                }
            }
        }
        int slot = 1;
        while(!shulkerItemStacks.isEmpty()) {
            Location loc = newShulkerLocs.get(0);
            if(loc.getBlock().getType() != Material.SHULKER_BOX) loc.getBlock().setType(Material.SHULKER_BOX);
            if (slot == 27) {
                slot = 1;
                newShulkerLocs.remove(0);
            } else {
                Inventory inventory = ((ShulkerBox) loc.getBlock().getState()).getInventory();
                inventory.addItem(shulkerItemStacks.get(0));
                p.getInventory().addItem(shulkerItemStacks.get(0));
                shulkerItemStacks.remove(0);
                slot++;
            }
        }
    }

    public void setShulker(Location loc){
        if (loc.getBlock().getType() != Material.SHULKER_BOX) {
            loc.getBlock().setType(Material.SHULKER_BOX);
        }
    }
    public boolean isRoomInShulker(Inventory inventory){
        for (ItemStack i : inventory.getContents()) {
            if(i == null){
                return true;
            }
        }
        return false;
    }

    private void undoCompressing(Player p, ArrayList<BlockState> blockState, ArrayList<ItemFrame> frameList){
        //Blocks and BlockData
        int blockStateLen = blockState.size();
        int blocksPerTick = plugin.maxRollbackBlocksPerTick;
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                int loopNum = 0;
                while(blocksPerTick > loopNum) {
                    if (!blockState.isEmpty()) {
                        blockState.get(0).update(true);
                        blockState.remove(0);
                    } else {
                        p.sendMessage(plugin.format("&aSuccessfully uncompressed " + blockStateLen + " blocks!"));
                        undoItemFrames(p, frameList);
                        this.cancel();
                        break;
                    }
                    loopNum++;
                }
            }
        };runnable.runTaskTimer(plugin, 0L, 1L);
        undoBlockState.remove(p);
        undoItemFrames(p, frameList);
    }
    private void undoItemFrames(Player p, ArrayList<ItemFrame> frameList){
        //ItemFrames and their data
        if(!frameList.isEmpty()){
            int frameLen;
            frameLen = frameList.size();
            int blocksPerTick = plugin.maxRollbackBlocksPerTick;
            BukkitRunnable runnable2 = new BukkitRunnable() {
                @Override
                public void run() {
                    int loopNum = 0;
                    while(loopNum > blocksPerTick) {
                        if (!frameList.isEmpty()) {
                            ItemFrame frame = (ItemFrame) frameList.get(0).getWorld().spawnEntity(frameList.get(0).getLocation(), EntityType.ITEM_FRAME);
                            frame.setRotation(frameList.get(0).getRotation());
                            frame.setItem(frameList.get(0).getItem());
                        } else {
                            this.cancel();
                            break;
                        }
                        loopNum++;
                    }
                }
            };runnable2.runTaskTimer(plugin, 0L, 1L);
            undoFrames.remove(p);
            p.sendMessage(plugin.format("&aSuccessfully uncompressed "+frameLen+" ItemFrames!"));
        }
    }
}