package com.adbrowning.util;

import java.util.Arrays;

/**
 * Created by adam on 5/16/14.
 */
public class Strand implements CharSequence {
    protected byte[] contents;
    protected boolean hasMultiByteChars = false;

    protected Strand() {}

    public Strand(byte[] utf8Bytes) {
        this(utf8Bytes, false);
    }

    public Strand(byte[] utf8Bytes, boolean makeCopy) {
        if(makeCopy) {
            contents = Arrays.copyOf(utf8Bytes, utf8Bytes.length);
        } else {
            contents = utf8Bytes;
        }
        for(int i = 0; !hasMultiByteChars && i < contents.length; ++i) {
            hasMultiByteChars = utf8CharSize(contents[i]) > 1;
        }
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.</p>
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public int length() {
        return length(getStartingIndex(), getStrandEnd());
    }

    protected int length(int start, int end) {
        int retVal = 0;
        if(hasMultiByteChars) {
            for(int index = start; index < end;) {
                ++retVal;
                index += utf8CharSize(contents[index]);
            }
        } else {
            retVal = end-start;
        }

        return retVal;
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing. </p>
     * <p/>
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than
     *                                   <tt>length()</tt>
     */
    @Override
    public char charAt(int index) {
        return charAt(index, getStartingIndex());
    }

    public char charAt(int index, int firstByte) {
        if(hasMultiByteChars) {
            int arrayIndex = firstByte;
            int charIndex = 0;
            while(charIndex < index) {
                ++charIndex;
                int numBytes = utf8CharSize(contents[arrayIndex]);
                arrayIndex += numBytes;
            }

            char retVal = decodeUTF8Char(contents, arrayIndex);
            return retVal;
        }
        return (char) (0xFF & contents[firstByte + index]);
    }

    /**
     * Returns a new <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index <tt>end - 1</tt>.  The length
     * (in <code>char</code>s) of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned. </p>
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     *                                   if <tt>end</tt> is greater than <tt>length()</tt>,
     *                                   or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        int rawStart = getStartingIndex();
        int currentChar = 0;
        for(; currentChar < start; ++currentChar) {
            rawStart += utf8CharSize(contents[rawStart]);
        }
        int rawEnd = rawStart;
        for(; currentChar < end; ++currentChar) {
            rawEnd += utf8CharSize(contents[rawEnd]);
        }
        return new Substrand(contents, rawStart, rawEnd);
    }

    /**
     * Implements startsWith as defined in String, but accepting a raw array of UTF8 bytes
     * @param prefix
     * @return
     */
    public boolean startsWith(byte[] prefix) {
        return startsWithStartingFrom(prefix, getStartingIndex());
    }

    /**
     * Implements startsWith as defined in String, but using a provided offset so that this can be reused
     * from a subsequence that didn't copy its contents into a new, smaller array
     * @param prefix
     * @param startAt
     * @return
     */
    protected boolean startsWithStartingFrom(byte[] prefix, int startAt) {
        if(prefix.length > getStrandLength()) {
            return false;
        }
        boolean retVal = true;
        for(int i = 0; retVal && i < prefix.length; ++i) {
            retVal = prefix[i] == contents[startAt+i];
        }
        return retVal;
    }

    /**
     * Returns one greater than the last valid index
     * @return
     */
    protected int getStrandEnd() {
        return contents.length;
    }

    protected int getStrandLength() {
        return contents.length;
    }

    protected int getStartingIndex() {
        return 0;
    }

    protected char decodeUTF8Char(byte[] utf8Bytes, int startingIndex) {
        char retVal = 0;
        int numBytes = utf8CharSize(utf8Bytes[startingIndex]);
        if(numBytes == -1) {
            throw new IllegalStateException("Byte with value " + Integer.toHexString(utf8Bytes[startingIndex]) + " at " +
                    startingIndex + " is not a valid UTF-8 char header");
        }
        int mask;
        switch(numBytes) {
            case 1:
                mask = 0xFF;
                break;
            case 2:
                mask = 0x1F;
                break;
            case 3:
                mask = 0x0F;
                break;
            case 4:
                mask = 0x07;
                break;
            case 5:
                mask = 0x03;
                break;
            case 6:
                mask = 1;
                break;
            default:
                throw new IllegalStateException("Unknown number of bytes in code point: " + numBytes);
        }

        for(int i = 0; i < numBytes; ++i) {
            retVal <<= 6;
            int addedBits = mask & utf8Bytes[i+startingIndex];
            retVal |= addedBits;
            mask = 0x3F;
        }
        return retVal;
    }

    /*
     * Decodes the number of bytes consumed by a UTF-8 char based on its leading byte
     */
    protected int utf8CharSize(int b) {
        if((b & 0x80) == 0) {
            return 1;
        }
        if((b & 0xFC) == 0xFC) {
            return 6;
        }
        if((b & 0xF8) == 0xF8) {
            return 5;
        }
        if((b & 0xF0) == 0xF0) {
            return 4;
        }
        if((b & 0xE0) == 0xE0) {
            return 3;
        }
        if((b & 0xC0) == 0xC0) {
            return 2;
        }
        return -1;
    }
}
