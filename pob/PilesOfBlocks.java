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

@Mod(modid = "PilesOfBlocks", name = "PilesOfBlocks", version = "0.1.0")
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

	public static BlockItemSetting blockItemSettings[] = new BlockItemSetting[256];
	public static int nBigBlockItemSettings;

	private static Configuration config;
	private static Logger logger;

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

		nBigBlockItemSettings = 0;

		if (printBlockItemInfo)
			for (Block b : Block.blocksList) {
				if (b != null && b.getBlockName() != null)
					logger.log(Level.FINEST, "Block " + b.getBlockName()
							+ " found");
			}

		for(int rule=0;rule<256;rule++) {
			Property nameProp = config.get(ConfigCategory_Blocks, "B"
					+ rule + "_Name", "");
			Property metaDataProp = config.get(ConfigCategory_Blocks, "B"
					+ rule + "_MetaData", "");
			Property stackSizeProp = config.get(ConfigCategory_Blocks, "B"
					+ rule  + "_MaxStackSize", "");
			Property minPopTimeProp = config.get(ConfigCategory_Blocks, "B"
					+ rule + "_MinPopTime", "");
			Property maxPopTimeProp = config.get(ConfigCategory_Blocks, "B"
					+ rule + "_MaxPopTime", "");

			if (nameProp == null || nameProp.value.equals(""))
				break;


			for (Block b : Block.blocksList) {				
				if (b != null && b.getBlockName() != null
						&& b.getBlockName().matches(nameProp.value)) {
					logger.log(Level.INFO, "Block " + b.getBlockName()
							+ " matches rule #" + nBigBlockItemSettings + " "
							+ nameProp.value);
					
					BlockItemSetting bs = new BlockItemSetting();
					blockItemSettings[nBigBlockItemSettings] = bs;
					bs.name = nameProp.value;
					bs.blockID = -1;
					nBigBlockItemSettings++;
					
					bs.blockID = b.blockID;
					if (metaDataProp.value.equals(""))
						bs.metaData = defaultMetaData;
					else
						bs.metaData = metaDataProp.getInt(defaultMetaData);
					if (stackSizeProp.value.equals("")) bs.stackSize = defaultStackSize;
					else bs.stackSize = stackSizeProp.getInt(defaultStackSize);
					if (minPopTimeProp.value.equals("")) bs.minPopTime = defaultMinPopTime;
					else bs.minPopTime = minPopTimeProp.getInt(defaultMinPopTime);
					if (maxPopTimeProp.value.equals("")) bs.maxPopTime = defaultMaxPopTime;
					else bs.maxPopTime = minPopTimeProp.getInt(defaultMaxPopTime);
					
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
/*			if (bigBlockItemSettings[nBigBlockItemSettings].blockID == -1) {
				logger.log(Level.WARNING, "failed to find block named: "
						+ nameProp.value);
			}
			*/
		}

		System.out.println("Finished loading settings!");
		config.save();

		eventListener = new EventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);

	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
	}

}
