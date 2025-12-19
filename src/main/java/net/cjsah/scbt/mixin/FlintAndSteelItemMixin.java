package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
    @Inject(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void placedBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ScoreboardTools.addScore(context.getPlayer(), ScoreboardTools.MinedObjectives);
    }
}
