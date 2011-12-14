package com.jeffplaisance.serialization.ast;

import com.jeffplaisance.serialization.Token;

import java.util.List;

/**
* @author jplaisance
*/
public final class Field {
    private final Token name;
    private final Type valueType;
    private final int tag;
    private final Token defaultValue;

    public Field(final Token name, final Type valueType, final int tag, final Token defaultValue) {
        this.name = name;
        this.valueType = valueType;
        this.tag = tag;
        this.defaultValue = defaultValue;
    }

    public Token getName() {
        return name;
    }

    public Type getValueType() {
        return valueType;
    }

    public int getTag() {
        return tag;
    }

    public Token getDefaultValue() {
        return defaultValue;
    }

    public static Field parse(List list) {
        Token name = (Token)list.get(0);
        Type valueType = Type.parse(list.get(1));
        int tag = Integer.parseInt(((Token)list.get(2)).getValue());
        Token defaultValue = list.size() == 4 ? (Token)list.get(3) : null;
        return new Field(name, valueType, tag, defaultValue);
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" +
                name +
                '\'' +
                ", valueType=" +
                valueType +
                ", tag=" +
                tag +
                ", defaultValue='" +
                defaultValue +
                '\'' +
                '}';
    }
}
