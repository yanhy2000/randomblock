package top.yanhy.randomblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import top.yanhy.randomblock.command.ReloadCommand;
import top.yanhy.randomblock.config.ConfigManager;
import top.yanhy.randomblock.util.AsyncChunkProcessor;

public class Randomblock implements ModInitializer {
    @Override
    public void onInitialize() {
        // 初始化配置
        ConfigManager.reload();

        // 注册指令
        ReloadCommand.register();

        // 监听区块生成事件（仅在新区块生成时触发）
        ServerChunkEvents.CHUNK_LOAD.register(AsyncChunkProcessor::processChunk);
//        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
//                world.getServer().execute(() -> {
//                    AsyncChunkProcessor.processChunk(world, chunk);
//                });
//        });

        // 服务器停止时关闭线程池
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AsyncChunkProcessor.EXECUTOR.shutdown());
    }
}