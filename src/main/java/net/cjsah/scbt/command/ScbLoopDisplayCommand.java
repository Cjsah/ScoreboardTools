package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;

import static net.cjsah.scbt.ScoreboardTools.feedback;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ScbLoopDisplayCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> node) {
        node.then(literal("loop").then(argument("slot", ScoreboardSlotArgumentType.scoreboardSlot())
                .then(literal("add").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(ScbLoopDisplayCommand::add)))
                .then(literal("remove").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(ScbLoopDisplayCommand::remove)))
                .then(literal("schedule").then(argument("schedule", IntegerArgumentType.integer(1)).executes(ScbLoopDisplayCommand::schedule)))
                .then(literal("enable").executes(ScbLoopDisplayCommand::enable))
                .then(literal("disable").executes(ScbLoopDisplayCommand::disable))

        ));
    }

    private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(context, (internal, slot) -> {
            ScoreboardObjective scoreboard = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
            if (!internal.contains(slot, scoreboard)) {
                internal.add(slot, scoreboard);
                feedback(context, "Existed Scoreboard Objective");
            }
            feedback(context, "Completed");
        });
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(context, (internal, slot) -> {
            ScoreboardObjective scoreboard = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
            if (internal.contains(slot, scoreboard)) internal.remove(slot, scoreboard);
            feedback(context, "Completed");
        });
    }

    private static int schedule(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(context, (internal, slot) -> {
            internal.setSchedule(slot, IntegerArgumentType.getInteger(context, "schedule"));
            feedback(context, "Completed");
        });
    }

    private static int enable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(context, (internal, slot) -> {
            internal.setEnable(slot, true);
            feedback(context, "Completed");
        });
    }

    private static int disable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(context, (internal, slot) -> {
            internal.setEnable(slot, false);
            feedback(context, "Completed");
        });
    }

    private static int execute(CommandContext<ServerCommandSource> context, BiConsumer<ScoreboardSchedule, Integer> consumer) throws CommandSyntaxException {
        ScoreboardSchedule internal = ((ScoreboardScheduleFake) context.getSource().getServer()).scbt$getSchedule();
        int slot = ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot");
        consumer.accept(internal, slot);
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
