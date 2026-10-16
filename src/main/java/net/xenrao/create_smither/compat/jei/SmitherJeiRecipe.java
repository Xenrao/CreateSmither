package net.xenrao.create_smither.compat.jei;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record SmitherJeiRecipe(ResourceLocation id, List<ItemStack> template, List<ItemStack> base,
	List<ItemStack> addition, ItemStack result) {
}
