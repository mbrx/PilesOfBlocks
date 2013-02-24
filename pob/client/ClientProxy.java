package mbrx.pob.client;

import net.minecraftforge.client.MinecraftForgeClient;
import mbrx.pob.CommonProxy;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
	}

	@Override
	public void registerRenderers() {
		MinecraftForgeClient.preloadTexture(ITEMS_PNG);
		MinecraftForgeClient.preloadTexture(BLOCK_PNG);
	}
}
