package net.cjsah.scoretools;

import net.cjsah.scoretools.mixin.ScoreboardCriterionInvoker;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class Criterion {
    public static final ScoreboardCriterion MINED_COUNT = create("minedCount");
    public static final ScoreboardCriterion PLACED_COUNT = create("placedCount");

    private static ScoreboardCriterion create(String name) {
        return ScoreboardCriterionInvoker.invokeCreate(name);
    }
    
    public static void loadClass() {}
}
