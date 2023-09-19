package net.cjsah.scbt.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cjsah.scbt.command.ScbLoopDisplayCommand;
import net.cjsah.scbt.command.ScbPresetCommand;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private static LiteralArgumentBuilder<ServerCommandSource> registerLoop(String name) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(name);
        ScbLoopDisplayCommand.register(literal);
        return literal;
    }

    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 1
            )
    )

    private static LiteralArgumentBuilder<ServerCommandSource> registerPreset(String name) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(name);
        ScbPresetCommand.register(literal);
        return literal;
    }

    @Inject(method = "executeClearDisplay", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private static void clearDisplay(ServerCommandSource source, int slot, CallbackInfoReturnable<Integer> cir) {
        ((ScoreboardScheduleFake)source.getServer()).scbt$getSchedule().disable(slot);
    }

    @Redirect(method = "executeSetDisplay", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getObjectiveForSlot(I)Lnet/minecraft/scoreboard/ScoreboardObjective;"))
    private static ScoreboardObjective setDisplayPredicate(Scoreboard scoreboard, int slot, ServerCommandSource source) {
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(slot);
        return ((ScoreboardScheduleFake) source.getServer()).scbt$getSchedule().disable(slot) ? null : objective;
    }

}
