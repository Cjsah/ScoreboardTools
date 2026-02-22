package net.cjsah.scbt.data;

import net.cjsah.scbt.data.record.IScoreMapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreboardToolSaveData extends SavedData {
    private final ScoreboardToolContext context;

    public ScoreboardToolSaveData(ScoreboardToolContext context) {
        this.context = context;
    }

    public ScoreboardToolSaveData load(CompoundTag tag, HolderLookup.Provider provider) {
        this.context.setCarpetBotScore(tag.getBoolean("FakePlayerScore"));
        this.loadBinds(tag.getList("ScoreboardBind", Tag.TAG_LIST));
        this.loadSchedule(tag.getCompound("DisplayInternal"));
        return this;
    }

    private void loadBinds(ListTag tags) {
        this.context.clearScoreBinds();
        if (tags.isEmpty()) return;
        for (int i = 0; i < tags.size(); i++) {
            CompoundTag tag = tags.getCompound(i);
            String name = tag.getString("Name");
            String type = tag.getString("Type");
            int record = tag.getInt("Record");
            ScoreType scoreType = ScoreType.getByName(type);
            if (scoreType == null) continue;
            IScoreMapper scoreMapper = scoreType.getScoreMapper(record);
            this.context.addScoreBind(name, scoreType, scoreMapper);
        }
    }

    private void loadSchedule(CompoundTag tag) {
        this.context.getScheduler().clear();
        if (tag.isEmpty()) return;
        for (String key : tag.getAllKeys()) {
            DisplaySlot slot = DisplaySlot.CODEC.byName(key);
            if (slot == null) continue;
            CompoundTag compound = tag.getCompound(key);
            List<String> objectives = compound
                .getList("Contents", Tag.TAG_STRING)
                .stream()
                .map(Tag::getAsString)
                .toList();
            int schedule = compound.getInt("Schedule");
            int internal = compound.getInt("Internal");
            int index = compound.getInt("Index");
            boolean enable = compound.getBoolean("Enable");
            this.context.initScoreScheduler(slot, objectives, schedule, internal, index, enable);
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("FakePlayerScore", this.context.isCarpetBotScore());
        tag.put("ScoreboardBind", this.saveBinds());
        tag.put("DisplayInternal", this.saveSchedule());
        return tag;
    }

    private ListTag saveBinds() {
        ListTag tags = new ListTag();
        this.context.saveScoreBind(cell -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", cell.getColumnKey().getName());
            tag.putString("Type", cell.getRowKey().getName());
            tag.putInt("Record", cell.getValue().saveId());
            tags.add(tag);
        });
        return tags;
    }

    private CompoundTag saveSchedule() {
        CompoundTag tag = new CompoundTag();
        this.context.saveScoreScheduler(schedule -> {
            CompoundTag node = new CompoundTag();
            ListTag list = new ListTag();
            for (Objective objective : schedule.getList()) {
                list.add(StringTag.valueOf(objective.getName()));
            }
            node.put("Contents", list);
            node.putInt("Schedule", schedule.getSchedule());
            node.putInt("Internal", schedule.getInternal());
            node.putInt("Index", schedule.getIndex());
            node.putBoolean("Enable", schedule.isEnable());
            tag.put(schedule.getSlot().getSerializedName(), node);
        });
        return tag;
    }

}
