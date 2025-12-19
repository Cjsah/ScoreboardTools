package net.cjsah.scbt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.carpetBotScore;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Shadow
    @Final
    private ServerStatsCounter stats;
    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;
    @Unique
    private int scbt$lastExpLevel = 0;

    @WrapOperation(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/scores/Scoreboard;forAllObjectives(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Lnet/minecraft/world/scores/ScoreHolder;Ljava/util/function/Consumer;)V"
        )
    )
    public void onDeathScore(Scoreboard instance, ObjectiveCriteria objectiveCriteria, ScoreHolder scoreHolder, Consumer<ScoreAccess> consumer, Operation<Void> original) {
        if (carpetBotScore(this.player)) {
            original.call(instance, objectiveCriteria, scoreHolder, consumer);
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
        if (this.player.experienceLevel != this.scbt$lastExpLevel) {
            this.scbt$lastExpLevel = this.player.experienceLevel;
            ScoreboardTools.setScore(this.player, ScoreboardTools.LevelObjectives, this.player.experienceLevel);
        }
    }

    @Unique
    private void scbt$updateElytraFlyingDistanceScoreboard() {
        for (Map.Entry<Objective, ElytraFlyingDistanceRecordType> entry : ScoreboardTools.ElytraFlyingDistanceObjectives.entrySet()) {
            Objective objective = entry.getKey();
            int distance = this.stats.getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM));
            int aviate = this.stats.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM));
            int total = distance + aviate;
            switch (entry.getValue()) {
                case METRE -> ScoreboardTools.setScore(player, objective, total / 100);
                case KILO_METRE -> ScoreboardTools.setScore(player, objective, total / 100000);
            }
        }
    }

    @Unique
    private void scbt$updateOnlineScoreboard() {
        for (Map.Entry<Objective, OnlineTimeRecordType> entry : ScoreboardTools.OnlineObjectives.entrySet()) {
            Objective objective = entry.getKey();
            int totalTime = this.stats.getValue(Stats.CUSTOM.get(Stats.TOTAL_WORLD_TIME));
            int interval = switch (entry.getValue()) {
                case TICK -> 1;
                case SECOND -> 20;
                case MINUTE -> 1200;
                case HOUR -> 72000;
                case DAY -> 1728000;
            };
            ScoreboardTools.setScore(player, objective, totalTime / interval);
        }
    }
}
