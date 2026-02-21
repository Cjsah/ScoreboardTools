package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ScoreboardTools implements ModInitializer {
    @Override
    public void onInitialize() {
    }

    public static void feedbackCompleted(CommandContext<CommandSourceStack> context) {
        feedback(context, "Completed");
    }

    public static void feedback(CommandContext<CommandSourceStack> context, String text) {
        context.getSource().sendSystemMessage(Component.literal(text));
    }
}
