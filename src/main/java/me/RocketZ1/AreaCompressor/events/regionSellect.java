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
            if(plugin.confirmTimer.contains(p.getUniqueId())) plugin.confirmTimer.remove(p.getUniqueId());
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Location pos2Block = e.getClickedBlock().getLocation();
            plugin.pos2.put(p.getUniqueId(), pos2Block);
            p.sendMessage(plugin.format("&dPos2 set to (" + x + ", " + y + ", " + z + ")"));
            if(plugin.confirmTimer.contains(p.getUniqueId())) plugin.confirmTimer.remove(p.getUniqueId());
            return;
        }
    }
}