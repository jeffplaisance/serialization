package com.jeffplaisance.serialization.ast;

import com.google.common.collect.Lists;
import com.jeffplaisance.serialization.Token;

import java.util.Collections;
import java.util.List;

/**
 * @author jplaisance
 */
public final class Type {

    private final Token name;

    private final List<Type> typeParameters;

    private Type(final Token name, final List<Type> typeParameters) {
        this.name = name;
        this.typeParameters = typeParameters;
    }

    public Token getName() {
        return name;
    }

    public List<Type> getTypeParameters() {
        return typeParameters;
    }

    public static Type parse(Object type){
        Token name;
        List<Type> typeParameters;
        if (type instanceof Token) {
            name = (Token)type;
            typeParameters = Collections.emptyList();
        } else if (type instanceof List) {
            List list = (List)type;
            name = (Token)list.get(0);
            typeParameters = Lists.newArrayList();
            for (int i = 1; i < list.size(); i++) {
                typeParameters.add(parse(list.get(i)));
            }
            typeParameters = Collections.unmodifiableList(typeParameters);
        } else {
            throw new IllegalArgumentException();
        }
        return new Type(name, typeParameters);
    }

    @Override
    public String toString() {
        return "Type{" + "name='" + name + '\'' + ", typeParameters=" + typeParameters + '}';
    }
}
