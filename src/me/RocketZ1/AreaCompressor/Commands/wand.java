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
