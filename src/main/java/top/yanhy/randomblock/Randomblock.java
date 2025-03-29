package top.yanhy.randomblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import top.yanhy.randomblock.command.ReloadCommand;
import top.yanhy.randomblock.config.ConfigManager;
import top.yanhy.randomblock.util.AsyncChunkProcessor;
import top.yanhy.randomblock.util.ChunkStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

import java.io.File;
import java.util.concurrent.Executors;

public class Randomblock implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.reload();
        ReloadCommand.register();

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                startWorldProcessing(server);
            }
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                stopWorldProcessing();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            stopWorldProcessing();
            ChunkStorage.closeDB();
        });
    }

    private void startWorldProcessing(MinecraftServer server) {
        if (AsyncChunkProcessor.EXECUTOR.isShutdown()) {
            AsyncChunkProcessor.EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        File worldDir = server.getSavePath(WorldSavePath.ROOT).toFile();
        ChunkStorage.initDB(worldDir);
        AsyncChunkProcessor.EXECUTOR.execute(() -> AsyncChunkProcessor.processWorld(server.getOverworld()));
    }

    private void stopWorldProcessing() {
        if (!AsyncChunkProcessor.EXECUTOR.isShutdown()) {
            AsyncChunkProcessor.EXECUTOR.shutdownNow();
        }
    }
}
