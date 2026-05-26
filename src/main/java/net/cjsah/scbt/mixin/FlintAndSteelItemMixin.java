package net.cjsah.scbt.mixin;

import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.MinecraftServer;
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
            //#if MC < 260000
            target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            //#else
            //$$ target = "Lnet/minecraft/advancements/criterion/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemInstance;)V"
            //#endif
        )
    )
    private void placedBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        MinecraftServer server = context.getLevel().getServer();
        if (server == null) return;
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) server).scbt$getContext();
        scoreContext.addScore(context.getPlayer(), ScoreType.MINED_COUNT);
    }
}
