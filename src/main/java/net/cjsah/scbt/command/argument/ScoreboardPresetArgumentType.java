package net.cjsah.scbt.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.cjsah.scbt.config.LoopPreset;
import net.cjsah.scbt.config.ScbToolsConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ScoreboardPresetArgumentType implements ArgumentType<LoopPreset> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "bar");

    private ScoreboardPresetArgumentType() {}

    public static ScoreboardPresetArgumentType scoreboardPreset() {
        return new ScoreboardPresetArgumentType();
    }

    public static LoopPreset getScoreboardPreset(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, LoopPreset.class);
    }

    @Override
    public LoopPreset parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        return ScbToolsConfig.getInstance().getPreset(name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ScbToolsConfig.getInstance().getPresetsNames(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
