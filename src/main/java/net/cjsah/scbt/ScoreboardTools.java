package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    public static boolean FakePlayerScore = true;

    @Override
    public void onInitialize() {
    }

    public static void feedbackCompleted(CommandContext<CommandSourceStack> context) {
        feedback(context, "Completed");
    }

    public static void feedback(CommandContext<CommandSourceStack> context, String text) {
        context.getSource().sendSystemMessage(Component.literal(text));
    }

    public static boolean carpetBotScore(Player player) {
        return FakePlayerScore || CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
    }

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("carpet.patches.EntityPlayerMPFake");
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        CARPET_PLAYER_CLASS = clazz;
    }
}
