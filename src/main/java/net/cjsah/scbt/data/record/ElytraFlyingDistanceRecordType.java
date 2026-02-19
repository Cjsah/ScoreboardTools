package net.cjsah.scbt.data.record;

public enum ElytraFlyingDistanceRecordType implements IScoreMapper {
    KILO_METRE(100000),
    METRE(100);

    private final int factor;

    ElytraFlyingDistanceRecordType(int factor) {
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
