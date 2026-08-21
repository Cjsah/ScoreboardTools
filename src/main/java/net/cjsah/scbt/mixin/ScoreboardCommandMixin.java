package net.cjsah.scbt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.command.ScoreboardToolCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {

    @WrapOperation(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
            remap = false
        )
    )
    private static ArgumentBuilder<CommandSourceStack, ?> registerCommand(LiteralArgumentBuilder<CommandSourceStack> instance, Predicate<CommandSourceStack> predicate, Operation<ArgumentBuilder<CommandSourceStack, ?>> original) {
        ArgumentBuilder<CommandSourceStack, ?> builder = original.call(instance, predicate);
        ScoreboardToolCommand.registerCommand(builder);
        return builder;
    }

    @WrapOperation(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
            ordinal = 1,
            remap = false
        )
    )
    private static ArgumentBuilder<CommandSourceStack, ?> appendToolCriteria(
        RequiredArgumentBuilder<CommandSourceStack, ?> instance,
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder,
        Operation<ArgumentBuilder<CommandSourceStack, ?>> original,
        @Local CommandBuildContext commandBuildContext
    ) throws CommandSyntaxException {
        ScoreboardToolCommand.registerCriteria(instance, commandBuildContext);
        return original.call(instance, argumentBuilder);
    }

    @WrapOperation(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
            ordinal = 1,
            remap = false
        )
    )
    private static LiteralArgumentBuilder<CommandSourceStack> appendBindCommand(String string, Operation<LiteralArgumentBuilder<CommandSourceStack>> original) throws CommandSyntaxException {
        LiteralArgumentBuilder<CommandSourceStack> builder = original.call(string);
        ScoreboardToolCommand.registerBind(builder);
        return builder;
    }

    @Inject(method = "removeObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;removeObjective(Lnet/minecraft/world/scores/Objective;)V"))
    private static void removeToolObjective(CommandSourceStack source, Objective objective, CallbackInfoReturnable<Integer> cir) {
        ScoreboardToolCommand.removeObjective(source, objective);
    }

}
