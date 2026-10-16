package net.xenrao.create_smither.compat.jei;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xenrao.create_smither.CreateSmither;
import net.xenrao.create_smither.blocks.CreateSmitherBlockRegistries;

// Yanked from Create's MechanicalCraftingCategory lol
@ParametersAreNonnullByDefault
public class MechanicalSmithingCategory implements IRecipeCategory<SmitherJeiRecipe> {

	public static final RecipeType<SmitherJeiRecipe> TYPE =
		RecipeType.create(CreateSmither.MODID, "mechanical_smithing", SmitherJeiRecipe.class);

	private static final int WIDTH = 177;
	private static final int HEIGHT = 107;

	// input row
	private static final int INPUT_X = 25;
	private static final int INPUT_Y = 44;
	private static final int SLOT_SPACING = 19;

	private final IDrawable icon;
	private final AnimatedSmither smither = new AnimatedSmither();

	public MechanicalSmithingCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
			new ItemStack(CreateSmitherBlockRegistries.MECHANICAL_SMITHER.get()));
	}

	@Override
	public RecipeType<SmitherJeiRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable(CreateSmither.MODID + ".recipe.mechanical_smithing");
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName(SmitherJeiRecipe recipe) {
		return recipe.id();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SmitherJeiRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 81)
			.addItemStack(recipe.result());

		addInput(builder, 0, recipe.template());
		addInput(builder, 1, recipe.base());
		addInput(builder, 2, recipe.addition());
	}

	private static void addInput(IRecipeLayoutBuilder builder, int index, List<ItemStack> stacks) {
		if (stacks.isEmpty())
			return;
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1 + index * SLOT_SPACING, INPUT_Y + 1)
			.addItemStacks(stacks);
	}

	@Override
	public void draw(SmitherJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
		double mouseY) {
		for (int i = 0; i < 3; i++)
			AllGuiTextures.JEI_SLOT.render(graphics, INPUT_X + i * SLOT_SPACING, INPUT_Y);

		AllGuiTextures.JEI_SLOT.render(graphics, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 128, 59);
		smither.draw(graphics, 129, 25);

		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(0, 0, 300);
		// TODO: make that damn string static when i add support for mechanical smithing recipe
		graphics.drawString(Minecraft.getInstance().font, "3", 142, 39, 0xFFFFFF);
		pose.popPose();
	}

}
