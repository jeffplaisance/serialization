package com.jeffplaisance.serialization.ast;

import com.jeffplaisance.serialization.Token;

import java.util.List;

/**
 * @author jplaisance
 */
public final class Include {

    private final Token fileName;

    public Include(final Token fileName) {
        this.fileName = fileName;
    }

    public Token getFileName() {
        return fileName;
    }

    public static Include parse(List input) {
        Token fileName = (Token)input.get(1);
        return new Include(fileName);
    }
}
