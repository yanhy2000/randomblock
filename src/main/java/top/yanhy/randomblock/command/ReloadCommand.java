package top.yanhy.randomblock.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.WorldChunk;
import top.yanhy.randomblock.config.ConfigManager;
import top.yanhy.randomblock.util.AsyncChunkProcessor;

public class ReloadCommand {
    public static void register() {
        // 在 ReloadCommand.java 中添加

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, res) -> dispatcher.register(
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("rb")
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reload")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    ConfigManager.reload();
                                    Text message = Text.literal( "随机方块配置已重载！" );
                                    if (MinecraftClient.getInstance().player != null) {
                                        MinecraftClient.getInstance().player.sendMessage(message, false);
                                    }
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("fixchunk")
                                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("x")
                                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("z")
                                                .executes(context -> {
                                                    int x = IntegerArgumentType.getInteger(context, "x");
                                                    int z = IntegerArgumentType.getInteger(context, "z");
                                                    ClientWorld world = context.getSource().getWorld();
                                                    WorldChunk chunk = world.getChunk(x, z);
                                                    AsyncChunkProcessor.processChunk(world, chunk);
                                                    Text message = Text.literal( "已修复区块 (" + x + ", " + z + ")" );
                                                    if (MinecraftClient.getInstance().player != null) {
                                                        MinecraftClient.getInstance().player.sendMessage(message, false);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

        ));
    }
}