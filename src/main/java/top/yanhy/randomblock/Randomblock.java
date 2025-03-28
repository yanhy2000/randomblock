package top.yanhy.randomblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.world.ChunkEvents;
import top.yanhy.randomblock.command.ReloadCommand;
import top.yanhy.randomblock.config.ConfigManager;

public class Randomblock implements ModInitializer {
    @Override
    public void onInitialize() {
        // 初始化配置
        ConfigManager.reload();

        // 注册指令
        ReloadCommand.register();

        // 监听区块加载
        ChunkEvents.LOAD.register((chunk, world) -> {
            if (world.getRegistryKey() != World.OVERWORLD) return;
            AsyncChunkProcessor.processChunk(world, chunk);
        });

        // 服务器停止时关闭线程池
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            AsyncChunkProcessor.EXECUTOR.shutdown();
        });
    }
}