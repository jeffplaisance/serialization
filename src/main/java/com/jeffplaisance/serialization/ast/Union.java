package com.jeffplaisance.serialization.ast;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author jplaisance
 */
public final class Union {

    private final Type type;

    private final List<Field> fields;

    private Union(final Type type, final List<Field> fields) {
        this.type = type;
        this.fields = fields;
    }

    public Type getType() {
        return type;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static Union parse(final List input) {
        Type type = Type.parse(input.get(1));
        List<List> lists = (List<List>)input.get(2);
        List<Field> fields = Lists.newArrayList();
        for (List list : lists) {
            fields.add(Field.parse(list));
        }
        return new Union(type, Collections.unmodifiableList(fields));
    }

    @Override
    public String toString() {
        return "Union{" + "type=" + type + ", fields=" + fields + '}';
    }
}
