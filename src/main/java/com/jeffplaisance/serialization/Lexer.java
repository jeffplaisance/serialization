package com.jeffplaisance.serialization;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author jplaisance
 */
public final class Lexer {
    public static PeekingIterator<Token> lex(Reader reader) {
        return new LexerIterator(reader);
    }

    public static class LexerIterator extends AbstractIterator<Token> implements PeekingIterator<Token> {

        private final PeekingReader reader;
        private final StringBuilder builder = new StringBuilder();
        private boolean done = false;
        private int line = 0;
        private int position = 0;

        private LexerIterator(Reader reader) {
            this.reader = new PeekingReader(reader);
        }

        private void advance() throws IOException {
            int c = reader.read();
            if (c == '\n') {
                line++;
                position = 0;
            } else {
                position++;
            }
        }

        @Override
        protected Token computeNext() {
            try {
                if (done) {
                    endOfData();
                    return null;
                }
                while (true) {
                    int c = reader.peek();
                    if (c < 0) {
                        endOfData();
                        return null;
                    }
                    if (Character.isWhitespace(c)) {
                        advance();
                    } else {
                        break;
                    }
                }
                int startLine = line;
                int startPosition = position;
                builder.setLength(0);
                boolean quoted = false;
                boolean escaped = false;
                for (int c; (c = reader.peek()) >= 0; advance()) {
                    if (escaped) {
                        escaped = false;
                        builder.append((char)c);
                    } else {
                        if (c == '"') {
                            if (!quoted) {
                                quoted = true;
                            } else {
                                advance();
                                return new Token(StringEscapeUtils.unescapeJava(builder.toString()), startLine, startPosition);
                            }
                        } else {
                            if (c == '\\') {
                                escaped = true;
                            } else if (!quoted) {
                                if (Character.isWhitespace(c)) {
                                    return new Token(StringEscapeUtils.unescapeJava(builder.toString()), startLine, startPosition);
                                } else if (c == '(') {
                                    if (builder.length() == 0) {
                                        advance();
                                        return new Token("(", startLine, startPosition);
                                    }
                                    return new Token(StringEscapeUtils.unescapeJava(builder.toString()), startLine, startPosition);
                                } else if (c == ')') {
                                    if (builder.length() == 0) {
                                        advance();
                                        return new Token(")", startLine, startPosition);
                                    }
                                    return new Token(StringEscapeUtils.unescapeJava(builder.toString()), startLine, startPosition);
                                }
                            } else if (c == '\n') {
                                throw new IllegalArgumentException("use \\n or trailing \\ for multiline strings (keep in mind that trailing \\ does not work with windows newlines)");
                            }
                            builder.append((char)c);
                        }
                    }
                }
                done = true;
            return new Token(StringEscapeUtils.unescapeJava(builder.toString()), startLine, startPosition);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static final class Poop {}
    }

    public static void main(String[] args) {
        String str = "         (       hello\n   my name\n is   jeff( \"hell\\no( my )na\\nme is \\\\n  \" jeff\\ p\\nlaisance ) )         ";
        PeekingIterator<Token> iterator = lex(new StringReader(str));
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
