package com.adbrowning.util;

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


/**
 * Implements the Apostolico-Giancarlo variant of the Boyer-Moore string matching, adapted from description in _Algorithms on
 * Strings, Trees, and Sequences; Gusfield 1997
 */
public class BMSearcher {

    /**
     * Calculates the length of the prefix that matches starting at any each point in pattern; e.g. in aabaabc
     * it calculates [0, 1, 0, 3, 1, 0, 0] from the following comparisons:
     * aabaabc | aabaabc : degenerate case; not useful
     * aabaabc | abaabc  : 1 letter (a) in common before mismatch (a != b)
     * aabaabc | baabc   : mismatch on first letter (a != b)
     * aabaabc | aabc    : 3 letters (aab) in common before first mismatch (a != c)
     * aabaabc | abc     : 1 letter (a) in common before mismatch (a != b)
     * aabaabc | bc      : mismatch on first letter (a != b)
     * aabaabc | c       : mismatch on first letter (a != b)
     * @param pattern
     * @return
     */
    public static int[] calculateCommonPrefixLengths(byte[] pattern) {
        int[] retVal = new int[pattern.length];
        int leftIndex=-1;
        int rightIndex=-1;
        for(int i = 1; i < pattern.length; ++i) {
            if(i > rightIndex) {
                int numMatched = findMatchLength(pattern, 0, i);
                if(numMatched > 0) {
                    leftIndex = i;
                    rightIndex = i + numMatched - 1;
                }
                retVal[i] = numMatched;
            } else {
                /* index of first time this character of the substring rooted at leftIndex was
                *  observed as a suffix of the substring
                */
                int kPrime = i - leftIndex;
                int zAtKPrime = retVal[kPrime];
                int remainingMatchLength = rightIndex - i;
                if(zAtKPrime < remainingMatchLength) {
                    // if we started the prefix here, it would be shorter than if we started it at leftIndex
                    retVal[i] = zAtKPrime;
                } else {
                    int numMatched = findMatchLength(pattern, remainingMatchLength, rightIndex+1);
                    if(numMatched > 0) {
                        retVal[i] = numMatched;
                        leftIndex = i;
                        rightIndex = i + numMatched - 1;
                    } else {
                        retVal[i] = zAtKPrime;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Calculates the number of letters in common within pattern starting from searchingIndex and prefixIndex
     * @param pattern
     * @param prefixIndex
     * @param searchingIndex
     * @return
     */
    private static int findMatchLength(byte[] pattern, int prefixIndex, int searchingIndex) {
        int numMatched = 0;
        boolean mismatchFound = false;
        for(int i = searchingIndex; !mismatchFound && i < pattern.length; ++i) {
            if(pattern[i] != pattern[numMatched+prefixIndex]) {
                mismatchFound = true;
            } else {
                numMatched++;
            }
        }
        return numMatched;
    }
}
