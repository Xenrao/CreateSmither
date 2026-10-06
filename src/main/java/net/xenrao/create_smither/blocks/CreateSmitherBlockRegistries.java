package net.xenrao.create_smither.blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_smither.CreateSmither;
import net.xenrao.create_smither.CreateSmitherConfig;

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
				.onRegister(block -> BlockStressValues.IMPACTS.register(block, CreateSmitherConfig::getSmitherImpact))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntityEntry<MechanicalSmitherBlockEntity> MECHANICAL_SMITHER_BE = REGISTRATE
		.blockEntity("mechanical_smither", MechanicalSmitherBlockEntity::new)
		.visual(() -> SingleAxisRotatingVisual.of(AllPartialModels.SHAFTLESS_COGWHEEL))
		.validBlocks(CreateSmitherBlockRegistries.MECHANICAL_SMITHER)
		.renderer(() -> MechanicalSmitherRenderer::new)
		.register();


    public static void register() {
    }
}
