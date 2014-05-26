/*
 *  Copyright 2014 Adam Browning
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.adbrowning.util;

import java.util.Iterator;

/**
 * Provides linear access to a Strand; this has the benefit of having constant time access to the next element, whereas
 * a for loop calling charAt on a Strand may be O(n^2), as it has to do a linear scan to find the nth char if there are
 * multi-byte characters. This also adds some convenience methods to make tokenizing easier.
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

    /**
     * Sets an internal start token marker before the last char returned from nextChar (or next); if next has not yet been called, the
     * marker is set immediately before the first character
     */
    public void startTokenBeforeMostRecentChar() {
        tokenStart = Math.max(index, startIndex);
    }

    /**
     * Sets an internal start token marker after the last char returned from nextChar (or next); if next has not yet been called,
     * the marker is set immediately after the first char. This should NOT be called before calling next (or nextChar) at least once
     */
    public void startTokenAfterMostRecentChar() {
        tokenStart = index + Strand.utf8CharSize(bytes[Math.max(index, startIndex)]);
    }

    /**
     * Returns the substrand between the start token marker and the most recent char returned from a call to next, exclusive
     * (i.e. the char returned from the call to next is not included)
     * @return
     */
    public Strand endTokenBeforeMostRecentChar() {
        return new Substrand(bytes, tokenStart, index);
    }

    /**
     * Returns the substrand between the start token marker and the most recent char returned from a call to next, inclusive
     * (i.e. the char returned from the call to next is included)
     * @return
     */
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
