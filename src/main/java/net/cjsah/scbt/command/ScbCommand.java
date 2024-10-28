package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.MinedObjectives;
import static net.cjsah.scbt.ScoreboardTools.PlacedObjectives;
import static net.cjsah.scbt.ScoreboardTools.feedback;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ScbCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("scbt")
                .then(bind())
                .then(unbind())
                .then(literal("loop").then(argument("slot", ScoreboardSlotArgumentType.scoreboardSlot())
                        .then(literal("add").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(ScbCommand::add)))
                        .then(literal("remove").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(ScbCommand::remove)))
                        .then(literal("schedule").then(argument("schedule", IntegerArgumentType.integer(1)).executes(ScbCommand::schedule)))
                        .then(literal("enable").executes(ScbCommand::enable))
                        .then(literal("disable").executes(ScbCommand::disable))
                ))
        );

    }

    private static LiteralArgumentBuilder<ServerCommandSource> bind() {
        LiteralArgumentBuilder<ServerCommandSource> bind = literal("bind");
        appendCriterion(bind, ScoreboardTools.MINED_COUNT, MinedObjectives::add);
        appendCriterion(bind, ScoreboardTools.PLACED_COUNT, PlacedObjectives::add);
        return bind;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> unbind() {
        LiteralArgumentBuilder<ServerCommandSource> bind = literal("unbind");
        appendCriterion(bind, ScoreboardTools.MINED_COUNT, MinedObjectives::remove);
        appendCriterion(bind, ScoreboardTools.PLACED_COUNT, PlacedObjectives::remove);
        return bind;
    }

    private static void appendCriterion(LiteralArgumentBuilder<ServerCommandSource> literal, String name, Consumer<ScoreboardObjective> execute) {
        literal.then(literal(name).then(argument("name", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(context -> {
            ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "name");
            execute.accept(objective);
            return Command.SINGLE_SUCCESS;
        })));
    }

    private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            ScoreboardObjective scoreboard = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
            if (!internal.contains(slot, scoreboard)) {
                internal.add(slot, scoreboard);
                feedback(context, "Existed Scoreboard Objective");
            }
            feedback(context, "Completed");
        });
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            ScoreboardObjective scoreboard = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
            if (internal.contains(slot, scoreboard)) internal.remove(slot, scoreboard);
            feedback(context, "Completed");
        });
    }

    private static int schedule(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setSchedule(slot, IntegerArgumentType.getInteger(context, "schedule"));
            feedback(context, "Completed");
        });
    }

    private static int enable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setEnable(slot, true);
            feedback(context, "Completed");
        });
    }

    private static int disable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setEnable(slot, false);
            feedback(context, "Completed");
        });
    }

    private static int executeInternal(CommandContext<ServerCommandSource> context, BiConsumer<ScoreboardSchedule, ScoreboardDisplaySlot> consumer) throws CommandSyntaxException {
        ScoreboardSchedule internal = ((ScoreboardScheduleFake) context.getSource().getServer()).scbt$getSchedule();
        ScoreboardDisplaySlot slot = ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot");
        consumer.accept(internal, slot);
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }

}
