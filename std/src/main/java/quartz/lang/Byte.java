package quartz.lang;

/**
 * Class representing the primitive byte.
 */
public final class Byte {
    public final byte value;

    public Byte(byte value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Byte.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Byte && ((Byte) obj).value == value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
