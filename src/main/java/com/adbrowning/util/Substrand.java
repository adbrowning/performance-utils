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

/**
 * Implementation of Strand that accepts a beginning and end index, allowing a cheap implementation of subSequence for Strand.
 */
public class Substrand extends Strand {
    private int start;
    private int end;
    public Substrand(byte[] contents, int start, int end) {
        super(contents, false);
        if(utf8CharSize(contents[start])==-1) {
            throw new IllegalArgumentException("Substrand starts within a multi-byte character: first byte is " + contents[start]);
        }
        this.start = start;
        this.end = end;
    }

    public Substrand(byte[] contents, int start, int end, boolean hadMultibyte) {
        super(contents, false, hadMultibyte);
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
