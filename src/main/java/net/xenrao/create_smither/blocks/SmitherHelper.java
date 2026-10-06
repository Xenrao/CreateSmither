package net.xenrao.create_smither.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SmitherHelper {

	public static MechanicalSmitherBlockEntity getCrafter(BlockAndTintGetter reader, BlockPos pos) {
		BlockEntity blockEntity = reader.getBlockEntity(pos);
		if (!(blockEntity instanceof MechanicalSmitherBlockEntity))
			return null;
		return (MechanicalSmitherBlockEntity) blockEntity;
	}

}
