package net.xenrao.create_smither.blocks;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SmitherGridHandler {

	/**
	 * false: the 3 items can be in any order (all 6 permutations are tried against vanilla smithing recipes).
	 * true: line order is used. Seen from the front of the smithers: left -> right (horizontal line)
	 * or top -> bottom (vertical line) = template, base, addition.
	 */
	public static final boolean STRICT_ORDER = false;

	private static final int[][] PERMUTATIONS = { { 0, 1, 2 }, { 0, 2, 1 }, { 1, 0, 2 }, { 1, 2, 0 }, { 2, 0, 1 },
		{ 2, 1, 0 } };

	// Chain traversal

	public static List<MechanicalSmitherBlockEntity> getAllCraftersOfChain(MechanicalSmitherBlockEntity root) {
		return getAllCraftersOfChainIf(root, Predicates.alwaysTrue());
	}

	public static List<MechanicalSmitherBlockEntity> getAllCraftersOfChainIf(MechanicalSmitherBlockEntity root,
		Predicate<MechanicalSmitherBlockEntity> test) {
		return getAllCraftersOfChainIf(root, test, false);
	}

	public static List<MechanicalSmitherBlockEntity> getAllCraftersOfChainIf(MechanicalSmitherBlockEntity root,
		Predicate<MechanicalSmitherBlockEntity> test, boolean poweredStart) {
		List<MechanicalSmitherBlockEntity> crafters = new ArrayList<>();
		List<Pair<MechanicalSmitherBlockEntity, MechanicalSmitherBlockEntity>> frontier = new ArrayList<>();
		Set<MechanicalSmitherBlockEntity> visited = new HashSet<>();
		frontier.add(Pair.of(root, null));

		boolean empty = false;
		boolean allEmpty = true;

		while (!frontier.isEmpty()) {
			Pair<MechanicalSmitherBlockEntity, MechanicalSmitherBlockEntity> pair = frontier.remove(0);
			MechanicalSmitherBlockEntity current = pair.getKey();
			MechanicalSmitherBlockEntity last = pair.getValue();

			if (visited.contains(current))
				return null;
			if (!(test.test(current)))
				empty = true;
			else
				allEmpty = false;

			crafters.add(current);
			visited.add(current);

			MechanicalSmitherBlockEntity target = getTargetingCrafter(current);
			if (target != last && target != null)
				frontier.add(Pair.of(target, current));
			for (MechanicalSmitherBlockEntity preceding : getPrecedingCrafters(current))
				if (preceding != last)
					frontier.add(Pair.of(preceding, current));
		}

		return empty && !poweredStart || allEmpty ? null : crafters;
	}

	public static MechanicalSmitherBlockEntity getTargetingCrafter(MechanicalSmitherBlockEntity crafter) {
		BlockState state = crafter.getBlockState();
		if (!isCrafter(state))
			return null;

		BlockPos targetPos = crafter.getBlockPos()
			.relative(MechanicalSmitherBlock.getTargetDirection(state));
		MechanicalSmitherBlockEntity targetBE = SmitherHelper.getCrafter(crafter.getLevel(), targetPos);
		if (targetBE == null)
			return null;

		BlockState targetState = targetBE.getBlockState();
		if (!isCrafter(targetState))
			return null;
		if (state.getValue(HORIZONTAL_FACING) != targetState.getValue(HORIZONTAL_FACING))
			return null;
		return targetBE;
	}

	public static List<MechanicalSmitherBlockEntity> getPrecedingCrafters(MechanicalSmitherBlockEntity crafter) {
		BlockPos pos = crafter.getBlockPos();
		Level world = crafter.getLevel();
		List<MechanicalSmitherBlockEntity> crafters = new ArrayList<>();
		BlockState blockState = crafter.getBlockState();
		if (!isCrafter(blockState))
			return crafters;

		Direction blockFacing = blockState.getValue(HORIZONTAL_FACING);
		Direction blockPointing = MechanicalSmitherBlock.getTargetDirection(blockState);
		for (Direction facing : Iterate.directions) {
			if (blockFacing.getAxis() == facing.getAxis())
				continue;
			if (blockPointing == facing)
				continue;

			BlockPos neighbourPos = pos.relative(facing);
			BlockState neighbourState = world.getBlockState(neighbourPos);
			if (!isCrafter(neighbourState))
				continue;
			if (MechanicalSmitherBlock.getTargetDirection(neighbourState) != facing.getOpposite())
				continue;
			if (blockFacing != neighbourState.getValue(HORIZONTAL_FACING))
				continue;
			MechanicalSmitherBlockEntity be = SmitherHelper.getCrafter(world, neighbourPos);
			if (be == null)
				continue;

			crafters.add(be);
		}

		return crafters;
	}

	private static boolean isCrafter(BlockState state) {
		return CreateSmitherBlockRegistries.MECHANICAL_SMITHER.has(state);
	}

	// Recipe matching

	/**
	 * @return the smithing result, or null if the grid is not a valid 1x3 line / no recipe matches
	 */
	public static ItemStack tryToApplyRecipe(Level level, GroupedItems items) {
		items.calcStats();

		// exactly 3 slots, forming a straight line
		if (items.grid.size() != 3)
			return null;
		boolean horizontal = items.width == 3 && items.height == 1;
		boolean vertical = items.width == 1 && items.height == 3;
		if (!horizontal && !vertical)
			return null;

		List<Map.Entry<Pair<Integer, Integer>, ItemStack>> cells = new ArrayList<>(items.grid.entrySet());
		if (horizontal)
			cells.sort(Comparator.comparingInt(e -> e.getKey()
				.getLeft()));
		else
			cells.sort(Comparator.comparingInt((Map.Entry<Pair<Integer, Integer>, ItemStack> e) -> e.getKey()
				.getRight())
				.reversed());

		List<ItemStack> stacks = new ArrayList<>(3);
		for (Map.Entry<Pair<Integer, Integer>, ItemStack> cell : cells)
			stacks.add(cell.getValue());

		int[][] orders = STRICT_ORDER ? new int[][] { PERMUTATIONS[0] } : PERMUTATIONS;
		for (int[] order : orders) {
			SmithingRecipeInput input = new SmithingRecipeInput(stacks.get(order[0])
				.copy(),
				stacks.get(order[1])
					.copy(),
				stacks.get(order[2])
					.copy());
			Optional<RecipeHolder<SmithingRecipe>> recipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.SMITHING, input, level);
			if (recipe.isEmpty())
				continue;
			ItemStack result = recipe.get()
				.value()
				.assemble(input, level.registryAccess());
			if (!result.isEmpty())
				return result;
		}
		return null;
	}

	// Grid container

	public static class GroupedItems {
		Map<Pair<Integer, Integer>, ItemStack> grid = new HashMap<>();
		int minX, minY, maxX, maxY, width, height;
		boolean statsReady;

		public GroupedItems() {}

		public GroupedItems(ItemStack stack) {
			grid.put(Pair.of(0, 0), stack);
		}

		public void mergeOnto(GroupedItems other, Pointing pointing) {
			int xOffset = pointing == Pointing.LEFT ? 1 : pointing == Pointing.RIGHT ? -1 : 0;
			int yOffset = pointing == Pointing.DOWN ? 1 : pointing == Pointing.UP ? -1 : 0;
			grid.forEach(
				(pair, stack) -> other.grid.put(Pair.of(pair.getKey() + xOffset, pair.getValue() + yOffset), stack));
			other.statsReady = false;
		}

		public void write(CompoundTag nbt, HolderLookup.Provider registries) {
			ListTag gridNBT = new ListTag();
			grid.forEach((pair, stack) -> {
				CompoundTag entry = new CompoundTag();
				entry.putInt("x", pair.getKey());
				entry.putInt("y", pair.getValue());
				entry.put("item", stack.saveOptional(registries));
				gridNBT.add(entry);
			});
			nbt.put("Grid", gridNBT);
		}

		public static GroupedItems read(CompoundTag nbt, HolderLookup.Provider registries) {
			GroupedItems items = new GroupedItems();
			ListTag gridNBT = nbt.getList("Grid", Tag.TAG_COMPOUND);
			gridNBT.forEach(inbt -> {
				CompoundTag entry = (CompoundTag) inbt;
				int x = entry.getInt("x");
				int y = entry.getInt("y");
				ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound("item"));
				items.grid.put(Pair.of(x, y), stack);
			});
			return items;
		}

		public void calcStats() {
			if (statsReady)
				return;
			statsReady = true;

			minX = 0;
			minY = 0;
			maxX = 0;
			maxY = 0;

			for (Pair<Integer, Integer> pair : grid.keySet()) {
				int x = pair.getKey();
				int y = pair.getValue();
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
			}

			width = maxX - minX + 1;
			height = maxY - minY + 1;
		}

		public boolean onlyEmptyItems() {
			for (ItemStack stack : grid.values())
				if (!stack.isEmpty())
					return false;
			return true;
		}

	}

}
