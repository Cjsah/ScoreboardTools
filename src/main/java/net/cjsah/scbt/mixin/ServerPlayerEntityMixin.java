package net.cjsah.scbt.mixin;

import net.cjsah.scbt.config.ScbToolsConfig;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.notCarpetBot;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore(Lnet/minecraft/scoreboard/ScoreboardCriterion;Ljava/lang/String;Ljava/util/function/Consumer;)V"
            )
    )
    public void die(Scoreboard instance, ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        if (!ScbToolsConfig.getInstance().isCarpetPlayerScore() && notCarpetBot((ServerPlayerEntity) (Object) this)) {
            instance.forEachScore(criterion, player, action);
        }
    }
}
