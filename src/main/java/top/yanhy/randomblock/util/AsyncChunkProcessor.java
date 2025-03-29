package top.yanhy.randomblock.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import top.yanhy.randomblock.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AsyncChunkProcessor {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    public static ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT);

    private static int currentRadius = 16;
    private static final int RADIUS_INCREMENT = 8;

    public static void processWorld(ServerWorld world) {
        BlockPos spawnPos = world.getSpawnPos();
        int spawnX = spawnPos.getX();
        int spawnZ = spawnPos.getZ();

        for (int r = 0; r <= currentRadius; r += RADIUS_INCREMENT) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int dx = -r; dx <= r; dx += 16) {
                for (int dz = -r; dz <= r; dz += 16) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                    int chunkX = (spawnX + dx) >> 4;
                    int chunkZ = (spawnZ + dz) >> 4;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    if (ChunkStorage.isChunkProcessed(chunkPos)) {
                        continue;
                    }

                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    futures.add(processChunkAsync(world, chunk, chunkPos));
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            currentRadius += RADIUS_INCREMENT;
        }
    }

    private static CompletableFuture<Void> processChunkAsync(ServerWorld world, Chunk chunk, ChunkPos chunkPos) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockPos> positionsToUpdate = new ArrayList<>();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = world.getBottomY(); y < world.getHeight(); y++) {
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
        }, EXECUTOR).thenAcceptAsync(positions -> {
            for (BlockPos pos : positions) {
                Block randomBlock = WeightedRandomizer.getRandomBlock();
                world.setBlockState(pos, randomBlock.getDefaultState(), 3);
            }
            // 处理完成后标记区块
            ChunkStorage.markChunkProcessed(chunkPos);
            Text message = Text.literal( "处理区块: " + chunk.getPos().x + ", " + chunk.getPos().z );
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(message, false);
            }
        }, world.getServer());

    }
}
