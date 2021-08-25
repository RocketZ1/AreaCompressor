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

package me.RocketZ1.AreaCompressor;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.RocketZ1.AreaCompressor.Commands.AreaCompressorCmd;
import me.RocketZ1.AreaCompressor.Commands.wand;
import me.RocketZ1.AreaCompressor.Files.ConfigManager;
import me.RocketZ1.AreaCompressor.events.regionSellect;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    public WorldGuardPlugin worldGuardPlugin;
    public WorldEditPlugin worldEditPlugin;
    public GriefPrevention griefPrevention;

    public Map<UUID, Location> pos1 = new HashMap<>();
    public Map<UUID, Location> pos2 = new HashMap<>();
    public Boolean compressShulkers = false;
    public Integer maxBlocksCompressed = -1;
    public Integer maxRollbackBlocksPerTick = 50;
    public ArrayList<Material> blacklistedBlocks = new ArrayList<>();

    public ArrayList<UUID> confirmTimer = new ArrayList<>();

    public ConfigManager config;

    @Override
    public void onEnable(){
        this.config = new ConfigManager(this);
        setupConfig();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new regionSellect(this), this);
        new wand(this);
        new AreaCompressorCmd(this);
        worldGuardPlugin = getWorldGuardPlugin();
        worldEditPlugin = getWorldEditPlugin();
        griefPrevention = getGriefPreventionPlugin();
    }

    public void setupConfig(){
        if(config.getConfig().contains("max-blocks-compressed")){
            try{
                maxBlocksCompressed = config.getConfig().getInt("max-blocks-compressed");
            }catch (Exception e){
                getServer().getLogger().log(Level.INFO, format("max-blocks-compressed in config.yml not found! Defaulting to 1000."));
                maxBlocksCompressed = 1000;
            }
        }else{
            getServer().getLogger().log(Level.INFO, format("max-blocks-compressed in config.yml not found! Defaulting to 1000."));
            maxBlocksCompressed = 1000;
        }
        if(config.getConfig().contains("shulkers-in-shulkers")){
            try{
                compressShulkers = config.getConfig().getBoolean("shulkers-in-shulkers");
            }catch (Exception e){
                getServer().getLogger().log(Level.INFO, format("shulkers-in-shulkers in config.yml not found! Defaulting to false."));
                compressShulkers = false;
            }
        }else{
            getServer().getLogger().log(Level.INFO, format("shulkers-in-shulkers in config.yml not found! Defaulting to false."));
            compressShulkers = false;
        }
        if(config.getConfig().contains("rollback-blocks-per-tick")){
            try{
                maxRollbackBlocksPerTick = config.getConfig().getInt("rollback-blocks-per-tick");
            }catch (Exception e){
                getServer().getLogger().log(Level.INFO, format("rollback-blocks-per-tick in config.yml not found! Defaulting to 50."));
                maxRollbackBlocksPerTick = 50;
            }
        }else{
            getServer().getLogger().log(Level.INFO, format("rollback-blocks-per-tick in config.yml not found! Defaulting to 50."));
            maxRollbackBlocksPerTick = 50;
        }

        if(config.getConfig().contains("blacklisted-blocks")){
            List<String> list = config.getConfig().getStringList("blacklisted-blocks");
            if(!list.isEmpty()){
                list.forEach(mat ->{
                    if(Material.getMaterial(mat) != null){
                        blacklistedBlocks.add(Material.getMaterial(mat));
                    }
                });
            }
        }else{
            getServer().getLogger().log(Level.INFO, format("blacklisted-blocks in config.yml not found! There are no BlackListed blocks."));
        }
        blacklistedBlocks.add(Material.AIR);
        blacklistedBlocks.add(Material.CAVE_AIR);
        blacklistedBlocks.add(Material.VOID_AIR);

    }

    public boolean checkRegion(Player p, com.sk89q.worldedit.util.Location loc) {
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
        RegionContainer container = getWorldGuard().getPlatform().getRegionContainer();
        RegionManager regions = container.get(localPlayer.getWorld());
        if(regions == null)
            return false;
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for(ProtectedRegion region : set){
            if(region.contains(loc.toVector().toBlockPoint())){
                if(region.getOwners().contains(localPlayer.getUniqueId())) return false;
                if(region.getMembers().contains(localPlayer.getUniqueId())) return false;
                return true;
            }
        }
        return false;
    }

    private WorldGuardPlugin getWorldGuardPlugin() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
    private WorldGuard getWorldGuard() {
        WorldGuard worldGuard = WorldGuard.getInstance();
        if (worldGuard == null || !(worldGuard instanceof WorldGuard)) {
            return null;
        }
        return worldGuard;
    }

    private WorldEditPlugin getWorldEditPlugin() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldEdit");
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
        return (WorldEditPlugin) plugin;
    }


    public String format(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public final ItemStack wand(){
        ItemStack wand = new ItemStack(Material.GUNPOWDER);
        ItemMeta wand_meta = wand.getItemMeta();
        wand_meta.setDisplayName(format("&bArea Compressor region wand"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(format("&dLeft click to select pos1; Right click to select pos2"));
        wand_meta.setLore(lore);
        wand.setItemMeta(wand_meta);
        return wand;
    }

    private GriefPrevention getGriefPreventionPlugin(){
        Plugin plugin = this.getServer().getPluginManager().getPlugin("GriefPrevention");
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof GriefPrevention)) {
            return null; // Maybe you want throw an exception instead
        }
        return (GriefPrevention) plugin;
    }

    public boolean checkClaimAccess(Player p, Location loc){
        for(Claim claim : griefPrevention.dataStore.getClaims()){
            if(claim.contains(loc, true, true)){
                if(griefPrevention.dataStore.getPlayerData(p.getUniqueId()).ignoreClaims) return true;
                if(claim.allowAccess(p) != null){
                    return false;
                }else{
                    return true;
                }
            }
        }
        return true;
    }

}
