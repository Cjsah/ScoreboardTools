package net.cjsah.scoretools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scoretools.fake.ScoreboardScheduleFake;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandDisplayCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> node) {
        node.then(literal("loop").then(argument("slot", ScoreboardSlotArgumentType.scoreboardSlot())
                .then(literal("add").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(context ->
                        execute(context, (internal, slot) -> internal.add(slot, ScoreboardObjectiveArgumentType.getObjective(context, "objective"))
                )))).then(literal("remove").then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(context ->
                        execute(context, (internal, slot) -> internal.remove(slot, ScoreboardObjectiveArgumentType.getObjective(context, "objective"))
                )))).then(literal("schedule").then(argument("schedule", IntegerArgumentType.integer(1)).executes(context ->
                        execute(context, (internal, slot) -> internal.setSchedule(slot, IntegerArgumentType.getInteger(context, "schedule")))
                )))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, BiConsumer<ScoreboardSchedule, Integer> consumer) throws CommandSyntaxException {
        ScoreboardSchedule internal = ((ScoreboardScheduleFake) context.getSource().getServer()).getSchedule();
        int slot = ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot");
        consumer.accept(internal, slot);
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
