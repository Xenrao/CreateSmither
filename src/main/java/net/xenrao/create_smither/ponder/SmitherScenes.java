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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xenrao.create_smither.blocks.MechanicalSmitherBlock;
import net.xenrao.create_smither.blocks.MechanicalSmitherBlockEntity;

/**
 *   y=0            : 5x5 base plate
 *   y=1, z=2       : x=0 Depot | x=1,2,3 Mechanical Smither (facing=north, pointing=right)
 *   y=1, x=4       : z=2 Cogwheel (axis z), z=3 and z=4 Shaft (axis z)
 */
public class SmitherScenes {

	public static void setup(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("mechanical_smither", "Automating Smithing Recipes");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		Selection smithers = util.select().fromTo(1, 1, 2, 3, 1, 2);
		Selection kinetics = util.select().fromTo(4, 1, 2, 4, 1, 4);
		BlockPos depotPos = util.grid().at(0, 1, 2);
		BlockPos exitPos = util.grid().at(1, 1, 2); // end of the chain, the only one that runs the recipe
		BlockPos[] feeders = { util.grid().at(3, 1, 2), util.grid().at(2, 1, 2), util.grid().at(1, 1, 2) };

		// start with wrongly arranged paths
		scene.world().modifyBlocks(smithers, s -> s.setValue(MechanicalSmitherBlock.POINTING, Pointing.DOWN), false);
		scene.world().setKineticSpeed(smithers, 0);
		scene.world().setKineticSpeed(kinetics, 0);

		for (int x = 3; x >= 1; x--) {
			scene.world().showSection(util.select().position(x, 1, 2), Direction.DOWN);
			scene.idle(3);
		}

		scene.overlay().showText(70)
			.text("Three Mechanical Smithers in a row can automate any Smithing Table recipe")
			.pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.UP))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);

		// wrench each smither
		for (int i = 0; i < feeders.length; i++) {
			BlockPos pos = feeders[i];
			scene.overlay().showControls(util.vector().blockSurface(pos, Direction.NORTH), Pointing.RIGHT, 20)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
			scene.idle(7);
			scene.world().modifyBlocks(util.select().position(pos),
				s -> s.setValue(MechanicalSmitherBlock.POINTING, Pointing.RIGHT), false);
			scene.idle(i == 0 ? 15 : 10);
			if (i == 0) {
				scene.overlay().showText(60)
					.text("Using a Wrench, the Smithers' paths can be arranged")
					.pointAt(util.vector().blockSurface(pos, Direction.NORTH))
					.attachKeyFrame()
					.placeNearTarget();
			}
		}
		scene.idle(50);

		scene.overlay().showText(90)
			.text("For a valid setup, all paths have to converge into one exit, forming a line of exactly three")
			.pointAt(util.vector().blockSurface(exitPos, Direction.WEST)
				.add(0, 0, -.5f))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(40);

		BlockPos[][] couples = { { util.grid().at(3, 1, 2), util.grid().at(2, 1, 2) },
			{ util.grid().at(2, 1, 2), util.grid().at(1, 1, 2) }, { util.grid().at(1, 1, 2), depotPos } };
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

		scene.world().showSection(util.select().position(depotPos), Direction.EAST);
		scene.idle(20);
		scene.overlay().showText(60)
			.text("The result will be placed into the inventory at the exit")
			.pointAt(util.vector().blockSurface(depotPos, Direction.NORTH))
			.placeNearTarget();
		scene.idle(70);

		// speed
		scene.rotateCameraY(60);
		scene.idle(20);
		scene.world().showSection(kinetics, Direction.NORTH);
		scene.overlay().showText(60)
			.text("Mechanical Smithers require Rotational Force to operate")
			.pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(8);
		scene.world().setKineticSpeed(kinetics, 48);
		scene.world().setKineticSpeed(smithers, -48);
		scene.world().multiplyKineticSpeed(util.select().position(2, 1, 2), -1);
		scene.idle(55);
		scene.rotateCameraY(-60);
		scene.idle(30);

		// items
		ItemStack template = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
		ItemStack base = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemStack addition = new ItemStack(Items.NETHERITE_INGOT);

		scene.overlay().showControls(util.vector().blockSurface(feeders[0], Direction.NORTH), Pointing.RIGHT, 40)
			.rightClick()
			.withItem(template);
		scene.idle(7);
		insert(scene, feeders[0], template);
		scene.idle(10);
		scene.overlay().showText(50)
			.text("Right-Click the front to insert Items manually")
			.pointAt(util.vector().blockSurface(feeders[0], Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(60);

		// recipe result
		scene.world().modifyBlockEntity(exitPos, MechanicalSmitherBlockEntity.class,
			be -> be.setScriptedResult(new ItemStack(Items.NETHERITE_PICKAXE)));

		insert(scene, feeders[1], base);
		scene.idle(5);
		insert(scene, feeders[2], addition);

		scene.overlay().showText(90)
			.attachKeyFrame()
			.text("Once every Smither holds an Item, the smithing process will begin. The order of the Items doesn't matter")
			.pointAt(util.vector().blockSurface(feeders[1], Direction.NORTH))
			.placeNearTarget();
		scene.idle(120);
	}

	private static void insert(CreateSceneBuilder scene, BlockPos pos, ItemStack stack) {
		scene.world().modifyBlockEntity(pos, MechanicalSmitherBlockEntity.class,
			be -> be.getInventory()
				.insertItem(0, stack.copy(), false));
	}

}
