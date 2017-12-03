package quartz.lang;

/**
 * Class representing the primitive int.
 */
public final class Int extends Any {
    public final int value;

    public Int(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Integer.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Int && ((Int) obj).value == value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
