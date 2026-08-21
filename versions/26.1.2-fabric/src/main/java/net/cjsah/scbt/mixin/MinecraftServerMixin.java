package net.cjsah.scbt.mixin;

import com.mojang.datafixers.DataFixer;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.data.ScoreboardToolSaveData;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.Optional;
import java.util.function.BooleanSupplier;

//#if MC >= 260200
//$$ import net.minecraft.server.notifications.NotificationManager;
//#endif

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ScoreboardToolFake {

    @Final
    @Shadow
    private SavedDataStorage savedDataStorage;

    @Unique
    private ScoreboardToolContext scbt$scoreboardContext;

    @Shadow
    public abstract ServerScoreboard getScoreboard();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(
        Thread serverThread,
        LevelStorageSource.LevelStorageAccess storageSource,
        PackRepository packRepository,
        WorldStem worldStem,
        Optional<GameRules> gameRules,
        Proxy proxy,
        DataFixer fixerUpper,
        Services services,
        LevelLoadListener levelLoadListener,
        boolean propagatesCrashes,
        //#if MC >= 260200
        //$$ NotificationManager notificationManager,
        //#endif
        CallbackInfo ci
    ) {
        this.scbt$scoreboardContext = new ScoreboardToolContext(this.getScoreboard());
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerScoreboard;load(Lnet/minecraft/world/scores/ScoreboardSaveData$Packed;)V", shift = At.Shift.AFTER))
    private void injectSaveData(CallbackInfo ci) {
        this.scbt$scoreboardContext.load(this.savedDataStorage.computeIfAbsent(ScoreboardToolSaveData.TYPE).getData());
    }

    @Inject(
        method = "tickChildren",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 0
        )
    )
    private void scoreboardTick(BooleanSupplier haveTime, CallbackInfo ci) {
        this.scbt$scoreboardContext.getScheduler().tick();
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerScoreboard;storeToSaveDataIfDirty(Lnet/minecraft/world/scores/ScoreboardSaveData;)V", shift = At.Shift.AFTER))
    private void saveScoreContext(boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        this.scbt$scoreboardContext.storeToSaveDataIfDirty(this.savedDataStorage.computeIfAbsent(ScoreboardToolSaveData.TYPE));
    }

    @Override
    public ScoreboardToolContext scbt$getContext() {
        return this.scbt$scoreboardContext;
    }
}
