package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ScoreboardState.class)
public class ScoreboardStateMixin {

    @Shadow @Final private Scoreboard scoreboard;

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<ScoreboardState> cir) {
        ScoreboardTools.readNbt(this.scoreboard, nbt);
        this.scheduleExecute((internal) -> internal.readNbt(nbt));
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void write(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfoReturnable<NbtCompound> cir) {
        ScoreboardTools.writeNbt(nbt);
        this.scheduleExecute((internal) -> internal.writeNbt(nbt));
    }

    @Unique
    private void scheduleExecute(Consumer<ScoreboardSchedule> consumer) {
        if (this.scoreboard instanceof ServerScoreboard scb) {
            consumer.accept(((ScoreboardScheduleFake) ((ServerScoreboardAccessor) scb).getServer()).scbt$getSchedule());
        }
    }
}
