package net.cjsah.scbt.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.cjsah.scbt.command.ScbCommand;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandRegisterMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ScoreboardCommand;register(Lcom/mojang/brigadier/CommandDispatcher;Lnet/minecraft/command/CommandRegistryAccess;)V"))
    public void register(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        ScbCommand.register(this.dispatcher);
    }
}
