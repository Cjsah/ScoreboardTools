package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ScoreboardSaveData.class)
public class ScoreboardSaveDataMixin {

    @Shadow @Final private Scoreboard scoreboard;

    @Inject(method = "load", at = @At("RETURN"))
    private void read(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfoReturnable<ScoreboardSaveData> cir) {
        ScoreboardTools.readNbt(this.scoreboard, nbt);
        this.scheduleExecute((internal) -> internal.readNbt(nbt));
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void write(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        ScoreboardTools.writeNbt(nbt);
        this.scheduleExecute((internal) -> internal.writeNbt(nbt));
    }

    @Unique
    private void scheduleExecute(Consumer<ScoreboardSchedule> consumer) {
        if (this.scoreboard instanceof ServerScoreboard scb) {
            MinecraftServer server = ((ServerScoreboardAccessor) scb).getServer();
            consumer.accept(((ScoreboardScheduleFake) server).scbt$getSchedule());
        }
    }
}
