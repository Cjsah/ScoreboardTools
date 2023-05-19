package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ScoreboardState.class)
public class ScoreboardStateMixin {

    @Shadow @Final private Scoreboard scoreboard;

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void read(NbtCompound nbt, CallbackInfoReturnable<ScoreboardState> cir) {
        if (nbt.contains("DisplayInternal")) {
            this.scheduleExecute((internal) -> internal.readNbt(nbt.getCompound("DisplayInternal")));
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void write(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        this.scheduleExecute((internal) -> {
            NbtCompound compound = new NbtCompound();
            internal.writeNbt(compound);
            if (!compound.isEmpty()) nbt.put("DisplayInternal", compound);
        });
    }

    private void scheduleExecute(Consumer<ScoreboardSchedule> consumer) {
        if (this.scoreboard instanceof ServerScoreboard scb) {
            consumer.accept(((ScoreboardScheduleFake) ((ServerScoreboardAccessor) scb).getServer()).getSchedule());
        }
    }
}
