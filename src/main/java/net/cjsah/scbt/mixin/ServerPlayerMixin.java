package net.cjsah.scbt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
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

import java.util.function.Consumer;

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
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) this.player.server).scbt$getContext();
        if (scoreContext.canScore(this.player)) {
            original.call(instance, objectiveCriteria, scoreHolder, consumer);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        scbt$updateOnlineScoreboard();
        scbt$updateLevelScoreboard();
        scbt$updateFlyingDistanceScoreboard();
    }

    @Unique
    private void scbt$updateLevelScoreboard() {
        if (this.player.experienceLevel != this.scbt$lastExpLevel) {
            this.scbt$lastExpLevel = this.player.experienceLevel;
            ScoreboardToolContext scoreContext = ((ScoreboardToolFake) this.player.server).scbt$getContext();
            scoreContext.setOriginScore(this.player, ScoreType.LEVEL, this.player.experienceLevel);
        }
    }

    @Unique
    private void scbt$updateFlyingDistanceScoreboard() {
        int distance = this.stats.getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM));
        int aviate = this.stats.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM));
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) this.player.server).scbt$getContext();
        scoreContext.setOriginScore(this.player, ScoreType.FLYING_DISTANCE, distance + aviate);
    }

    @Unique
    private void scbt$updateOnlineScoreboard() {
        int totalTime = this.stats.getValue(Stats.CUSTOM.get(Stats.TOTAL_WORLD_TIME));
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) this.player.server).scbt$getContext();
        scoreContext.setOriginScore(this.player, ScoreType.ONLINE_TIME, totalTime);
    }
}
