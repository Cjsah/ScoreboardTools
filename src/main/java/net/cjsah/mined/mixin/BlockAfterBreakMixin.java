package net.cjsah.mined.mixin;

import net.cjsah.mined.MinedMain;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class BlockAfterBreakMixin {

    @Final
    @Shadow
    protected ServerPlayerEntity player;

    @SuppressWarnings("ConstantConditions")
    @Inject(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"
            )
    )
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isBot(this.player)) this.player.getServer().getScoreboard().forEachScore(MinedMain.MINED_COUNT, this.player.getEntityName(), ScoreboardPlayerScore::incrementScore);
    }

    private boolean isBot(ServerPlayerEntity player) {
        if (MinedMain.BOT_CLASS == null) return false;
        return MinedMain.BOT_CLASS.isInstance(player);
    }

}
