package quartz.lang;

/**
 * Class representing the primitive boolean.
 */
public final class Bool {
    public final boolean value;

    public Bool(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Boolean.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bool && ((Bool) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Boolean.hashCode(value);
    }
}
