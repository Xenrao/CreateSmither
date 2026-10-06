package net.xenrao.create_smither.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.xenrao.create_smither.CreateSmither;
import net.xenrao.create_smither.blocks.CreateSmitherBlockRegistries;

public class SmitherPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return CreateSmither.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		HELPER.forComponents(CreateSmitherBlockRegistries.MECHANICAL_SMITHER)
			.addStoryBoard("mechanical_smither/setup", SmitherScenes::setup);
	}

}
