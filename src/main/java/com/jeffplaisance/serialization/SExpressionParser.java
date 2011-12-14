package com.jeffplaisance.serialization;

import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;

import java.io.StringReader;
import java.util.List;

/**
 * @author jplaisance
 */
public final class SExpressionParser {
    public static List parse(PeekingIterator<Token> tokens) {
        List ret = Lists.newArrayList();
        if (!tokens.hasNext()) return null;
        boolean opened = false;
        while (tokens.hasNext()) {
            if (!opened) {
                final Token token = tokens.next();
                if (!token.getValue().equals("(")) throw new IllegalArgumentException();
                opened = true;
                continue;
            }
            Token token = tokens.peek();
            if (token.getValue().equals("(")) {
                ret.add(parse(tokens));
            } else if (token.getValue().equals(")")) {
                tokens.next();
                return ret;
            } else {
                ret.add(token);
                tokens.next();
            }
        }
        throw new IllegalArgumentException("no closing paren");
    }

    public static void main(String[] args) {
        String str = "         (       hello\n   my name\n is   jeff( \"hell\\no( my )na\\nme is \\\\n  \" jeff\\ p\\nlaisance ) )         ";
        PeekingIterator<Token> iterator = Lexer.lex(new StringReader(str));
        System.out.println(parse(iterator));
    }
}
