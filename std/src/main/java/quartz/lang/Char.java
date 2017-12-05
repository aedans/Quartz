package quartz.lang;

/**
 * Class representing the primitive char.
 */
public final class Char extends Any {
    public final char value;

    public Char(char value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return java.lang.Character.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Char && ((Char) obj).value == value;
    }

    @Override
    public int hashCode() {
        return java.lang.Character.hashCode(value);
    }
}
