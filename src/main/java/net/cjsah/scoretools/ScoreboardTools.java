package net.cjsah.scoretools;

import net.fabricmc.api.ModInitializer;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    @Override
    public void onInitialize() {
        Criterion.loadClass();
    }

    @SuppressWarnings("ConstantConditions")
    public static void addScore(ServerPlayerEntity player, ScoreboardCriterion criterion) {
        if (notCarpetBot(player)) player.getServer().getScoreboard().forEachScore(criterion, player.getEntityName(), ScoreboardPlayerScore::incrementScore);
    }

    private static boolean notCarpetBot(ServerPlayerEntity player) {
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
