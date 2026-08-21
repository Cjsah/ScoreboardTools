package net.cjsah.scbt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.data.ScoreboardToolSaveData;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ScoreboardToolFake {

    @Unique
    private ScoreboardToolContext scbt$scoreboardContext;

    @Shadow
    public abstract ServerScoreboard getScoreboard();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(
        Thread thread,
        LevelStorageSource.LevelStorageAccess levelStorageAccess,
        PackRepository packRepository,
        WorldStem worldStem,
        Proxy proxy,
        DataFixer dataFixer,
        Services services,
        LevelLoadListener levelLoadListener,
        CallbackInfo ci
    ) {
        this.scbt$scoreboardContext = new ScoreboardToolContext(this.getScoreboard());
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerScoreboard;load(Lnet/minecraft/world/scores/ScoreboardSaveData$Packed;)V", shift = At.Shift.AFTER))
    private void injectSaveData(CallbackInfo ci, @Local DimensionDataStorage savedDataStorage) {
        this.scbt$scoreboardContext.load(savedDataStorage.computeIfAbsent(ScoreboardToolSaveData.TYPE).getData());
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
        this.scbt$scoreboardContext.getScheduler().tick();
    }

    @Shadow
    @Final
    public abstract ServerLevel overworld();

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerScoreboard;storeToSaveDataIfDirty(Lnet/minecraft/world/scores/ScoreboardSaveData;)V", shift = At.Shift.AFTER))
    private void saveScoreContext(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        this.scbt$scoreboardContext.storeToSaveDataIfDirty(this.overworld().getDataStorage().computeIfAbsent(ScoreboardToolSaveData.TYPE));
    }

    @Override
    public ScoreboardToolContext scbt$getContext() {
        return this.scbt$scoreboardContext;
    }
}
