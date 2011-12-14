package com.jeffplaisance.serialization.ast;

import com.google.common.collect.Lists;
import com.jeffplaisance.serialization.Token;

import java.util.Collections;
import java.util.List;

/**
 * @author jplaisance
 */
public final class Module {

    private final Token name;

    private final List definitions;

    public Module(final Token name, final List definitions) {
        this.name = name;
        this.definitions = definitions;
    }

    public Token getName() {
        return name;
    }

    public List getDefinitions() {
        return definitions;
    }

    public static Module parse(List input) {
        Token name = (Token)input.get(1);
        List<List> lists = (List<List>)input.get(2);
        List definitions = Lists.newArrayList();
        for (List list : lists) {;
            definitions.add(Parser.parse(list));
        }
        return new Module(name, Collections.unmodifiableList(definitions));
    }

    @Override
    public String toString() {
        return "Module{" + "name=" + name + ", definitions=" + definitions + '}';
    }
}
