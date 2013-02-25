/* This file is part of PilesOfBlocks.

	Copyright 2013 Mathias Broxvall
	
    PilesOfBlocks is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PilesOfBlocks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PilesOfBlocks.  If not, see <http://www.gnu.org/licenses/>.
*/ 	
    	
package mbrx.pob;

import java.util.logging.Level;
import java.util.logging.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.LanguageRegistry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import java.util.Dictionary;
import java.util.Hashtable;

@Mod(modid = "PilesOfBlocks", name = "PilesOfBlocks", version = "0.1.1")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class PilesOfBlocks {
	// Singleton instance of mod class instansiated by Forge
	@Instance("PilesOfBlocks")
	public static PilesOfBlocks instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "mbrx.pob.client.ClientProxy", serverSide = "mbrx.pob.CommonProxy")
	public static CommonProxy proxy;
	public static EventListener eventListener;

	private static final String ConfigCategory_Generic = "general";
	private static final String ConfigCategory_Blocks = "blocks";

	private static int defaultMetaData = -1;
	private static int defaultStackSize = 1;
	public static int defaultMinPopTime = 20;
	public static int defaultMaxPopTime = 60;
	public static boolean printBlockItemInfo = false;

	//public static BlockItemSetting blockItemSettings[] = new BlockItemSetting[256];
	public static Dictionary<Integer,BlockItemSetting> blockItemSettings = new Hashtable<Integer,BlockItemSetting>();	

	private static Configuration config;
	private static Logger logger;

	class Rule {
		String blockName;
		int metaData;
		int stackSize;
		int minPopTime;
		int maxPopTime;
	}
	private static Rule rules[] = new Rule[256];
	private int nRules;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
	}

	@Init
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();

		config.load();

		config.addCustomCategoryComment(ConfigCategory_Generic,
				"All generic settings for questcraft");
		config.addCustomCategoryComment(
				ConfigCategory_Blocks,
				"Rules for all blocks that should be affected by this.\n"
						+ "All rules have a name of Bi_xyz where i is an integer counting up from 0.\n"
						+ "Each rule has to have a name _Name (eg. B42_Name) that is a regular expression for matching blocks to the rule.\n"
						+ "Additional information for the rules include any of the following:\n"
						+ "  _MetaData: overrides the metadata to give to the block when it pops\n"
						+ "  _StackSize: overrides the maximum allowed stacksize for blockitems matching the rule\n");
		

		Property defaultMetaDataProp = config.get(ConfigCategory_Generic,
				"defaultMetaData", "-1");
		defaultMetaDataProp.comment = "The meta value to assign blocks that a placed back in world."
				+ "Default -1 uses the same as from the given item ID."
				+ "255 is needed for blocks with dual states in the BlockPhysics mod";
		defaultMetaData = defaultMetaDataProp.getInt(-1);

		Property printBlockItemInfoProp = config.get(ConfigCategory_Generic,
				"printBlockInfo", "false");
		printBlockItemInfoProp.comment = "Print a chat message whenver a blockitem is picked up with the corresponding name of the block used for making rules. Default false";
		printBlockItemInfo = printBlockItemInfoProp.getBoolean(false);

		Property defaultStackSizeProp = config.get(ConfigCategory_Generic,
				"defaultStackSize", "1");
		defaultStackSizeProp.comment = "The default maximum size of any of the stacks that are listen in the blocks section, may be overridden for individual blocks";
		defaultStackSize = defaultStackSizeProp.getInt(1);

		Property defaultMinPopTimeProp = config.get(ConfigCategory_Generic,
				"defaultMinPopTime", "20");
		defaultMinPopTimeProp.comment = "The default minimum time (in ticks) before an itemstack matched by the rules will pop-out to become a block";
		defaultMinPopTime = defaultMinPopTimeProp.getInt(20);

		Property defaultMaxPopTimeProp = config.get(ConfigCategory_Generic,
				"defaultMaxPopTime", "20");
		defaultMaxPopTimeProp.comment = "The default minimum time (in ticks) before an itemstack matched by the rules will pop-out to become a block";
		defaultMaxPopTime = defaultMaxPopTimeProp.getInt(20);

		for(nRules=0;nRules<256;) {
			Property nameProp = config.get(ConfigCategory_Blocks, "B"
					+ nRules + "_Name", "");
			Property metaDataProp = config.get(ConfigCategory_Blocks, "B"
					+ nRules + "_MetaData", "");
			Property stackSizeProp = config.get(ConfigCategory_Blocks, "B"
					+ nRules  + "_MaxStackSize", "");
			Property minPopTimeProp = config.get(ConfigCategory_Blocks, "B"
					+ nRules + "_MinPopTime", "");
			Property maxPopTimeProp = config.get(ConfigCategory_Blocks, "B"
					+ nRules + "_MaxPopTime", "");

			if (nameProp == null || nameProp.value.equals(""))
				break;
			
			Rule r = new Rule();
			r.blockName = nameProp.value;
			if(metaDataProp.value == "") r.metaData = -2;
			else r.metaData = metaDataProp.getInt(-2);
			if(stackSizeProp.value == "") r.stackSize = -2;
			else r.stackSize = stackSizeProp.getInt(-2);
			if(minPopTimeProp.value == "") r.minPopTime = -2;
			else r.minPopTime = minPopTimeProp.getInt(-2);
			if(maxPopTimeProp.value == "") r.maxPopTime = -2;
			else r.maxPopTime = maxPopTimeProp.getInt(-2);
			rules[nRules++] = r;
		}

		System.out.println("Finished loading settings, "+nRules+" rules found");
		config.save();

		eventListener = new EventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		if (printBlockItemInfo)
			for (Block b : Block.blocksList) {
				if (b != null && b.getBlockName() != null)
					logger.log(Level.FINEST, "Block #" + b.blockID + " " + b.getBlockName()
							+ " found");
			}

		for (Block b : Block.blocksList) {				
			if (b != null && b.getBlockName() != null) {
				boolean hasRule=false;
				BlockItemSetting bs = new BlockItemSetting();
				bs.metaData = defaultMetaData;
				bs.stackSize = defaultStackSize;
				bs.minPopTime = defaultMinPopTime;
				bs.maxPopTime = defaultMaxPopTime;
				bs.blockID = b.blockID;
				
				for(int i=0;i<nRules;i++) {
					Rule r=rules[i];
					if(b.getBlockName().matches(r.blockName)) {
						hasRule=true;
						if(r.metaData != -2) bs.metaData = r.metaData;
						if(r.stackSize != -2) bs.stackSize = r.stackSize;
						if(r.minPopTime != -2) bs.minPopTime = r.minPopTime;
						if(r.maxPopTime != -2) bs.maxPopTime = r.maxPopTime;
						if (printBlockItemInfo)
							logger.log(Level.FINEST,"Applying rule #"+i+" to "+b.getBlockName());
					}
				}
				if(hasRule) {
					blockItemSettings.put(b.blockID, bs);

					// Find the corresponding item
					boolean found = false;
					for (Item i : Item.itemsList) {
						if (i != null && i.itemID == b.blockID) {
							logger.log(
									Level.INFO,
									"Setting stacksize of "
											+ i.getItemName()
											+ " to "
											+ bs.stackSize);
							i.setMaxStackSize(bs.stackSize);
							found = true;
							break;
						}
					}
					if (!found) {
						System.out
								.println("Could not find any item with itemID="
										+ (b.blockID));
					}					
				}				
			
			}
		}
	}
}
