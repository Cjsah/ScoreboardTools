package net.cjsah.scbt.mixin;

import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12102
//$$ import net.minecraft.world.InteractionResult;
//#else
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
//#endif

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Inject(
        method = "use",
        at = @At(
            value = "INVOKE",
            //#if MC >= 260200
            //$$ target = "Lnet/minecraft/advancements/triggers/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemInstance;)V"
            //#elseif MC >= 260000
            //$$ target = "Lnet/minecraft/advancements/criterion/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemInstance;)V"
            //#else
            target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            //#endif
        )
    )
    private void placedBlock(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<
        //#if MC >= 12102
        //$$ InteractionResult
        //#else
        InteractionResultHolder<ItemStack>
        //#endif
        > cir) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) server).scbt$getContext();
        scoreContext.addScore(player, ScoreType.PLACED_COUNT);
    }
}
