package top.yanhy.randomblock.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import top.yanhy.randomblock.config.ConfigManager;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncChunkProcessor {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    public static void processChunk(World world, Chunk chunk) {
        // 异步计算要修改的方块位置
        CompletableFuture.supplyAsync(() -> {
            ArrayList<Object> positionsToUpdate = new ArrayList<>();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -64; y < 319; y++) {
                        BlockPos pos = new BlockPos(
                                chunk.getPos().getStartX() + x,
                                y,
                                chunk.getPos().getStartZ() + z
                        );
                        if (!ConfigManager.isProtected(world.getBlockState(pos))) {
                            positionsToUpdate.add(pos);
                        }
                    }
                }
            }
            return positionsToUpdate;
        }).thenAcceptAsync(positions -> {
            // 在主线程执行方块更新
            for (Object pos : positions) {
                Block randomBlock = WeightedRandomizer.getRandomBlock();
                world.setBlockState((BlockPos) pos, randomBlock.getDefaultState(), 3);
            }
        }, world.getServer());
//        Text message = Text.literal( "处理区块: " + chunk.getPos().x + ", " + chunk.getPos().z );
//        if (MinecraftClient.getInstance().player != null) {
//            MinecraftClient.getInstance().player.sendMessage(message, false);
//        }
    }
}