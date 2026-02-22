package net.cjsah.scbt.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardToolSaveData extends SavedData {
    private final ScoreboardToolContext context;

    public ScoreboardToolSaveData(ScoreboardToolContext context) {
        this.context = context;
    }

    public void load(Packed packed) {
        this.context.setCarpetBotScore(packed.fakePlayerScore);
        this.loadBinds(packed.binds);
        this.loadSchedule(packed.schedules);
    }

    private void loadBinds(List<BindPacked> binds) {
        this.context.clearScoreBinds();
        for (BindPacked bind : binds) {
            ScoreType scoreType = ScoreType.getByName(bind.type);
            if (scoreType == null) continue;
            IScoreMapper scoreMapper = scoreType.getScoreMapper(bind.record);
            this.context.addScoreBind(bind.name, scoreType, scoreMapper);
        }
    }

    private void loadSchedule(Map<DisplaySlot, SchedulePacked> schedules) {
        this.context.getScheduler().clear();
        for (Map.Entry<DisplaySlot, SchedulePacked> entry : schedules.entrySet()) {
            SchedulePacked schedule = entry.getValue();
            this.context.initScoreScheduler(
                entry.getKey(),
                schedule.contents,
                schedule.schedule,
                schedule.internal,
                schedule.index,
                schedule.enable
            );
        }
    }

    public Packed pack() {
        return new Packed(this.context.isCarpetBotScore(), this.saveBinds(), this.saveSchedule());
    }

    private List<BindPacked> saveBinds() {
        List<BindPacked> binds = new ArrayList<>();
        this.context.saveScoreBind(cell ->
            binds.add(new BindPacked(
                cell.getColumnKey().getName(),
                cell.getRowKey().getName(),
                cell.getValue().saveId()
            )));
        return binds;
    }

    private Map<DisplaySlot, SchedulePacked> saveSchedule() {
        Map<DisplaySlot, SchedulePacked> schedules = new HashMap<>();
        this.context.saveScoreScheduler((slot, impl) -> {
            List<String> contents = impl.getList().stream().map(Objective::getName).toList();
            schedules.put(slot, new SchedulePacked(
                contents,
                impl.getSchedule(),
                impl.getInternal(),
                impl.getIndex(),
                impl.isEnable()
            ));
        });
        return schedules;
    }

    public record Packed(boolean fakePlayerScore, List<BindPacked> binds, Map<DisplaySlot, SchedulePacked> schedules) {
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
