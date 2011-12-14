package com.jeffplaisance.serialization;

import java.io.IOException;
import java.io.Reader;

/**
 * @author jplaisance
 */
public final class PeekingReader extends Reader {

    private final Reader reader;
    private int next = -1;

    public PeekingReader(final Reader reader) {
        this.reader = reader;
    }

    public int peek() throws IOException {
        if (next < 0) next = reader.read();
        return next;
    }

    @Override
    public int read() throws IOException {
        if (next < 0) return reader.read();
        int ret = next;
        next = -1;
        return ret;
    }

    @Override
    public int read(final char[] cbuf, int off, int len) throws IOException {
        if (len == 0) return 0;
        if (len < 0) throw new IllegalArgumentException("len must be >= 0");
        if (off < 0) throw new IllegalArgumentException("off must be >= 0");
        if (off+len > cbuf.length) throw new IllegalArgumentException("off+len must be <= cbuf.length");
        if (off+len < 0) throw new IllegalArgumentException("off+len must be <= Integer.MAX_VALUE");
        if (next >= 0 ) {
            cbuf[off] = (char)next;
            next = -1;
            off++;
            len--;
        }
        return reader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
