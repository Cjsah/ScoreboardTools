package net.cjsah.scoretools.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cjsah.scoretools.CommandDisplayCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 7
            )
    )
    private static LiteralArgumentBuilder<ServerCommandSource> register(String name) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(name);
        CommandDisplayCommand.register(literal);
        return literal;
    }


}
