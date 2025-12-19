package net.cjsah.scbt.mixin;

import com.mojang.datafixers.DataFixer;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ScoreboardScheduleFake {

    @Unique
    private ScoreboardSchedule scbt$ScoreboardSchedule;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        this.scbt$ScoreboardSchedule = new ScoreboardSchedule((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "tickChildren",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void scoreboardTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.scbt$ScoreboardSchedule.tick();
    }

    @Override
    public ScoreboardSchedule scbt$getSchedule() {
        return this.scbt$ScoreboardSchedule;
    }
}
