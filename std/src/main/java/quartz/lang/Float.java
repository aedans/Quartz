package quartz.lang;

/**
 * Class representing the primitive float.
 */
public final class Float extends Any {
    public final float value;

    public Float(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Float.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Float && ((Float) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Float.hashCode(value);
    }
}
