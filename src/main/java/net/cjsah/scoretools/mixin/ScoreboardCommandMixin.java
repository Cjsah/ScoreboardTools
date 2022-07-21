package net.cjsah.scoretools.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
        return literal(name).then(literal("loop").then(argument("slot", ScoreboardSlotArgumentType.scoreboardSlot())
                .then(literal("add").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(context -> {
                    //TODO 添加
                    return Command.SINGLE_SUCCESS;
                }))).then(literal("remove").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(context -> {
                    //TODO 移除
                    return Command.SINGLE_SUCCESS;
                }))).then(literal("internal").then(argument("internal", IntegerArgumentType.integer(1)).executes(context -> {
                    //TODO 切换间隔
                    return Command.SINGLE_SUCCESS;
                })))));
    }


}
