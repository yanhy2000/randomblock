package top.yanhy.randomblock.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import top.yanhy.randomblock.config.ConfigManager;
import static net.minecraft.server.command.CommandManager.*;

public class ReloadCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    literal("rb")
                            .then(literal("reload")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .executes(context -> {
                                        ConfigManager.reload();
                                        context.getSource().sendFeedback(
                                                new LiteralText("随机方块配置已重载！"),
                                                true
                                        );
                                        return 1;
                                    })
                            )
            );
        });
    }
}