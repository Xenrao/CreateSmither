package net.xenrao.create_smither;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xenrao.create_smither.blocks.CreateSmitherBlockRegistries;

public class CreateSmitherCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    CreateSmither.MODID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_SMITHER_TAB =
            TABS.register("create_smither",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.create_smither"))
                            .icon(CreateSmitherBlockRegistries.MECHANICAL_SMITHER::asStack)
                            .displayItems((parameters, output) -> {
                                output.accept(CreateSmitherBlockRegistries.MECHANICAL_SMITHER.asStack());
                            })
                            .build()
            );
}
