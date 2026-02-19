package net.cjsah.scbt.data.record;

public enum DummyRecordType implements IScoreMapper {
    DUMMY;

    @Override
    public int saveId() {
        return this.ordinal();
    }

    @Override
    public int mapValue(int origin) {
        return origin;
    }
}
