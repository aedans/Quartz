package quartz.lang;

/**
 * Class representing the primitive double.
 */
public final class Double extends Any {
    public final double value;

    public Double(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Double.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Double && ((Double) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Double.hashCode(value);
    }
}
