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

    private static boolean isRunning = false;
    private static boolean isPaused = false;
    private static int taskRadius = 0;
    private static ServerWorld currentWorld = null;
    private static int totalChunks = 0;
    private static int processedChunks = 0;

    public static void startProcessingTask(ServerWorld world, int radius) {
        if (isRunning) {
            sendChatMessage("已有运行中的任务，先停止再启动新任务。");
            return;
        }
        isRunning = true;
        isPaused = false;
        taskRadius = radius;
        currentWorld = world;
        totalChunks = calculateTotalChunks(radius);
        processedChunks = 0;

        sendChatMessage("任务开始，预计处理 " + totalChunks + " 个区块。");
        EXECUTOR.execute(() -> processWorld(world, radius));
    }

    public static void stopProcessingTask() {
        if (!isRunning) return;
        isRunning = false;
        isPaused = false;
        taskRadius = 0;
        currentWorld = null;
        EXECUTOR.shutdownNow();
        EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public static void pauseProcessingTask() {
        if (isRunning) {
            isPaused = true;
        }
    }

    public static void continueProcessingTask() {
        if (isRunning && isPaused) {
            isPaused = false;
            EXECUTOR.execute(() -> processWorld(currentWorld, taskRadius));
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    private static void processWorld(ServerWorld world, int radius) {
        if (world == null) return;
        BlockPos spawnPos = world.getSpawnPos();
        int spawnX = spawnPos.getX();
        int spawnZ = spawnPos.getZ();

        for (int r = 0; r <= radius; r += 8) {
            if (!isRunning || isPaused) return;

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int dx = -r; dx <= r; dx += 16) {
                for (int dz = -r; dz <= r; dz += 16) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                    int chunkX = (spawnX + dx) >> 4;
                    int chunkZ = (spawnZ + dz) >> 4;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    if (ChunkStorage.isChunkProcessed(chunkPos)) continue;

                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    futures.add(processChunkAsync(world, chunk, chunkPos));
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        isRunning = false;
        sendChatMessage("任务完成，成功处理 " + processedChunks + " 个区块。");
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
            ChunkStorage.markChunkProcessed(chunkPos);
            processedChunks++;
            sendProgressMessage();
        }, world.getServer());
    }

    private static int calculateTotalChunks(int radius) {
        int count = 0;
        for (int r = 0; r <= radius; r += 8) {
            for (int dx = -r; dx <= r; dx += 16) {
                for (int dz = -r; dz <= r; dz += 16) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    count++;
                }
            }
        }
        return count;
    }

    private static void sendProgressMessage() {
        if (totalChunks > 0) {
            int progress = (int) ((processedChunks / (double) totalChunks) * 100);
            sendChatMessage("任务进度: " + progress + "%");
        }
    }

    private static void sendChatMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(message), false);
        }
    }
}
