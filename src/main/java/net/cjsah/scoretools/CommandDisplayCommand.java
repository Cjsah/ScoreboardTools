package net.cjsah.scoretools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scoretools.fake.ScoreboardInternalFake;
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
                ))).then(literal("internal").then(argument("internal", IntegerArgumentType.integer(1)).executes(context ->
                        execute(context, (internal, slot) -> internal.setInternal(slot, IntegerArgumentType.getInteger(context, "internal")))
                ))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, BiConsumer<ScoreboardInternal, Integer> consumer) throws CommandSyntaxException {
        ScoreboardInternal internal = ((ScoreboardInternalFake) context.getSource().getServer()).getInternal();
        int slot = ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot");
        consumer.accept(internal, slot);
        return Command.SINGLE_SUCCESS;
    }

    @FunctionalInterface
    private interface BiConsumer<T, R> {
        void accept(T t, R r) throws CommandSyntaxException;
    }
}
