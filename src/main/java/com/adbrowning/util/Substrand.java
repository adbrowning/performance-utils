package com.adbrowning.util;

/**
 * Created by adam on 5/19/14.
 */
public class Substrand extends Strand {
    private int start;
    private int end;
    public Substrand(byte[] contents, int start, int end) {
        super(contents, false);
        if(utf8CharSize(contents[start])==-1) {
            throw new IllegalArgumentException("Substrand starts within a multi-byte character: first byte is " + contents[start]);
        }
        int numBytes = 0;
        int expectedNumBytes = -1;
        for(int i = end-1; expectedNumBytes == -1 && i >= 0; --i) {
            expectedNumBytes = utf8CharSize(contents[i]);
            ++numBytes;
        }
        if(numBytes != expectedNumBytes) {
            throw new IllegalArgumentException("End was in the midst of a multi-byte character");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Returns one greater than the last valid index
     *
     * @return
     */
    @Override
    protected int getStrandEnd() {
        return end;
    }

    @Override
    protected int getStrandLength() {
        return end - start;
    }

    @Override
    protected int getStartingIndex() {
        return start;
    }
}
