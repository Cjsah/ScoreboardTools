package net.cjsah.scbt.data.record;

public enum OnlineTimeRecordType implements IScoreMapper {
    DAY(1728000),
    HOUR(72000),
    MINUTE(1200),
    SECOND(20),
    TICK(1);

    private final int factor;

    OnlineTimeRecordType(int factor) {
        this.factor = factor;
    }

    @Override
    public int saveId() {
        return this.ordinal();
    }

    @Override
    public int mapValue(int origin) {
        return origin / this.factor;
    }
}
