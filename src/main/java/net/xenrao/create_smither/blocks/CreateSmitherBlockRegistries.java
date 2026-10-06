package net.xenrao.create_smither.blocks;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_smither.CreateSmither;

public class CreateSmitherBlockRegistries {

    private static final CreateRegistrate REGISTRATE = CreateSmither.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }
    /*
    public static final BlockEntry<DelayedTransporterBlock> DELAYED_TRANSPORTER =
            REGISTRATE.block("delayed_transporter", DelayedTransporterBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {
                    })
                    .setData(ProviderType.LANG, (ctx, prov) -> {
                    })
                    .item()
                    .model((ctx, prov) -> {
                    })
                    .build()
                    .register();

     */

    /*
    public static final BlockEntityEntry<DelayedTransporterBlockEntity> DELAYED_TRANSPORTER =
            REGISTRATE.blockEntity("delayed_transporter", DelayedTransporterBlockEntity::new)
                    .validBlocks(RandomBulkSheetBlocks.DELAYED_TRANSPORTER)
                    .register();

     */

    public static void register() {
    }
}
