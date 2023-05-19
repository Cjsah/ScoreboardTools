package net.cjsah.scbt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.command.argument.ScoreboardPresetArgumentType;
import net.cjsah.scbt.config.LoopPreset;
import net.cjsah.scbt.config.ScbToolsConfig;
import net.cjsah.scbt.config.ScoreboardPreset;
import net.cjsah.scbt.fake.ScoreboardScheduleFake;
import net.minecraft.command.argument.ScoreboardCriterionArgumentType;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.cjsah.scbt.ScoreboardTools.feedback;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ScbPresetCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> node) {
        node.then(literal("preset")
                .then(literal("run").then(argument("preset", ScoreboardPresetArgumentType.scoreboardPreset()).executes(ScbPresetCommand::executePreset)))
                .then(literal("update").executes(ScbPresetCommand::update)));
    }

    private static int update(CommandContext<ServerCommandSource> context) {
        ScbToolsConfig.getInstance().update();
        feedback(context, "Completed");
        return Command.SINGLE_SUCCESS;
    }

    private static int executePreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        ScoreboardSchedule internal = ((ScoreboardScheduleFake) context.getSource().getServer()).getSchedule();
        LoopPreset preset = ScoreboardPresetArgumentType.getScoreboardPreset(context, "preset");
        ScoreboardPreset[] scoreboards = preset.getScoreboards();
        List<ScoreboardObjective> objectives = new ArrayList<>();
        for (ScoreboardPreset scbPreset : scoreboards) {
            String name = scbPreset.getName();
            if (scoreboard.getNullableObjective(name) != null) {
                throw ScoreboardCommand.OBJECTIVES_ADD_DUPLICATE_EXCEPTION.create();
            }
            ScoreboardCriterion criteria = ScoreboardCriterion.getOrCreateStatCriterion(scbPreset.getCriteria()).orElseThrow(() ->
                    ScoreboardCriterionArgumentType.INVALID_CRITERION_EXCEPTION.create(scbPreset.getCriteria()));
            Text text = scbPreset.getText().isEmpty() ? Text.of(name) : Text.Serializer.fromJson(scbPreset.getText());
            scoreboard.addObjective(name, criteria, text, criteria.getDefaultRenderType());
            ScoreboardObjective objective = scoreboard.getObjective(name);
            objectives.add(objective);
            context.getSource().sendFeedback(Text.translatable("commands.scoreboard.objectives.add.success", objective.toHoverableText()), true);
        }
        internal.preset(objectives, preset);
        feedback(context, "Completed");
        return Command.SINGLE_SUCCESS;
    }
}
