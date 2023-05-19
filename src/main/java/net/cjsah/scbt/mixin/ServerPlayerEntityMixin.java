package net.cjsah.scbt.mixin;

import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.registry.Criterions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(
            method = "updateKilledAdvancementCriterion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/OnKilledCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)V"
            )
    )
    public void killed(Entity entityKilled, int score, DamageSource damageSource, CallbackInfo ci) {
        ScoreboardTools.addScore((ServerPlayerEntity) (Object) this, Criterions.KILLED_COUNT);
    }
}
