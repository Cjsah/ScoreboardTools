package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.cjsah.scbt.config.ScbToolsConfig;
import net.cjsah.scbt.registry.ArgumentTypes;
import net.cjsah.scbt.registry.Criterions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ScoreboardTools implements ModInitializer {
    public static final String MOD_ID = "scbt";
    public static final Class<?> CARPET_PLAYER_CLASS;

    @Override
    public void onInitialize() {
        Criterions.loadClass();
        ArgumentTypes.registry();
        ScbToolsConfig.init();
        ScbToolsConfig.getInstance().update();
    }

    public static Identifier of(String name) {
        return new Identifier(MOD_ID, name);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addScore(ServerPlayerEntity player, ScoreboardCriterion criterion) {
        if (notCarpetBot(player)) player.getServer().getScoreboard().forEachScore(criterion, player.getEntityName(), ScoreboardPlayerScore::incrementScore);
    }

    public static void feedback(CommandContext<ServerCommandSource> context, String text) {
        context.getSource().sendFeedback(() -> Text.of(text), false);
    }

    public static boolean notCarpetBot(ServerPlayerEntity player) {
        return CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
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
