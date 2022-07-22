package net.cjsah.scoretools.mixin;

import net.cjsah.scoretools.ScoreboardInternal;
import net.cjsah.scoretools.fake.ScoreboardInternalFake;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ScoreboardInternalFake {

    private final ScoreboardInternal internal = new ScoreboardInternal((MinecraftServer) (Object) this);

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

    @Override
    public ScoreboardInternal getInternal() {
        return this.internal;
    }
}
