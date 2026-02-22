package net.cjsah.scbt.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.scores.DisplaySlot;

import java.util.List;
import java.util.Map;

public class ScoreboardToolSaveData extends SavedData {
    public static final SavedDataType<ScoreboardToolSaveData> TYPE = new SavedDataType<>(
        "scoreboard_tool_data",
        ScoreboardToolSaveData::new,
        Packed.CODEC.xmap(ScoreboardToolSaveData::new, ScoreboardToolSaveData::getData),
        DataFixTypes.SAVED_DATA_SCOREBOARD
    );

    private ScoreboardToolSaveData.Packed packed;

    public ScoreboardToolSaveData() {
        this.packed = Packed.EMPTY;
    }

    public ScoreboardToolSaveData(ScoreboardToolSaveData.Packed packed) {
        this.packed = packed;
    }

    public Packed getData() {
        return this.packed;
    }

    public void setData(ScoreboardToolSaveData.Packed packed) {
        if (!packed.equals(this.packed)) {
            this.packed = packed;
            this.setDirty();
        }
    }

    public record Packed(boolean fakePlayerScore, List<BindPacked> binds, Map<DisplaySlot, SchedulePacked> schedules) {
        public static final ScoreboardToolSaveData.Packed EMPTY = new ScoreboardToolSaveData.Packed(true, List.of(), Map.of());
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("FakePlayerScore").forGetter(Packed::fakePlayerScore),
            BindPacked.CODEC.listOf().optionalFieldOf("ScoreboardBind", List.of()).forGetter(Packed::binds),
            Codec.unboundedMap(DisplaySlot.CODEC, SchedulePacked.CODEC).optionalFieldOf("DisplayInternal", Map.of()).forGetter(Packed::schedules)
        ).apply(instance, Packed::new));
    }

    public record BindPacked(String name, String type, int record) {
        public static final Codec<BindPacked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("Name").forGetter(BindPacked::name),
            Codec.STRING.fieldOf("Type").forGetter(BindPacked::type),
            Codec.INT.fieldOf("Record").forGetter(BindPacked::record)
        ).apply(instance, BindPacked::new));
    }

    public record SchedulePacked(List<String> contents, int schedule, int internal, int index, boolean enable) {
        public static final Codec<SchedulePacked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("Contents").forGetter(SchedulePacked::contents),
            Codec.INT.fieldOf("Schedule").forGetter(SchedulePacked::schedule),
            Codec.INT.fieldOf("Internal").forGetter(SchedulePacked::internal),
            Codec.INT.fieldOf("Index").forGetter(SchedulePacked::index),
            Codec.BOOL.fieldOf("Enable").forGetter(SchedulePacked::enable)
        ).apply(instance, SchedulePacked::new));

    }
}
