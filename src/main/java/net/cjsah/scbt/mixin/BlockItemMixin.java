package net.cjsah.scbt.mixin;

import net.cjsah.scbt.registry.Criterions;
import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/PlacedBlockCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void placedBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        ScoreboardTools.addScore((ServerPlayerEntity) context.getPlayer(), Criterions.PLACED_COUNT);
    }
}
