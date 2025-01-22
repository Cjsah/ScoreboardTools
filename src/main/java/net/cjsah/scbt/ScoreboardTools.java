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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    public static final String MINED_COUNT = "minedCount";
    public static final String PLACED_COUNT = "placedCount";
    public static final String ONLINE_TIME = "onlineTime";

    public static boolean FakePlayerScore = true;
    public static final Set<ScoreboardObjective> MinedObjectives = new HashSet<>();
    public static final Set<ScoreboardObjective> PlacedObjectives = new HashSet<>();
    public static final HashMap<ScoreboardObjective, OnlineTimeRecoreType> OnlineObjectives = new HashMap<>();

    @Override
    public void onInitialize() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static void addScore(PlayerEntity player, Set<ScoreboardObjective> objectives) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreateScore(player, it, true).incrementScore());
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setScore(PlayerEntity player, Set<ScoreboardObjective> objectives, int score) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreateScore(player, it, true).setScore(score));
    }

    public static void readNbt(Scoreboard scoreboard, NbtCompound nbt) {
        FakePlayerScore = !nbt.contains("FakePlayerScore") || nbt.getBoolean("FakePlayerScore");
        NbtCompound bind = nbt.getCompound("ScoreboardBind");
        MinedObjectives.clear();
        PlacedObjectives.clear();
        OnlineObjectives.clear();
        if (bind.isEmpty()) return;
        readObjectives(bind, scoreboard, MINED_COUNT, MinedObjectives);
        readObjectives(bind, scoreboard, PLACED_COUNT, PlacedObjectives);
        readOnlineTimeObjectives(bind, scoreboard);
    }

    public static void writeNbt(NbtCompound nbt) {
        NbtCompound compound = new NbtCompound();
        writeObjectives(compound, MINED_COUNT, MinedObjectives);
        writeObjectives(compound, PLACED_COUNT, PlacedObjectives);
        writeOnlineTimeObjectives(compound);
        nbt.put("ScoreboardBind", compound);
        nbt.putBoolean("FakePlayerScore", FakePlayerScore);
    }

    private static void readOnlineTimeObjectives(
        NbtCompound nbt,
        Scoreboard scoreboard
    ) {
        NbtCompound compound = nbt.getCompound(ONLINE_TIME);
        for (String name : compound.getKeys()) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name);
            if (objective != null) {
                ScoreboardTools.OnlineObjectives.put(objective, OnlineTimeRecoreType.values()[compound.getInt(name)]);
            }
        }
    }

    private static void readObjectives(NbtCompound nbt, Scoreboard scoreboard, String key, Collection<ScoreboardObjective> objectives) {
        for (NbtElement name : nbt.getList(key, NbtElement.STRING_TYPE)) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name.asString());
            if (objective != null) objectives.add(objective);
        }
    }

    private static void writeOnlineTimeObjectives(
        NbtCompound nbt
    ) {
        NbtCompound compound = new NbtCompound();
        for (ScoreboardObjective objective : ScoreboardTools.OnlineObjectives.keySet()) {
            compound.putInt(objective.getName(), ScoreboardTools.OnlineObjectives.get(objective).ordinal());
        }
        nbt.put(ONLINE_TIME, compound);
    }

    private static void writeObjectives(NbtCompound nbt, String key, Collection<ScoreboardObjective> objectives) {
        NbtList list = new NbtList();
        for (ScoreboardObjective objective : objectives) {
            list.add(NbtString.of(objective.getName()));
        }
        nbt.put(key, list);
    }

    public static void feedback(CommandContext<ServerCommandSource> context, String text) {
        context.getSource().sendFeedback(() -> Text.of(text), false);
    }

    public static boolean carpetBotScore(PlayerEntity player) {
        return FakePlayerScore || CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
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
