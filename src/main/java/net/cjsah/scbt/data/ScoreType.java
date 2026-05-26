package net.cjsah.scbt.data;

import net.cjsah.scbt.data.record.DummyRecordType;
import net.cjsah.scbt.data.record.FlyingDistanceRecordType;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.cjsah.scbt.data.record.OnlineTimeRecordType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ScoreType {
    MINED_COUNT("minedCount", DummyRecordType.class),
    PLACED_COUNT("placedCount", DummyRecordType.class),
    LEVEL("level", DummyRecordType.class),
    ONLINE_TIME("onlineTime", OnlineTimeRecordType.class),
    FLYING_DISTANCE("flyingDistance", FlyingDistanceRecordType.class),
    FINISH_FISHING("finishFishing", DummyRecordType.class),
    ;

    private final String name;
    private final Class<? extends Enum<?>> recordType;

    ScoreType(String name, Class<? extends Enum<?>> recordType) {
        this.name = name;
        this.recordType = recordType;
        InnerClass.NAME_MAP.put(this.name, this);
    }

    public String getName() {
        return this.name;
    }

    public Class<? extends Enum<?>> getRecordType() {
        return this.recordType;
    }

    public IScoreMapper getScoreMapper(int recordType) {
        return (IScoreMapper) this.recordType.getEnumConstants()[recordType];
    }

    @Nullable
    public static ScoreType getByName(String name) {
        return InnerClass.NAME_MAP.get(name);
    }

    static class InnerClass {
        private static final Map<String, ScoreType> NAME_MAP = new HashMap<>();
    }
}
