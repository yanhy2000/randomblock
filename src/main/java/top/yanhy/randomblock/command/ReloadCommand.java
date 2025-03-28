package top.yanhy.randomblock.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import top.yanhy.randomblock.config.ConfigManager;

public class ReloadCommand {
    public static void register() {

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
        ));
    }
}