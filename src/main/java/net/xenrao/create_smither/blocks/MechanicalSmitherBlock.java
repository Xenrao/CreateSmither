package net.xenrao.create_smither.blocks;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.xenrao.create_smither.blocks.MechanicalSmitherBlockEntity.Phase;

public class MechanicalSmitherBlock extends HorizontalKineticBlock
	implements IBE<MechanicalSmitherBlockEntity>, ICogWheel {

	public static final EnumProperty<Pointing> POINTING = EnumProperty.create("pointing", Pointing.class);

	public MechanicalSmitherBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POINTING, Pointing.UP));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POINTING));
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		BlockPos placedOnPos = context.getClickedPos()
			.relative(face.getOpposite());
		BlockState blockState = context.getLevel()
			.getBlockState(placedOnPos);

		if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown())) {
			BlockState stateForPlacement = super.getStateForPlacement(context);
			Direction direction = stateForPlacement.getValue(HORIZONTAL_FACING);
			if (direction != face)
				stateForPlacement = stateForPlacement.setValue(POINTING, pointingFromFacing(face, direction));
			return stateForPlacement;
		}

		Direction otherFacing = blockState.getValue(HORIZONTAL_FACING);
		Pointing pointing = pointingFromFacing(face, otherFacing);
		return defaultBlockState().setValue(HORIZONTAL_FACING, otherFacing)
			.setValue(POINTING, pointing);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == newState.getBlock()) {
			if (getTargetDirection(state) != getTargetDirection(newState)) {
				MechanicalSmitherBlockEntity crafter = SmitherHelper.getCrafter(worldIn, pos);
				if (crafter != null)
					crafter.blockChanged();
			}
		}

		if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
			MechanicalSmitherBlockEntity crafter = SmitherHelper.getCrafter(worldIn, pos);
			if (crafter != null && !isMoving)
				crafter.ejectWholeGrid();
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	public static Pointing pointingFromFacing(Direction pointingFace, Direction blockFacing) {
		boolean positive = blockFacing.getAxisDirection() == AxisDirection.POSITIVE;

		Pointing pointing = pointingFace == Direction.DOWN ? Pointing.UP : Pointing.DOWN;
		if (pointingFace == Direction.EAST)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.WEST)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		if (pointingFace == Direction.NORTH)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.SOUTH)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		return pointing;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (context.getClickedFace() == state.getValue(HORIZONTAL_FACING)) {
			if (!context.getLevel().isClientSide)
				KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
					state.cycle(POINTING));
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof MechanicalSmitherBlockEntity crafter))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if (AllBlocks.MECHANICAL_ARM.isIn(stack))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		boolean isHand = stack.isEmpty() && hand == InteractionHand.MAIN_HAND;
		boolean wrenched = AllItems.WRENCH.isIn(stack);

		if (hitResult.getDirection() == state.getValue(HORIZONTAL_FACING)) {

			if (crafter.phase != Phase.IDLE && !wrenched) {
				crafter.ejectWholeGrid();
				return ItemInteractionResult.SUCCESS;
			}

			if (crafter.phase == Phase.IDLE && !isHand && !wrenched) {
				if (level.isClientSide)
					return ItemInteractionResult.SUCCESS;

				IItemHandler capability =
					level.getCapability(Capabilities.ItemHandler.BLOCK, crafter.getBlockPos(), null);
				if (capability == null)
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				ItemStack remainder = ItemHandlerHelper.insertItem(capability, stack.copy(), false);
				if (remainder.getCount() != stack.getCount())
					player.setItemInHand(hand, remainder);
				return ItemInteractionResult.SUCCESS;
			}

			ItemStack inSlot = crafter.getInventory()
				.getItem(0);
			if (inSlot.isEmpty())
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (!isHand && !ItemStack.isSameItemSameComponents(stack, inSlot))
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (level.isClientSide)
				return ItemInteractionResult.SUCCESS;
			player.getInventory()
				.placeItemBackInInventory(inSlot);
			crafter.getInventory()
				.setStackInSlot(0, ItemStack.EMPTY);
			return ItemInteractionResult.SUCCESS;
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	public static Direction getTargetDirection(BlockState state) {
		if (!CreateSmitherBlockRegistries.MECHANICAL_SMITHER.has(state))
			return Direction.UP;
		Direction facing = state.getValue(HORIZONTAL_FACING);
		Pointing point = state.getValue(POINTING);
		Vec3 targetVec = new Vec3(0, 1, 0);
		targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
		targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
		return Direction.getNearest(targetVec.x, targetVec.y, targetVec.z);
	}

	public static boolean isValidTarget(Level world, BlockPos targetPos, BlockState crafterState) {
		BlockState targetState = world.getBlockState(targetPos);
		if (!world.isLoaded(targetPos))
			return false;
		if (!CreateSmitherBlockRegistries.MECHANICAL_SMITHER.has(targetState))
			return false;
		if (crafterState.getValue(HORIZONTAL_FACING) != targetState.getValue(HORIZONTAL_FACING))
			return false;
		if (Math.abs(crafterState.getValue(POINTING)
			.getXRotation()
			- targetState.getValue(POINTING)
				.getXRotation()) == 180)
			return false;
		return true;
	}

	@Override
	public Class<MechanicalSmitherBlockEntity> getBlockEntityClass() {
		return MechanicalSmitherBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends MechanicalSmitherBlockEntity> getBlockEntityType() {
		return CreateSmitherBlockRegistries.MECHANICAL_SMITHER_BE.get();
	}

}
