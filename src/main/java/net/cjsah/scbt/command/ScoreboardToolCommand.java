package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.data.ScoreType;
import net.cjsah.scbt.data.ScoreboardScheduler;
import net.cjsah.scbt.data.ScoreboardToolContext;
import net.cjsah.scbt.data.record.DummyRecordType;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.cjsah.scbt.mixin.ScoreboardCommandInvoker;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScoreboardToolCommand {

    public static void registerCriteria(ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext commandContext) {
        for (ScoreType scoreType : ScoreType.values()) {
            ArgumentBuilder<CommandSourceStack, ?> node = literal(scoreType.getName());
            if (scoreType.getRecordType() == DummyRecordType.class) {
                resolveExecutor(node, commandContext, scoreType, DummyRecordType.DUMMY);
            } else {
                for (Enum<?> recordType : scoreType.getRecordType().getEnumConstants()) {
                    ArgumentBuilder<CommandSourceStack, ?> arg = literal(recordType.name());
                    resolveExecutor(arg, commandContext, scoreType, scoreType.getScoreMapper(recordType.ordinal()));
                    node.then(arg);
                }
            }
            builder.then(node);
        }
    }

    private static void resolveExecutor(ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext commandContext, ScoreType type, IScoreMapper scoreMapper) {
        builder
            .executes(context -> addObjective(context, type, scoreMapper, false))
            .then(argument("displayName", ComponentArgument.textComponent(commandContext))
                .executes(context -> addObjective(context, type, scoreMapper, true))
            );
    }

    private static int addObjective(CommandContext<CommandSourceStack> context, ScoreType type, IScoreMapper scoreMapper, boolean displayName)
    //#if MC >= 12109
    //$$ throws CommandSyntaxException
    //#endif
    {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "objective");
        ObjectiveCriteria criteria = ObjectiveCriteria.DUMMY;
        Component component = displayName ?
            //#if MC < 12109
            ComponentArgument.getComponent(context, "displayName")
            //#else
            //$$ ComponentArgument.getResolvedComponent(context, "displayName")
            //#endif
            : Component.literal(name);
        int result = ScoreboardCommandInvoker.invokeAddObjective(source, name, criteria, component);
        if (result == 0) {
            return result;
        }
        Scoreboard scoreboard = source.getServer().getScoreboard();
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) source.getServer()).scbt$getContext();
        Objective objective = scoreboard.getObjective(name);
        scoreContext.addScoreBind(objective, type, scoreMapper);
        return Command.SINGLE_SUCCESS;
    }

    public static void removeObjective(CommandSourceStack source, Objective objective) {
        ScoreboardToolContext scoreContext = ((ScoreboardToolFake) source.getServer()).scbt$getContext();
        scoreContext.removeObjetive(objective);
    }

    public static void registerCommand(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder
            .then(literal("loop")
                .then(argument("slot", ScoreboardSlotArgument.displaySlot())
                    .then(literal("add")
                        .then(argument("objective", ObjectiveArgument.objective())
                            .executes(ScoreboardToolCommand::add)))
                    .then(literal("remove")
                        .then(argument("objective", ObjectiveArgument.objective())
                            .executes(ScoreboardToolCommand::remove)))
                    .then(literal("schedule")
                        .then(argument("schedule", IntegerArgumentType.integer(1))
                            .executes(ScoreboardToolCommand::schedule)))
                    .then(literal("enable")
                        .executes(ScoreboardToolCommand::enable))
                    .then(literal("disable")
                        .executes(ScoreboardToolCommand::disable))
                ))
            .then(literal("fakePlayerScore")
                .executes(ScoreboardToolCommand::showFakePlayerScore)
                .then(argument("enable", BoolArgumentType.bool())
                    .executes(ScoreboardToolCommand::changeFakePlayerScore)));
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

    private static void feedbackCompleted(CommandContext<CommandSourceStack> context) {
        feedback(context, "Completed");
    }

    private static void feedback(CommandContext<CommandSourceStack> context, String text) {
        context.getSource().sendSystemMessage(Component.literal(text));
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
