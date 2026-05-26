package net.cjsah.scbt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {
    public FishingHookMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("resource")
    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private void appendFishingScore(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @Local Player player) {
        if (!this.level().isClientSide()) {
            ScoreboardToolContext scoreContext = ((ScoreboardToolFake) this.level().getServer()).scbt$getContext();
            scoreContext.addScore(player, ScoreType.FINISH_FISHING);
        }
    }

}
