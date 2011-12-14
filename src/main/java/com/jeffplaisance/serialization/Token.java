package com.jeffplaisance.serialization;

/**
 * @author jplaisance
 */
public final class Token {

    private final String value;

    private final int line;

    private final int position;

    public Token(final String value, final int line, final int position) {
        this.value = value;
        this.line = line;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Token{" + "value='" + value + '\'' + ", line=" + line + ", position=" + position + '}';
    }
}
