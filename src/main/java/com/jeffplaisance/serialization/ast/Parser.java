package com.jeffplaisance.serialization.ast;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.PeekingIterator;
import com.jeffplaisance.serialization.Lexer;
import com.jeffplaisance.serialization.SExpressionParser;
import com.jeffplaisance.serialization.Token;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * @author jplaisance
 */
public final class Parser {

    private static final Map<String, Function<List, Object>> parserMap;

    static {
        ImmutableMap.Builder<String, Function<List, Object>> builder = ImmutableMap.builder();
        builder.put(Struct.class.getSimpleName().toLowerCase(), new Function<List, Object>() {
            public Object apply(final List input) {
                return Struct.parse(input);
            }
        });
        builder.put(Union.class.getSimpleName().toLowerCase(), new Function<List, Object>() {
            public Object apply(final List input) {
                return Union.parse(input);
            }
        });
        builder.put(Module.class.getSimpleName().toLowerCase(), new Function<List, Object>() {
            public Object apply(final List input) {
                return Module.parse(input);
            }
        });
        builder.put(Include.class.getSimpleName().toLowerCase(), new Function<List, Object>() {
            public Object apply(final List input) {
                return Include.parse(input);
            }
        });
        parserMap = builder.build();
    }

    public static Object parse(List list) {
        return parserMap.get(((Token)list.get(0)).getValue()).apply(list);
    }

    public static void main(String[] args) {
        String str = "(module std (" +
                     "    (union (Option A) (\n" +
                     "        (None Nothing 0)\n" +
                     "        (Some (Some A) 1)\n" +
                     "    ))\n" +
                     "    \n" +
                     "    (struct (Some A) (\n" +
                     "        (some A 0)\n" +
                     "    ))" +
                     "))";
        final PeekingIterator<Token> iterator = Lexer.lex(new StringReader(str));
        for (List list; (list = SExpressionParser.parse(iterator)) != null;) {
            System.out.println(Parser.parse(list));
        }
    }
}
