package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(
            method = "place",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void placedBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ScoreboardTools.addScore(context.getPlayer(), ScoreboardTools.PlacedObjectives);
    }
}
