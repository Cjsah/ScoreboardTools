package net.cjsah.scbt.mixin;

import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.carpetBotScore;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow @Final private ServerStatHandler statHandler;

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

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
        scbt$updateOnlineScoreboard();
        scbt$updateLevelScoreboard();
        scbt$updateElytraFlyingDistanceScoreboard();
    }

    @Unique
    private void scbt$updateLevelScoreboard() {
        ScoreboardTools.setScore(player, ScoreboardTools.LevelObjectives, player.experienceLevel);
    }

    @Unique
    private void scbt$updateElytraFlyingDistanceScoreboard() {
        for (Map.Entry<ScoreboardObjective, ElytraFlyingDistanceRecordType> entry : ScoreboardTools.ElytraFlyingDistanceObjectives.entrySet()) {
            ScoreboardObjective objective = entry.getKey();
            int distance = this.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM));
            int aviate = this.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.AVIATE_ONE_CM));
            int total = distance + aviate;
            switch (entry.getValue()) {
                case METRE -> ScoreboardTools.setScore(player, objective, total / 100);
                case KILO_METRE -> ScoreboardTools.setScore(player, objective, total / 100000);
            }
        }
    }

    @Unique
    private void scbt$updateOnlineScoreboard() {
        Set<ScoreboardObjective> objectives = ScoreboardTools.OnlineObjectives.keySet();
        for (ScoreboardObjective objective : objectives) {
            int totalTime = this.statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TOTAL_WORLD_TIME));
            switch (ScoreboardTools.OnlineObjectives.get(objective)) {
                case TICK -> ScoreboardTools.setScore(player, objectives, totalTime);
                case SECOND -> {
                    int time = totalTime / 20;
                    ScoreboardTools.setScore(player, objectives, time);
                }
                case MINUTE -> {
                    int time = totalTime / 1200;
                    ScoreboardTools.setScore(player, objectives, time);
                }
                case HOUR -> {
                    int time = totalTime / 72000;
                    ScoreboardTools.setScore(player, objectives, time);
                }
                case DAY -> {
                    int time = totalTime / 1728000;
                    ScoreboardTools.setScore(player, objectives, time);
                }
            }
        }
    }
}
