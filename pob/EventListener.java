package mbrx.pob;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class EventListener {

	@ForgeSubscribe
	public void pickedUpEntityItem(EntityItemPickupEvent event) {
		if (PilesOfBlocks.printBlockItemInfo) {
			ItemStack itemStack = event.item.getEntityItem();
			event.entityPlayer
					.addChatMessage("[BigBlocks.cfg]: You have picked an item with name:"
							+ itemStack.getItem().getItemName());
		}
	}

	private BlockItemSetting lookupSetting(int itemID) {
		BlockItemSetting bbis = BlockItemSetting.nullSetting;
		int i;
		for (i = 0; i < PilesOfBlocks.nBigBlockItemSettings; i++)
			if (PilesOfBlocks.blockItemSettings[i].blockID == itemID) {
				bbis = PilesOfBlocks.blockItemSettings[i];
				break;
			}
		return bbis;
	}

	@ForgeSubscribe
	public void blockItemExpired(ItemExpireEvent event) {
		EntityItem eitem = event.entityItem;
		World world = eitem.worldObj;
		// See if this item corresponds to a block item that we are supposed to
		// handle
		BlockItemSetting bbis = lookupSetting(eitem.getEntityItem().itemID);
		if (bbis == BlockItemSetting.nullSetting)
			return;

		int x = (int) Math.round(eitem.posX - 0.5);
		int y = (int) (eitem.posY + 0.0); // was + 0.5
		int z = (int) Math.round(eitem.posZ - 0.5);
		int num = eitem.getEntityItem().stackSize;
		// Find closest empty air block in a pyramid surrounding the
		// expiration point
		for (int radius1 = 0; num > 0 && radius1 < 4; radius1++)
			for (int dy = 0; num > 0 && dy <= 2 * radius1; dy++) {
				int radius2 = (int) (radius1 - Math.round(dy * 0.4));
				// TODO - repeating this inner loop is redundant for the
				// interiors...
				for (int dx = -radius2; num > 0 && dx <= radius2; dx++)
					for (int dz = -radius2; num > 0 && dz <= radius2; dz++)
						if (world.isAirBlock(x + dx, y + dy, z + dz)) {
							if (bbis.metaData != -1)
								world.setBlockAndMetadataWithNotify(x + dx, y
										+ dy, z + dz,
										eitem.getEntityItem().itemID,
										bbis.metaData);
							else
								world.setBlockWithNotify(x + dx, y + dy,
										z + dz, eitem.getEntityItem().itemID);

							num--;
						}
			}

	}

	@ForgeSubscribe
	public void blockItemEnteredChunk(EnteringChunk event) {
		if (event.entity instanceof EntityItem) {
			EntityItem eitem = (EntityItem) event.entity;
			// This seems like a sligthly more complicated way of getting the
			// ItemStack for the object, however it avoids a lot of warnings
			// that would be generated for some EntityItems with no
			// corresponding item stack which enters the chunk sometimes
			ItemStack stack = eitem.getDataWatcher()
					.getWatchableObjectItemStack(10);
			if (stack == null)
				return;
			int itemID = stack.itemID;
			//System.out.println("Item " + itemID + " " + stack.getItemName() + " entering chunk.");
			BlockItemSetting bbis = lookupSetting(itemID);
			if (bbis == BlockItemSetting.nullSetting)
				return;
			eitem.delayBeforeCanPickup = Math.min(eitem.delayBeforeCanPickup,
					bbis.maxPopTime - 10);
			eitem.lifespan = Math.max(bbis.minPopTime,
					eitem.delayBeforeCanPickup + 10);
			System.out.println("Delay: "+eitem.delayBeforeCanPickup+" Life: "+eitem.lifespan);
		}
	}
}
