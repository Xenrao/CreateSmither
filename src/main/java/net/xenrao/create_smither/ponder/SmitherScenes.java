package net.xenrao.create_smither.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xenrao.create_smither.blocks.CreateSmitherBlockRegistries;
import net.xenrao.create_smither.blocks.MechanicalSmitherBlock;
import net.xenrao.create_smither.blocks.MechanicalSmitherBlockEntity;

public class SmitherScenes {

	/**
	 * Structure: assets/create_smither/ponder/mechanical_smither/setup.nbt (size 5 x 3 x 5)
	 *   y=0                : 5x5 base plate
	 *   y=1                : Andesite Scaffolding at x=0..4,z=2 and x=4,z=3..4
	 *   y=2, z=2           : x=0 Depot | x=1,2,3 Mechanical Smither (facing=north, pointing=right)
	 *   y=2, x=4           : z=2 Cogwheel (axis z), z=3 and z=4 Shaft (axis z)
	 */
	public static void setup(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_smither", "Automating Smithing Recipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().layer(1), Direction.DOWN);
		scene.idle(5);

		Selection smithers = util.select().fromTo(1, 2, 2, 3, 2, 2);
		Selection kinetics = util.select().fromTo(4, 2, 2, 4, 2, 4);
		BlockPos depotPos = util.grid().at(0, 2, 2);
		BlockPos exitPos = util.grid().at(1, 2, 2);
		BlockPos[] feeders = { util.grid().at(3, 2, 2), util.grid().at(2, 2, 2), util.grid().at(1, 2, 2) };

		// start with wrongly arranged paths
		scene.world().modifyBlocks(smithers, s -> s.setValue(MechanicalSmitherBlock.POINTING, Pointing.DOWN), false);
		scene.world().setKineticSpeed(smithers, 0);
		scene.world().setKineticSpeed(kinetics, 0);

		for (int x = 3; x >= 1; x--) {
			scene.world().showSection(util.select().position(x, 2, 2), Direction.DOWN);
			scene.idle(3);
		}

