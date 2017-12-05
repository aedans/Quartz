package quartz.lang;

/**
 * Class representing the primitive long.
 */
public final class Long extends Any {
    public final long value;

    public Long(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Long.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Long && ((Long) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Long.hashCode(value);
    }
}
