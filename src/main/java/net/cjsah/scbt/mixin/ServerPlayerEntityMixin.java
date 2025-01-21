package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.carpetBotScore;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore(Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/scoreboard/ScoreHolder;Ljava/util/function/Consumer;)V"
            )
    )
    public void die(Scoreboard instance, ScoreboardCriterion criterion, ScoreHolder scoreHolder, Consumer<ScoreAccess> action) {
        if (carpetBotScore((ServerPlayerEntity) (Object) this)) {
            instance.forEachScore(criterion, scoreHolder, action);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ScoreboardTools.addScore((ServerPlayerEntity) (Object) this, ScoreboardTools.OnlineObjectives);
    }
}
