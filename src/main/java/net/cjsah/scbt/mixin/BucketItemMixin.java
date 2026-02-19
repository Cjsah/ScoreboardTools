package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12102
//$$ import net.minecraft.world.InteractionResult;
//#else
import net.minecraft.world.InteractionResultHolder;
//#endif


@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void placedBlock(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<
        //#if MC >= 12102
        //$$ InteractionResult
        //#else
        InteractionResultHolder<ItemStack>
        //#endif
        > cir) {
        ScoreboardTools.addScore(player, ScoreboardTools.PlacedObjectives);
    }
}
