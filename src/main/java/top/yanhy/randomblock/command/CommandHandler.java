package top.yanhy.randomblock.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import top.yanhy.randomblock.config.ConfigManager;
import top.yanhy.randomblock.util.AsyncChunkProcessor;

public class CommandHandler {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("rb")
                .then(CommandManager.literal("start")
                        .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int radius = IntegerArgumentType.getInteger(context, "radius");
                                    AsyncChunkProcessor.startProcessingTask(context.getSource().getWorld(), radius);
                                    context.getSource().sendMessage(Text.literal("开始更新任务，半径：" + radius));
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("stop")
                        .executes(context -> {
                            if (AsyncChunkProcessor.isRunning()) {
                                AsyncChunkProcessor.stopProcessingTask();
                                context.getSource().sendMessage(Text.literal("任务已停止。"));
                            } else {
                                context.getSource().sendMessage(Text.literal("任务未运行。"));
                            }
                            return 1;
                        })
                )
                .then(CommandManager.literal("pause")
                        .executes(context -> {
                            if (AsyncChunkProcessor.isRunning()) {
                                AsyncChunkProcessor.pauseProcessingTask();
                                context.getSource().sendMessage(Text.literal("任务已暂停。"));
                            } else {
                                context.getSource().sendMessage(Text.literal("任务未运行。"));
                            }
                            return 1;
                        })
                )
                .then(CommandManager.literal("continue")
                        .executes(context -> {
                            if (AsyncChunkProcessor.isPaused()) {
                                AsyncChunkProcessor.continueProcessingTask();
                                context.getSource().sendMessage(Text.literal("任务已继续。"));
                            } else {
                                context.getSource().sendMessage(Text.literal("任务未暂停。"));
                            }
                            return 1;

                        })
                )
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            AsyncChunkProcessor.stopProcessingTask();
                            ConfigManager.reload();
                            context.getSource().sendMessage(Text.literal("配置已重新加载，并停止所有任务。"));
                            return 1;
                        })
                )
                //帮助信息
                .then(CommandManager.literal("help")
                        .executes(context -> {
                            Text message = Text.literal("""
                                    帮助信息：
                                    rb start <radius> 开始更新任务，半径为整数。
                                    rb stop 停止更新任务。
                                    rb pause 暂停更新任务。
                                    rb continue 继续更新任务。
                                    rb reload 重新加载配置并停止所有任务。
                                    rb help 显示帮助信息。""");
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.sendMessage(message, false);
                            }
                             return 1;
                         })
                )
        ));
    }
}
