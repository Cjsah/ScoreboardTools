package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.function.Consumer;

import static net.cjsah.scbt.ScoreboardTools.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScbCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(literal("scbt")
                .then(bind())
                .then(unbind())
                .then(literal("loop").then(argument("slot", ScoreboardSlotArgument.displaySlot())
                        .then(literal("add").then(argument("objective", ObjectiveArgument.objective()).executes(ScbCommand::add)))
                        .then(literal("remove").then(argument("objective", ObjectiveArgument.objective()).executes(ScbCommand::remove)))
                        .then(literal("schedule").then(argument("schedule", IntegerArgumentType.integer(1)).executes(ScbCommand::schedule)))
                        .then(literal("enable").executes(ScbCommand::enable))
                        .then(literal("disable").executes(ScbCommand::disable))
                ))
                .then(literal("fakePlayerScore").executes(ScbCommand::showFakePlayerScore).then(argument("enable", BoolArgumentType.bool()).executes(ScbCommand::changeFakePlayerScore)))
        );

    }

    private static LiteralArgumentBuilder<CommandSourceStack> bind() {
        LiteralArgumentBuilder<CommandSourceStack> bind = literal("bind");
        appendCriterion(bind, ScoreboardTools.MINED_COUNT, MinedObjectives::add);
        appendCriterion(bind, ScoreboardTools.PLACED_COUNT, PlacedObjectives::add);
        appendMapCriterion(bind, ScoreboardTools.ONLINE_TIME, OnlineObjectives::put, OnlineTimeRecordType.class);
        appendCriterion(bind, ScoreboardTools.LEVEL_BOARD, LevelObjectives::add);
        appendMapCriterion(bind, ScoreboardTools.ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives::put, ElytraFlyingDistanceRecordType.class);
        return bind;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unbind() {
        LiteralArgumentBuilder<CommandSourceStack> bind = literal("unbind");
        appendCriterion(bind, ScoreboardTools.MINED_COUNT, MinedObjectives::remove);
        appendCriterion(bind, ScoreboardTools.PLACED_COUNT, PlacedObjectives::remove);
        appendCriterion(bind, ScoreboardTools.ONLINE_TIME, OnlineObjectives::remove);
        appendCriterion(bind, ScoreboardTools.LEVEL_BOARD, LevelObjectives::remove);
        appendCriterion(bind, ScoreboardTools.ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives::remove);
        return bind;
    }

    private static void appendCriterion(LiteralArgumentBuilder<CommandSourceStack> literal, String name, Consumer<Objective> execute) {
        literal.then(literal(name).then(argument("name", ObjectiveArgument.objective()).executes(context -> {
            Objective objective = ObjectiveArgument.getObjective(context, "name");
            execute.accept(objective);
            feedbackCompleted(context);
            return Command.SINGLE_SUCCESS;
        })));
    }

    private static <T extends Enum<T>> void appendMapCriterion(
        LiteralArgumentBuilder<CommandSourceStack> literal,
        String name,
        BiConsumer<Objective, T> execute,
        Class<T> clazz
    ) {
        RequiredArgumentBuilder<CommandSourceStack, String> node =
            argument("name", ObjectiveArgument.objective());
        for (T type : clazz.getEnumConstants()) {
            node.then(literal(type.name()).executes(context -> {
                Objective objective = ObjectiveArgument.getObjective(context, "name");
                execute.accept(objective, type);
                feedbackCompleted(context);
                return Command.SINGLE_SUCCESS;
            }));
        }
        literal.then(literal(name).then(node));
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

    private static int executeInternal(CommandContext<CommandSourceStack> context, BiConsumer<ScoreboardSchedule, DisplaySlot> consumer) throws CommandSyntaxException {
        ScoreboardSchedule internal = ((ScoreboardScheduleFake) context.getSource().getServer()).scbt$getSchedule();
        DisplaySlot slot = ScoreboardSlotArgument.getDisplaySlot(context, "slot");
        consumer.accept(internal, slot);
        return Command.SINGLE_SUCCESS;
    }

    private static int changeFakePlayerScore(CommandContext<CommandSourceStack> context) {
        ScoreboardTools.FakePlayerScore = BoolArgumentType.getBool(context, "enable");
        return Command.SINGLE_SUCCESS;
    }

    private static int showFakePlayerScore(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("FakePlayerScore: " + (ScoreboardTools.FakePlayerScore ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