		scene.overlay().showText(70)
			.text("Three Mechanical Smithers in a row can automate any Smithing Table recipe")
			.pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.UP))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);

		// wrench each smither
		for (int i = 0; i < feeders.length; i++) {
			BlockPos pos = feeders[i];
			scene.overlay().showControls(util.vector().blockSurface(pos, Direction.NORTH), Pointing.UP, 10)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
			scene.idle(7);
			scene.world().modifyBlocks(util.select().position(pos),
				s -> s.setValue(MechanicalSmitherBlock.POINTING, Pointing.RIGHT), false);
			scene.idle(i == 0 ? 15 : 10);
			if (i == 0) {
				scene.overlay().showText(60)
					.text("Using a Wrench, the Smithers' paths can be arranged")
					.pointAt(util.vector().blockSurface(pos, Direction.UP))
					.attachKeyFrame()
					.placeNearTarget();
			}
		}
		scene.idle(50);

		scene.overlay().showText(90)
			.text("For a valid setup, all paths have to converge into one exit, forming a straight line of three")
			.pointAt(util.vector().blockSurface(exitPos, Direction.WEST)
				.add(0, 0, -.5f))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);

		BlockPos[][] couples = { { util.grid().at(3, 2, 2), util.grid().at(2, 2, 2) },
			{ util.grid().at(2, 2, 2), util.grid().at(1, 2, 2) } };
		for (BlockPos[] c : couples) {
			scene.idle(5);
			Vec3 p1 = util.vector().blockSurface(c[0], Direction.NORTH)
				.add(0, 0, -0.125);
			Vec3 p2 = util.vector().blockSurface(c[1], Direction.NORTH)
				.add(0, 0, -0.125);
			scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, new AABB(p1, p1), 2);
			scene.idle(1);
			scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, new AABB(p1, p2), 30);
		}
		scene.idle(20);

		// depot and rotation
		scene.world().showSection(util.select().position(depotPos), Direction.EAST);
		scene.world().showSection(kinetics, Direction.NORTH);
		scene.idle(15);
		scene.world().setKineticSpeed(kinetics, 48);
		scene.world().setKineticSpeed(smithers, -48);
		scene.world().multiplyKineticSpeed(util.select().position(2, 2, 2), -1);
		scene.idle(40);

		// recipe output
		scene.world().modifyBlockEntity(exitPos, MechanicalSmitherBlockEntity.class,
			be -> be.setScriptedResult(new ItemStack(Items.NETHERITE_PICKAXE)));

		insert(scene, feeders[0], new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
		scene.idle(5);
		insert(scene, feeders[1], new ItemStack(Items.DIAMOND_PICKAXE));
		scene.idle(5);
		insert(scene, feeders[2], new ItemStack(Items.NETHERITE_INGOT));

		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Once every Smither holds an Item, the smithing process will begin")
			.pointAt(util.vector().blockSurface(feeders[1], Direction.UP))
			.placeNearTarget();
		scene.idle(120);
	}

	/**
	 * Structure: assets/create_smither/ponder/mechanical_smither/shapes.nbt (size 5 x 4 x 5)
	 *   y=0                    : 5x5 base plate
	 *   y=1, x=1, z=2          : Depot
	 *   x=2, z=2               : Mechanical Smither at y=1 (facing=north, pointing=right),
	 *                            y=2 and y=3 (facing=north, pointing=down)
	 */
	public static void shapes(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_smither_shapes", "Valid Smither Layouts");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		Selection smithers = util.select().fromTo(2, 1, 2, 2, 3, 2);
		BlockPos depotPos = util.grid().at(1, 1, 2);
		BlockPos top = util.grid().at(2, 3, 2);
		BlockPos middle = util.grid().at(2, 2, 2);
		BlockPos exit = util.grid().at(2, 1, 2);
		BlockPos side = util.grid().at(3, 1, 2);
		BlockPos[] line = { top, middle, exit };

		scene.world().setKineticSpeed(smithers, 0);

		scene.world().showSection(util.select().position(exit), Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(util.select().position(middle), Direction.DOWN);
		scene.idle(3);
		scene.world().showSection(util.select().position(top), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(depotPos), Direction.EAST);
		scene.idle(10);

		scene.overlay().showText(90)
			.text("Smithers can be arranged in any straight line of three, horizontal or vertical")
			.pointAt(util.vector().blockSurface(middle, Direction.WEST))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(20);

		BlockPos[][] links = { { top, middle }, { middle, exit }, { exit, depotPos } };
		for (BlockPos[] c : links) {
			scene.idle(5);
			Vec3 p1 = util.vector().blockSurface(c[0], Direction.NORTH)
				.add(0, 0, -0.125);
			Vec3 p2 = util.vector().blockSurface(c[1], Direction.NORTH)
				.add(0, 0, -0.125);
			scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, new AABB(p1, p1), 2);
			scene.idle(1);
			scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, p1, new AABB(p1, p2), 30);
		}
		scene.idle(70);

		// item order
		ItemStack template = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
		ItemStack base = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemStack addition = new ItemStack(Items.NETHERITE_INGOT);

		scene.world().modifyBlockEntity(exit, MechanicalSmitherBlockEntity.class,
			be -> be.setScriptedResult(new ItemStack(Items.NETHERITE_PICKAXE)));
		insert(scene, top, addition);
		scene.idle(5);
		insert(scene, middle, template);
		scene.idle(5);
		insert(scene, exit, base);
		scene.idle(10);

		scene.overlay().showText(140)
			.text("Along a straight line, the Items can be placed in any order")
			.pointAt(util.vector().blockSurface(middle, Direction.WEST))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(25);

		// shuffle the visible items around a few times, order is (top, middle, exit)
		ItemStack[][] orders = { { template, base, addition }, { base, addition, template },
			{ template, addition, base }, { addition, template, base } };
		for (ItemStack[] order : orders) {
			for (int i = 0; i < line.length; i++)
				setSlot(scene, line[i], order[i]);
			scene.idle(25);
		}

		// rotation and run
		scene.world().setKineticSpeed(smithers, -48);
		scene.world().multiplyKineticSpeed(util.select().position(middle), -1);
		scene.idle(5);
		scene.world().modifyBlockEntity(exit, MechanicalSmitherBlockEntity.class,
			be -> be.checkCompletedRecipe(false));
		scene.idle(170);
		scene.world().removeItemsFromBelt(depotPos);
		scene.idle(10);

		// invalid (L-shape)
		scene.world().hideSection(util.select().position(top), Direction.UP);
		scene.idle(10);
		scene.world().setBlocks(util.select().position(side), smither(Pointing.RIGHT), false);
		scene.world().showSection(util.select().position(side), Direction.WEST);
		scene.idle(10);
		Selection bent = util.select().position(middle)
			.add(util.select().position(exit))
			.add(util.select().position(side));
		scene.overlay().showOutline(PonderPalette.RED, new Object(), bent, 90);
		scene.overlay().showText(90)
			.text("Bent shapes do not work, the Items would just be ejected")
			.pointAt(util.vector().blockSurface(side, Direction.UP))
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(100);

		// invalid (more than three smither)
		scene.world().showSection(util.select().position(top), Direction.DOWN);
		scene.idle(10);
		Selection tooMany = util.select().fromTo(2, 1, 2, 2, 3, 2)
			.add(util.select().position(side));
		scene.overlay().showOutline(PonderPalette.RED, new Object(), tooMany, 90);
		scene.overlay().showText(90)
			.text("Neither do setups with more than three Smithers")
			.pointAt(util.vector().blockSurface(top, Direction.WEST))
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(100);
	}

	private static BlockState smither(Pointing pointing) {
		return CreateSmitherBlockRegistries.MECHANICAL_SMITHER.getDefaultState()
			.setValue(MechanicalSmitherBlock.HORIZONTAL_FACING, Direction.NORTH)
			.setValue(MechanicalSmitherBlock.POINTING, pointing);
	}

	private static void setSlot(CreateSceneBuilder scene, BlockPos pos, ItemStack stack) {
		scene.world().modifyBlockEntity(pos, MechanicalSmitherBlockEntity.class,
			be -> be.getInventory()
				.setStackInSlot(0, stack.copy()));
	}

	private static void insert(CreateSceneBuilder scene, BlockPos pos, ItemStack stack) {
		scene.world().modifyBlockEntity(pos, MechanicalSmitherBlockEntity.class,
			be -> be.getInventory()
				.insertItem(0, stack.copy(), false));
	}

}
