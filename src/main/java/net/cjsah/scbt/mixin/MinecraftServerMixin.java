package net.cjsah.scbt.mixin;

import com.mojang.datafixers.DataFixer;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

//#if MC >= 12109
//$$ import net.minecraft.server.level.progress.LevelLoadListener;
//#else
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
//#endif
//#if MC >= 12111
//$$ import com.llamalad7.mixinextras.sugar.Local;
//#endif

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ScoreboardToolFake {

    @Unique
    private ScoreboardToolContext scbt$scoreboardContext;

    @Shadow
    public abstract ServerScoreboard getScoreboard();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services,
                      //#if MC >= 12109
                      //$$ LevelLoadListener levelLoadListener,
                      //#else
                      ChunkProgressListenerFactory chunkProgressListenerFactory,
                      //#endif
                      CallbackInfo ci) {
        this.scbt$scoreboardContext = new ScoreboardToolContext(this.getScoreboard());
    }

    //#if MC >= 12111
    //$$ @Inject(method = "createLevels", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/ServerScoreboard;load(Lnet/minecraft/world/scores/ScoreboardSaveData$Packed;)V"))
    //#else
    @Inject(method = "readScoreboard", at = @At("RETURN"))
    //#endif
    private void injectSaveData(
        //#if MC < 12111
        DimensionDataStorage dimensionDataStorage,
        //#endif
        CallbackInfo ci
        //#if MC >= 12111
        //$$ , @Local(name = "dimensionDataStorage") DimensionDataStorage dimensionDataStorage
        //#endif
    ) {
        //#if MC >= 12111
        //$$ this.scbt$scoreboardContext.load(dimensionDataStorage.computeIfAbsent(ScoreboardToolContext.TYPE).getData());
        //#elseif MC >= 12105
        //$$ dimensionDataStorage.computeIfAbsent(ScoreboardToolContext.TYPE);
        //#else
        dimensionDataStorage.computeIfAbsent(this.scbt$scoreboardContext.dataFactory(), "scoreboard_tool_data");
        //#endif
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

    @Override
    public ScoreboardToolContext scbt$getContext() {
        return this.scbt$scoreboardContext;
    }
}
