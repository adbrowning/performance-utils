package com.adbrowning.util;

import java.util.Iterator;

/**
 * Created by adam on 5/21/14.
 */
public class StrandIterator implements Iterator<Character> {
    protected int startIndex;
    protected int oneBeyondLastValidIndex;
    int tokenStart = 0;

    protected byte[] bytes;
    protected int index = -1;

    public StrandIterator(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.startIndex = offset;
        if(startIndex > bytes.length || startIndex < 0) {
            throw new IllegalArgumentException("offset must be between 0 and bytes.length (" + bytes.length + "); received offset: " + offset);
        }
        this.oneBeyondLastValidIndex = offset + length;
        if(oneBeyondLastValidIndex > bytes.length) {
            throw new IllegalArgumentException("length (" + length + ") exceeds remaining length of bytes (" + (bytes.length - offset) + ")");
        }
    }

    public StrandIterator(Strand strand) {
        bytes = strand.contents;
        startIndex = strand.getStartingIndex();
        oneBeyondLastValidIndex = strand.getStrandEnd();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return index < oneBeyondLastValidIndex;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Character next() {
        return Character.valueOf(nextChar());
    }

    public char nextChar() {
        if(index == -1) {
            index = startIndex;
        } else {
            index += Strand.utf8CharSize(bytes[index]);
        }

        char retVal = Strand.decodeUTF8Char(bytes, index);
        return retVal;
    }

    public void startTokenBeforeMostRecentChar() {
        tokenStart = Math.max(index, startIndex);
    }

    public void startTokenAfterMostRecentChar() {
        tokenStart = index + Strand.utf8CharSize(bytes[Math.max(index, startIndex)]);
    }

    public Strand endTokenBeforeMostRecentChar() {
        return new Substrand(bytes, tokenStart, index);
    }

    public Strand endTokenAfterMostRecentChar() {
        return new Substrand(bytes, tokenStart, index+Strand.utf8CharSize(bytes[index]));
    }
    /**
     * Remove is not supported by this implementation
     *
     * @throws UnsupportedOperationException Remove is not supported
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }
}
