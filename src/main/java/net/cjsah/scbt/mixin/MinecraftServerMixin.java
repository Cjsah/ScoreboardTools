package net.cjsah.scbt.mixin;

import com.mojang.datafixers.DataFixer;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
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
    private ScoreboardSchedule scoreboardSchedule;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.scoreboardSchedule = new ScoreboardSchedule((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void scoreboardTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.scoreboardSchedule.tick();
    }

    @Override
    public ScoreboardSchedule scbt$getSchedule() {
        return this.scoreboardSchedule;
    }
}
