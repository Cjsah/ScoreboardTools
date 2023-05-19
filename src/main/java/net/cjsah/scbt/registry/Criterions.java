package net.cjsah.scbt.registry;

import net.cjsah.scbt.mixin.ScoreboardCriterionInvoker;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class Criterions {
    public static final ScoreboardCriterion MINED_COUNT = create("minedCount");
    public static final ScoreboardCriterion PLACED_COUNT = create("placedCount");
    public static final ScoreboardCriterion KILLED_COUNT = create("killedCount");

    private static ScoreboardCriterion create(String name) {
        return ScoreboardCriterionInvoker.invokeCreate(name);
    }
    
    public static void loadClass() {}
}
