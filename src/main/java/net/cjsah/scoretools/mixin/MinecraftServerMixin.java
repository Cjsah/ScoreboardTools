package net.cjsah.scoretools.mixin;

import net.cjsah.scoretools.ScoreboardInternal;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.spawner.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    private final ScoreboardInternal internal = new ScoreboardInternal();

    @Inject(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/server/PlayerManager;updatePlayerLatency()V"
            )
    )
    private void scoreboardTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.internal.tick();
    }


    @Inject(
            method = "createWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;initScoreboard(Lnet/minecraft/world/PersistentStateManager;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void initScoreboardInterval(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci, ServerWorldProperties properties, GeneratorOptions gOptions, boolean bl, long l1, long l2, List<Spawner> list, Registry<DimensionOptions> registry, DimensionOptions dOptions, ServerWorld world, PersistentStateManager manager) {

    }
}
