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

import me.RocketZ1.AreaCompressor.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class wand implements CommandExecutor {
    private Main plugin;

    public wand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("areawand").setExecutor(this);
        plugin.getCommand("aw").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.format("&cOnly players can execute this command!"));
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("areacompressor.wand")) {
            p.sendMessage(plugin.format("&cYou do not have permission to execute this command!"));
            return true;
        }
        if(p.getInventory().firstEmpty() != -1){
            p.getInventory().addItem(plugin.wand());
        }else if(p.getInventory().firstEmpty() == -1){
            p.getWorld().dropItemNaturally(p.getLocation(), plugin.wand());
            p.sendMessage(plugin.format("&cYour inventory was full so the wand has dropped at your feet"));
        }
        p.sendMessage(plugin.format("&dLeft click to select pos1; Right click to select pos2"));
        return false;
    }
}
