package me.RocketZ1.AreaCompressor.Commands;

import me.RocketZ1.AreaCompressor.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AreaCompressorCmd implements CommandExecutor {

    private Main plugin;

    public AreaCompressorCmd(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("areacompressor").setExecutor(this);
        plugin.getCommand("ac").setExecutor(this);
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
        if (args.length != 0) {
            p.sendMessage(plugin.format("&cInvalid Command! Try /areacompressor"));
            return true;
        }
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
        if(confirmTimer.contains(p.getUniqueId())){
            getArea(p, loc1, loc2);
            confirmTimer.remove(p.getUniqueId());
        }else{
            isChunkLoadedCheck(p, loc1, loc2);
        }
        //compressarea (cords 1) (cords 2)
        return false;
    }

    public ArrayList<UUID> confirmTimer = new ArrayList<>();

    public void isChunkLoadedCheck(Player p, Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            p.sendMessage(plugin.format("&cError: Please make both loc1 & loc2 in the same world!"));
            return;
        }
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        boolean chunkUnloaded = false;
        for (int x1 = x; x1 < maxX; x1++) {
            for (int z1 = z; z1 < maxZ; z1++) {
                Location loc = new Location(p.getWorld(), x1, loc1.getBlockY(), z1);
                if (!loc.getChunk().isLoaded()) {
                    p.sendMessage(plugin.format("&cError: Cannot compress area if a chunk in the area is unloaded!"));
                    chunkUnloaded = true;
                    break;
                }
            }
        }
        if (chunkUnloaded) return;
        p.sendMessage(plugin.format("&aAre you sure you want to compress this area? Type the command again within 10 seconds to confirm!"));
        confirmTimer.add(p.getUniqueId());
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(confirmTimer.contains(p.getUniqueId())){
                    confirmTimer.remove(p.getUniqueId());
                }
                cancel();
            }
        };runnable.runTaskLater(plugin, 20 * 10);
    }


    public void getArea(Player p, Location loc1, Location loc2) {
        ArrayList<Location> itemFrameLoc = new ArrayList<>();
        List<ItemStack> ItemsList = new ArrayList<>();
        int x = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX()) + 1;
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY()) + 1;
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ()) + 1;
        int amtCompressed = 0;
        ItemGrabber grabber = new ItemGrabber();

        for (int x1 = x; x1 < maxX; x1++) {
            for (int y1 = y; y1 < maxY; y1++) {
                for (int z1 = z; z1 < maxZ; z1++) {
                    Location loc = new Location(p.getWorld(), x1, y1, z1);
                    if (loc.getBlock().getType().isAir() || loc.getBlock().getType() == Material.WATER ||
                            loc.getBlock().getType() == Material.LAVA)
                        continue;
                    amtCompressed++;
                    if (loc.getBlock().getState() instanceof Container) {
                        Inventory inventory = ((Container) loc.getBlock().getState()).getInventory();
                        grabber.addItems(inventory.getContents());
                        inventory.clear();
                    }
                    ItemStack item = new ItemStack(loc.getBlock().getType());
                    grabber.addItem(item);
                    loc.getBlock().setType(Material.AIR);
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
                        if (e instanceof ItemFrame) {
                            ItemFrame itemFrame = (ItemFrame) e;
                            if (itemFrameLoc.contains(itemFrame.getLocation())) continue;
                            grabber.addItem(itemFrame.getItem());
                            grabber.addItem(new ItemStack(Material.ITEM_FRAME));
                            itemFrame.remove();
                            itemFrameLoc.add(itemFrame.getLocation());
                        }
                    }
                }
            }
        }
        ItemsList = grabber.getItems();
        p.sendMessage(plugin.format("&aArea Compressed!\nCompressed " + amtCompressed + " blocks!"));
        for (int x1 = x; x1 < maxX; x1++) {
            for (int y1 = y; y1 < maxY; y1++) {
                for (int z1 = z; z1 < maxZ; z1++) {
                    while (!ItemsList.isEmpty()) {
                        Location loc = new Location(loc1.getWorld(), x1, y1, z1);
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
}