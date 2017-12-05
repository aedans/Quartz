package quartz.lang;

/**
 * Class representing the primitive short.
 */
public final class Short extends Any {
    public final short value;

    public Short(short value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Short.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Short && ((Short) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Short.hashCode(value);
    }
}
