package net.cjsah.mined;

import net.fabricmc.api.ModInitializer;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class MinedMain implements ModInitializer {

    public static final ScoreboardCriterion MINED_COUNT = ScoreboardCriterion.create("minedCount");

    public static final Class<?> BOT_CLASS;

    @Override
    public void onInitialize() {
    }

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("carpet.patches.EntityPlayerMPFake");
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        BOT_CLASS = clazz;
    }

}
