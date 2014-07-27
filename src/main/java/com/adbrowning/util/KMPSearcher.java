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

import java.util.Arrays;

/**
 * Implements the Knuth-Morris-Pratt string searching algorithm
 */
public class KMPSearcher {
    private int[] prefixFunction;
    private byte[] pattern;

    public KMPSearcher(byte[] pattern) {
        this(pattern, true);
    }

    public KMPSearcher(byte[] pattern, boolean copy) {
        if(copy) {
            this.pattern = Arrays.copyOf(pattern, pattern.length);
        } else {
            this.pattern = pattern;
        }
        prefixFunction = calculatePrefixFunction(pattern);
    }

    public int find(byte[] text, int start, int end) {
        int retVal = -1;
        int q = 0;
        for(int i = start; i < end; ++i) {
            while(q > 0 && pattern[q] != text[i]) {
                q = prefixFunction[q-1];
            }
            if(pattern[q] == text[i]) {
                ++q;
            }
            if(q == pattern.length) {
                retVal = (i - pattern.length) + 1;
                break;
            }
        }
        return retVal;
    }
    /**
     * Calculates the prefix pattern as adapted from pseudocode in 3rd edition Introduction to Algorithms by Cormen, et al.
     * @param pattern
     * @return
     */
    protected final int[] calculatePrefixFunction(byte[] pattern) {
        int[] pi = new int[pattern.length];
        pi[0] = -1;
        int k = -1;
        for(int q = 1; q < pi.length; ++q) {
            while(k > -1 && pattern[k+1] != pattern[q]) {
                k = pi[k];
            }
            if(pattern[k+1] == pattern[q]) {
                k++;
            }
            pi[q] = k;
        }
        for(int i = 0; i < pi.length; ++i) {
            pi[i] = pi[i]+1;
        }
        return pi;
    }
}
