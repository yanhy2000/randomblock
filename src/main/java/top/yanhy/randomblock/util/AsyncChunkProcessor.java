package top.yanhy.randomblock.util;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import top.yanhy.randomblock.config.ConfigManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncChunkProcessor {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public static void processChunk(World world, Chunk chunk) {
        EXECUTOR.submit(() -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -64; y < 319; y++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                        BlockState current = world.getBlockState(pos);

                        if (ConfigManager.isProtected(current)) continue;

                        Block randomBlock = WeightedRandomizer.getRandomBlock();
                        world.setBlockState(pos, randomBlock.getDefaultState());
                    }
                }
            }
        });
    }
}