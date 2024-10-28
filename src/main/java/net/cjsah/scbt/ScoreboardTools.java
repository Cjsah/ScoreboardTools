package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    public static final String MINED_COUNT = "minedCount";
    public static final String PLACED_COUNT = "placedCount";

    public static boolean FakePlayerScore = true;
    public static final Set<ScoreboardObjective> MinedObjectives = new HashSet<>();
    public static final Set<ScoreboardObjective> PlacedObjectives = new HashSet<>();

    @Override
    public void onInitialize() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static void addScore(PlayerEntity player, Set<ScoreboardObjective> objectives) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreateScore(player, it, true).incrementScore());
    }

    public static void readNbt(Scoreboard scoreboard, NbtCompound nbt) {
        FakePlayerScore = !nbt.contains("FakePlayerScore") || nbt.getBoolean("FakePlayerScore");
        NbtCompound bind = nbt.getCompound("ScoreboardBind");
        MinedObjectives.clear();
        PlacedObjectives.clear();
        if (bind.isEmpty()) return;
        for (NbtElement name : bind.getList(MINED_COUNT, NbtElement.STRING_TYPE)) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name.asString());
            if (objective != null) MinedObjectives.add(objective);
        }
        for (NbtElement name : bind.getList(PLACED_COUNT, NbtElement.STRING_TYPE)) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name.asString());
            if (objective != null) PlacedObjectives.add(objective);
        }
    }

    public static void writeNbt(NbtCompound nbt) {
        NbtCompound compound = new NbtCompound();
        NbtList list = new NbtList();
        for (ScoreboardObjective objective : MinedObjectives) {
            list.add(NbtString.of(objective.getName()));
        }
        compound.put(MINED_COUNT, list);
        list = new NbtList();
        for (ScoreboardObjective objective : PlacedObjectives) {
            list.add(NbtString.of(objective.getName()));
        }
        compound.put(PLACED_COUNT, list);
        nbt.put("ScoreboardBind", compound);
        nbt.putBoolean("FakePlayerScore", FakePlayerScore);
    }

    public static void feedback(CommandContext<ServerCommandSource> context, String text) {
        context.getSource().sendFeedback(() -> Text.of(text), false);
    }

    public static boolean carpetBotScore(PlayerEntity player) {
        return !FakePlayerScore || CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
    }

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("carpet.patches.EntityPlayerMPFake");
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        CARPET_PLAYER_CLASS = clazz;
    }
}
