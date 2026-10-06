package net.xenrao.create_smither;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.xenrao.create_smither.ponder.SmitherPonderPlugin;

@EventBusSubscriber
public class CreateSmitherClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> PonderIndex.addPlugin(new SmitherPonderPlugin()));
	}

}
