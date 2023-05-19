package net.cjsah.scbt.mixin;

import net.minecraft.scoreboard.ScoreboardCriterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScoreboardCriterion.class)
public interface ScoreboardCriterionInvoker {
    @Invoker("create")
    @SuppressWarnings("unused")
    static ScoreboardCriterion invokeCreate(String name) {
        throw new AssertionError();
    }
}
