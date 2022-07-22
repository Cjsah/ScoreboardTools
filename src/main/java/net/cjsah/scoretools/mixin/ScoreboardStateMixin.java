package net.cjsah.scoretools.mixin;

import net.cjsah.scoretools.ScoreboardInternal;
import net.cjsah.scoretools.fake.ScoreboardInternalFake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
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
            this.internalImpl((internal) -> internal.readNbt(nbt.getCompound("DisplayInternal")));
        }
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void write(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        this.internalImpl((internal) -> {
            NbtCompound compound = new NbtCompound();
            internal.writeNbt(compound);
            nbt.put("DisplayInternal", compound);
        });
    }

    private void internalImpl(Consumer<ScoreboardInternal> consumer) {
        if (this.scoreboard instanceof ServerScoreboard scoreboard) {
            consumer.accept(((ScoreboardInternalFake) ((ServerScoreboardAccessor) scoreboard).getServer()).getInternal());
        }
    }
}
