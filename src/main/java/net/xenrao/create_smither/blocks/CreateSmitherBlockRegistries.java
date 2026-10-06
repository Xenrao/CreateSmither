package net.xenrao.create_smither.blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_smither.CreateSmither;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class CreateSmitherBlockRegistries {

    private static final CreateRegistrate REGISTRATE = CreateSmither.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

 	public static final BlockEntry<MechanicalSmitherBlock> MECHANICAL_SMITHER =
		REGISTRATE.block("mechanical_smither", MechanicalSmitherBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.noOcclusion()
				.mapColor(MapColor.TERRACOTTA_YELLOW))
			.transform(axeOrPickaxe())
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(CStress.setImpact(2.0))
			.onRegister(CreateRegistrate.connectedTextures(SmitherCTBehaviour::new))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntityEntry<MechanicalSmitherBlockEntity> MECHANICAL_SMITHER_BE = REGISTRATE
		.blockEntity("mechanical_crafter", MechanicalSmitherBlockEntity::new)
		.visual(() -> SingleAxisRotatingVisual.of(AllPartialModels.SHAFTLESS_COGWHEEL))
		.validBlocks(CreateSmitherBlockRegistries.MECHANICAL_SMITHER)
		.renderer(() -> MechanicalSmitherRenderer::new)
		.register();


    public static void register() {
    }
}
