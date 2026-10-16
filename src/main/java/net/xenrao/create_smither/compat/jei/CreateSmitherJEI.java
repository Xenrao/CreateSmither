package net.xenrao.create_smither.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.xenrao.create_smither.CreateSmither;
import net.xenrao.create_smither.blocks.CreateSmitherBlockRegistries;

@JeiPlugin
@ParametersAreNonnullByDefault
public class CreateSmitherJEI implements IModPlugin {

	private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CreateSmither.MODID, "jei_plugin");

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new MechanicalSmithingCategory(registration.getJeiHelpers()
			.getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;

		List<ItemStack> allItems = new ArrayList<>();
		BuiltInRegistries.ITEM.forEach(item -> {
			ItemStack stack = new ItemStack(item);
			if (!stack.isEmpty())
				allItems.add(stack);
		});

		List<SmitherJeiRecipe> recipes = new ArrayList<>();
		for (RecipeHolder<SmithingRecipe> holder : level.getRecipeManager()
			.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING)) {
			SmithingRecipe recipe = holder.value();

			if (recipe instanceof SmithingTrimRecipe)
				continue;
			ItemStack result = recipe.getResultItem(level.registryAccess());
			if (result.isEmpty())
				continue;

			List<ItemStack> template = matching(allItems, recipe::isTemplateIngredient);
			List<ItemStack> base = matching(allItems, recipe::isBaseIngredient);
			List<ItemStack> addition = matching(allItems, recipe::isAdditionIngredient);
			if (template.isEmpty() && base.isEmpty() && addition.isEmpty())
				continue;

			recipes.add(new SmitherJeiRecipe(holder.id(), template, base, addition, result.copy()));
		}

		registration.addRecipes(MechanicalSmithingCategory.TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ItemStack smither = new ItemStack(CreateSmitherBlockRegistries.MECHANICAL_SMITHER.get());
		// recipes that probably I will forget to add support.
		registration.addRecipeCatalyst(smither, MechanicalSmithingCategory.TYPE);
		// smithing recipes
		registration.addRecipeCatalyst(smither, RecipeTypes.SMITHING);
	}

	private static List<ItemStack> matching(List<ItemStack> all, Predicate<ItemStack> test) {
		List<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : all)
			if (test.test(stack))
				result.add(stack);
		return result;
	}

}
