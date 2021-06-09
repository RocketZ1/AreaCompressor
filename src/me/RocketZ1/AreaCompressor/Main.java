package me.RocketZ1.AreaCompressor;

import me.RocketZ1.AreaCompressor.Commands.AreaCompressorCmd;
import me.RocketZ1.AreaCompressor.Commands.wand;
import me.RocketZ1.AreaCompressor.events.regionSellect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    //Perm List
    //
    //areacompressor.use
    //areacompressor.wand

    public Map<UUID, Location> pos1 = new HashMap<>();
    public Map<UUID, Location> pos2 = new HashMap<>();

    @Override
    public void onEnable(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new regionSellect(this), this);
        new wand(this);
        new AreaCompressorCmd(this);
    }

    public String format(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public ItemStack wand(){
        ItemStack wand = new ItemStack(Material.GUNPOWDER);
        ItemMeta wand_meta = wand.getItemMeta();
        wand_meta.setDisplayName(format("&bArea Compressor region wand"));
        wand.setItemMeta(wand_meta);
        return wand;
    }

}
