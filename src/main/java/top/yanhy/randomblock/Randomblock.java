package top.yanhy.randomblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.WorldSavePath;
import top.yanhy.randomblock.command.CommandHandler;
import top.yanhy.randomblock.config.ConfigManager;
import top.yanhy.randomblock.util.ChunkStorage;

import java.io.File;

public class Randomblock implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.reload();
        CommandHandler.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            File worldDir = server.getSavePath(WorldSavePath.ROOT).toFile();
            ChunkStorage.initDB(worldDir);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ChunkStorage.closeDB());
    }
}
