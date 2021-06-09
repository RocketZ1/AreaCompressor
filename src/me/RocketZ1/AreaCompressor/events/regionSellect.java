package me.RocketZ1.AreaCompressor.events;

import me.RocketZ1.AreaCompressor.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class regionSellect implements Listener {

    private Main plugin;

    public regionSellect(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void getPos(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!p.hasPermission("areacompressor.wand")) return;
        if(e.getItem() == null || e.getItem().getType() == Material.AIR) return;
        if(!e.getItem().isSimilar(plugin.wand())) return;
        int x;
        int y;
        int z;
        try {
            x = e.getClickedBlock().getX();
            y = e.getClickedBlock().getY();
            z = e.getClickedBlock().getZ();
        }catch (NullPointerException exception){
            return;
        }
        if(e.getAction() == Action.LEFT_CLICK_BLOCK){
            Location pos1Block = e.getClickedBlock().getLocation();
            plugin.pos1.put(p.getUniqueId(), pos1Block);
            p.sendMessage(plugin.format("&dPos1 set to (" + x + ", " + y + ", " + z + ")"));
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Location pos2Block = e.getClickedBlock().getLocation();
            plugin.pos2.put(p.getUniqueId(), pos2Block);
            p.sendMessage(plugin.format("&dPos2 set to (" + x + ", " + y + ", " + z + ")"));
        }
    }
}
