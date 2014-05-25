package com.adbrowning.util;

import java.io.UnsupportedEncodingException;
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

    public Strand(byte[] utf8Bytes, boolean makeCopy, boolean hasMultiByteChars) {
        if(makeCopy) {
            contents = Arrays.copyOf(utf8Bytes, utf8Bytes.length);
        } else {
            contents = utf8Bytes;
        }
        this.hasMultiByteChars = hasMultiByteChars;
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
     * Implements endWith as defined in String, but accepting a raw array of UTF8 bytes
     * @param suffix
     * @return
     */
    public boolean endsWith(byte[] suffix) {
        if(suffix.length > getStrandLength()) return false;
        boolean retVal = true;
        int strandEnd = getStrandEnd();
        for(int i = 1; retVal && i <= suffix.length; ++i) {
            retVal = suffix[suffix.length-i] == contents[strandEnd-i];
        }
        return retVal;
    }
    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String retVal = null;
        try {
            int start = getStartingIndex();
            int end = getStrandEnd();
            retVal = new String(contents, start, end - start, "utf8");;
        } catch (UnsupportedEncodingException e) {
            // this should never happen, but...
            char[] sink = new char[getStrandLength()];
            int charNum = 0;
            for(int i = getStartingIndex(); i < getStrandEnd();) {
                int numBytes = utf8CharSize(contents[i]);
                sink[charNum++] = decodeUTF8Char(contents, i);
                i += numBytes;
            }
            return new String(sink, 0, charNum);
        }

        return retVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof Strand)) {

            Strand strand = (Strand) o;
            int myLength = getStrandLength();
            int otherLength = strand.getStrandLength();
            if(myLength != otherLength) {
                return false;
            }
            int myStart = getStartingIndex();
            int otherStart = ((Strand) o).getStartingIndex();
            for(int i = 0; i < myLength; ++i) {
                if(contents[i+myStart] != ((Strand) o).contents[i+otherStart]) {
                    return false;
                }
            }
        } else if((o instanceof CharSequence)) {
            int myLenth = length();
            CharSequence other = (CharSequence) o;
            if(myLenth != other.length()) {
                return false;
            }
            for(int myIndex = getStartingIndex(), otherIndex = 0; myIndex < getStrandEnd(); ++otherIndex) {
                int numBytes = utf8CharSize(contents[myIndex]);
                if(other.charAt(otherIndex) != decodeUTF8Char(contents, myIndex)) {
                    return false;
                }
                myIndex += numBytes;
            }
        }  else {
            return false;
        }
        return true;
    }

    public int indexOf(String str) {
        int retVal;
        try {
            retVal = indexOf(str.getBytes("utf8"));
        }  catch(UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 encoding not supported");
        }
        return retVal;
    }

    public int indexOf(byte[] bytes) {
        int retVal = -1;

        int lastPossibleStart = getStrandEnd() - bytes.length;
        for(int i = getStartingIndex(); retVal == -1 && i <= lastPossibleStart; ++i) {

            if(contents[i] == bytes[0]) {
                boolean restMatched = true;
                // check the rest of the contents
                for(int j = 1; restMatched && j < bytes.length; ++j) {
                    if(contents[i+j] != bytes[j]) {
                        if(contents[i+j] == bytes[0]) {
                            i += (j-1);
                        } else {
                            i += j;
                        }
                        restMatched = false;
                    }
                }
                if(restMatched) {
                    if(hasMultiByteChars) {
                        retVal = 0;
                        // this looks weird, but it's because retVal increases by 1 for each char, but the index may increase by up to 6
                        for(int j = getStartingIndex(); j < i; ++retVal) {
                            j += utf8CharSize(contents[j]);
                        }
                    } else {
                        retVal = i;
                    }
                }
            } else {
                continue;
            }
        }
        return retVal;

    }
    @Override
    public int hashCode() {
        int retVal = 0;
        for(int i = getStartingIndex(); i < getStrandEnd(); ++i) {
            retVal = 31 * retVal + (0xFF & contents[i]);
        }
        return retVal;
    }


    public Strand[] split(byte[] sequence) {
        Strand[] allSplits = new Strand[10];
        int totalNumStrands = 0;
        Strand[] sink = new Strand[10];
        Strand toSplit = this;
        int numSplits;
        for(numSplits = toSplit.split(sequence, sink); numSplits == sink.length; numSplits = toSplit.split(sequence, sink)) {
            if(totalNumStrands + numSplits - 1 >= allSplits.length) {
                allSplits = Arrays.copyOf(allSplits, allSplits.length * 2);
            }
            System.arraycopy(sink, 0, allSplits, totalNumStrands, numSplits - 1);
            toSplit = sink[numSplits-1];
            totalNumStrands += numSplits - 1;
        }
        if(numSplits > 0) {
            if (totalNumStrands + numSplits >= allSplits.length) {
                allSplits = Arrays.copyOf(allSplits, totalNumStrands + numSplits);
            }
            System.arraycopy(sink, 0, allSplits, totalNumStrands, numSplits);
            totalNumStrands += numSplits;
        }
        return Arrays.copyOfRange(allSplits, 0, totalNumStrands);
    }

    public Strand[] split(byte[] sequence, int maxNumSplits) {

        final Strand[] retVal = new Strand[maxNumSplits];
        int numSplits = split(sequence, retVal);
        return Arrays.copyOfRange(retVal, 0, numSplits);
    }

    public int split(byte[] sequence, Strand[] splitInto) {

        int tokenStarts = getStartingIndex();
        int endIndex = getStrandEnd();
        int resultsIndex = 0;
        for(; tokenStarts < endIndex && resultsIndex < splitInto.length - 1; ++resultsIndex) {
            tokenStarts = nextSplit(sequence, splitInto, resultsIndex, tokenStarts)+1;
        }
        if(tokenStarts < endIndex) {
            splitInto[resultsIndex++] = new Substrand(contents, tokenStarts, endIndex, hasMultiByteChars);
        }
        for(int i = resultsIndex-1; i >= 0; --i) {
            if(splitInto[i].getStrandLength() > 0) {
                break;
            }
            --resultsIndex;
        }
        return resultsIndex;
    }

    /**
     * Extracts from startFrom to immediately before the start of sequence into splitInto at the index splitIntoIndex,
     * returning the index of the last byte of sequence or the end of the strand if sequence does not appear
     * @param sequence
     * @param splitInto
     * @param splitIntoIndex
     * @param startFrom the byte index from which to start searching
     * @return the index of the position of the last byte of sequence; NOTE: this is the byte index, not the char index
     */
    protected int nextSplit(byte[] sequence, Strand[] splitInto, int splitIntoIndex, int startFrom) {
        int endIndex = getStrandEnd();
        int sequenceIndex = 0;
        int potentialTokenEnd = endIndex;
        boolean hasMultibyte = false;
        for(int i = startFrom; i < endIndex; ++i) {
            hasMultibyte = hasMultiByteChars && (hasMultibyte || contents[i] < 0);
            if(sequence[sequenceIndex] == contents[i]) {
                potentialTokenEnd = Math.min(potentialTokenEnd, i);
                if(sequenceIndex == sequence.length-1) {
                    splitInto[splitIntoIndex] = new Substrand(contents, startFrom, potentialTokenEnd, hasMultibyte);
                    return i;
                }
                ++sequenceIndex;
            } else {
                sequenceIndex = contents[i] == sequence[0] ? 1 : 0;
                potentialTokenEnd = endIndex;
            }
        }
        splitInto[splitIntoIndex] = new Substrand(contents, startFrom, endIndex, hasMultibyte);
        // sequence didn't appear, so return one past the last index
        return endIndex;
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

    protected static char decodeUTF8Char(byte[] utf8Bytes, int startingIndex) {
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
    protected static int utf8CharSize(int b) {
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
