package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardScheduler;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.data.record.DummyRecordType;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import static net.cjsah.scbt.ScoreboardTools.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScoreboardToolCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(literal("scoretool")
            .then(bind())
            .then(unbind())
            .then(literal("loop").then(argument("slot", ScoreboardSlotArgument.displaySlot())
                .then(literal("add").then(argument("objective", ObjectiveArgument.objective()).executes(ScoreboardToolCommand::add)))
                .then(literal("remove").then(argument("objective", ObjectiveArgument.objective()).executes(ScoreboardToolCommand::remove)))
                .then(literal("schedule").then(argument("schedule", IntegerArgumentType.integer(1)).executes(ScoreboardToolCommand::schedule)))
                .then(literal("enable").executes(ScoreboardToolCommand::enable))
                .then(literal("disable").executes(ScoreboardToolCommand::disable))
            ))
            .then(literal("fakePlayerScore").executes(ScoreboardToolCommand::showFakePlayerScore).then(argument("enable", BoolArgumentType.bool()).executes(ScoreboardToolCommand::changeFakePlayerScore)))
        );

    }

    private static LiteralArgumentBuilder<CommandSourceStack> bind() {
        LiteralArgumentBuilder<CommandSourceStack> bind = literal("bind");
        for (ScoreType scoreType : ScoreType.values()) {
            var node = argument("name", ObjectiveArgument.objective());
            if (scoreType.getRecordType() == DummyRecordType.class) {
                node.executes(context ->
                    commandExecute(context, (scoreContext, objective) ->
                        scoreContext.addScoreBind(objective, scoreType, DummyRecordType.DUMMY)));
            } else {
                for (Enum<?> recordType : scoreType.getRecordType().getEnumConstants()) {
                    node.then(literal(recordType.name()).executes(context ->
                        commandExecute(context, (scoreContext, objective) ->
                            scoreContext.addScoreBind(objective, scoreType, scoreType.getScoreMapper(recordType.ordinal())))));
                }
            }
            bind.then(literal(scoreType.getName()).then(node));
        }
        return bind;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unbind() {
        LiteralArgumentBuilder<CommandSourceStack> bind = literal("unbind");
        for (ScoreType scoreType : ScoreType.values()) {
            bind.then(literal(scoreType.getName()).then(argument("name", ObjectiveArgument.objective())
                .executes(context ->
                    commandExecute(context, (scoreContext, objective) ->
                        scoreContext.removeScoreBind(scoreType, objective)))));
        }
        return bind;
    }

    private static int commandExecute(CommandContext<CommandSourceStack> context, BiConsumer<ScoreboardToolContext, Objective> execute) throws CommandSyntaxException {
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) context.getSource().getServer()).scbt$getContext();
        Objective objective = ObjectiveArgument.getObjective(context, "name");
        execute.accept(scoreContext, objective);
        feedbackCompleted(context);
        return Command.SINGLE_SUCCESS;
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            Objective scoreboard = ObjectiveArgument.getObjective(context, "objective");
            if (!internal.contains(slot, scoreboard)) {
                internal.add(slot, scoreboard);
                feedbackCompleted(context);
            } else {
                feedback(context, "Existed Scoreboard Objective");
            }
        });
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            Objective scoreboard = ObjectiveArgument.getObjective(context, "objective");
            if (internal.contains(slot, scoreboard)) internal.remove(slot, scoreboard);
            feedbackCompleted(context);
        });
    }

    private static int schedule(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setSchedule(slot, IntegerArgumentType.getInteger(context, "schedule"));
            feedbackCompleted(context);
        });
    }

    private static int enable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setEnable(slot, true);
            feedbackCompleted(context);
        });
    }

    private static int disable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeInternal(context, (internal, slot) -> {
            internal.setEnable(slot, false);
            feedbackCompleted(context);
        });
    }

    private static int executeInternal(CommandContext<CommandSourceStack> context, BiConsumer<ScoreboardScheduler, DisplaySlot> consumer) throws CommandSyntaxException {
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) context.getSource().getServer()).scbt$getContext();
        DisplaySlot slot = ScoreboardSlotArgument.getDisplaySlot(context, "slot");
        consumer.accept(scoreContext.getScheduler(), slot);
        return Command.SINGLE_SUCCESS;
    }

    private static int changeFakePlayerScore(CommandContext<CommandSourceStack> context) {
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) context.getSource().getServer()).scbt$getContext();
        scoreContext.setCarpetBotScore(BoolArgumentType.getBool(context, "enable"));
        return Command.SINGLE_SUCCESS;
    }

    private static int showFakePlayerScore(CommandContext<CommandSourceStack> context) {
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) context.getSource().getServer()).scbt$getContext();
        context.getSource().sendSystemMessage(Component.literal("FakePlayerScore: " + (scoreContext.isCarpetBotScore() ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
