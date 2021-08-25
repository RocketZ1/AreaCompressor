# AreaCompressor
Main Github Link: https://github.com/RocketZ1/AreaCompressor

## Simple Description
This is a plugin which adds the /areacompressor command that takes a selected region and will compress the area into shulker boxes. It compresses blocks, items in containers, and item frames!

## Commands

Command | Permissions | Description
----- | ---- | ------
/areacompressor | areacompressor.use | This command compresses the selected region into shulker boxes
/areawand | areacompressor.wand | This command gives the executing player the wand to select the region

## Command Aliases
Command | Alias
---- | ----
/areacompressor | /ac 
/areawand | /aw  

# How to use the Area Compressor Plugin
You obtain a wand from /areawand or /aw, and you left click a block to select the first position, and right click another block to select the second position. Once you have selected 2 positions, you made a region. Type /areacompressor or /ac to compress the area into shulker boxes, you will need to type the command again within 10 seconds to confirm you want to compress your selected region.

## Wand
You will need the permission "areacompressor.wand" for the wand to work, as well as to use the /areawand or /aw command.

## Additional Information
- To prevent major lag, your selected region will only compress if all the chunks in that region are loaded, this is to prevent unnessary strain on servers and prevent users from compressing massive areas which may cause lag.
- Both selected positions must be in the same world to make a valid region.
- The plugin does not remove water or lava when compressing an area, be careful not to flood your area.
