package com.adbrowning.util

import spock.lang.Specification

class StrandSpec extends Specification {


    def "Subsequence cutting off a multi-byte char"() {
        Strand theStrand = new Strand("\u0080ABCD\u0080".getBytes("utf8"))
        CharSequence subsequence = theStrand.subSequence(1, 5)
        expect:
        4 == subsequence.length()
        expected == subsequence.charAt(index)
        where:
        expected    |   index
        'A'         |   0
        'B'         |   1
        'C'         |   2
        'D'         |   3
    }

    def "Equals should produce same results for equal but non-identical Strands"() {
        expect:
        new Strand("ab\u0080cd".getBytes("utf8")) == new Strand("ab\u0080cd".getBytes("utf8"))
        new Strand("ab\u0080cd".getBytes("utf8")) == (new Strand("aab\u0080cdd".getBytes("utf8"))).subSequence(1, 6)
        new Strand("ab\u0080cd".getBytes("utf8")) != new Strand("aab\u0080cd".getBytes("utf8"))
        new Strand("ab\u05D0cd".getBytes("utf8")).equals(new String("ab\u05D0cd"))
    }


    def "Test endsWith"() {
        Strand theStrand = new Strand("\u0080ABCD\u0080E".getBytes("utf8"))
        expect:
        theStrand.endsWith("E".getBytes("utf8"))
        theStrand.endsWith("\u0080E".getBytes("utf8"))
        !theStrand.endsWith("E\u0080".getBytes("utf8"))
        theStrand.subSequence(1, 6).endsWith("\u0080".getBytes("utf8"))
    }

    def "Simple index of"() {
        Strand theStrand = new Strand("abc".getBytes("utf8"))
        expect:
        index == theStrand.indexOf(str)
        where:
        index   |   str
        0   |   "a"
        0   |   "ab"
        0   |   "abc"
        -1  |   "aa"
        1   |   "b"
        1   |   "bc"
        2   |   "c"
        -1  |   "d"
        -1  |   "abcd"
    }

    def "indexOf with multi-byte chars"() {
        Strand theStrand = new Strand("ab\u05d0cd".getBytes("utf8"))
        expect:
        index == theStrand.indexOf(str)
        where:
        index   |   str
        0   |   "a"
        0   |   "ab"
        0   |   "ab\u05D0"
        0   |   "ab\u05D0c"
        2   |   "\u05D0"
        2   |   "\u05D0c"
        3   |   "c"
        -1  |   "ab\u05D0d"
    }

    def "Test split with fewer than batch size splits"() {
        given:
        Strand theStrand = new Strand("a|b|c".getBytes("utf8"))
        Strand[] results = theStrand.split("|".getBytes("utf8"))
        Strand[] expected = [new Strand("a".getBytes("utf8")), new Strand("b".getBytes("utf8")), new Strand("c".getBytes("utf8"))]
        expect:
        results.length == expected.length
        for(int i = 0; i < expected.length; ++i) {
            results[i] == expected[i]
        }
    }

    def "Test split with exactly batch size splits"() {
        given:
        Strand theStrand = new Strand("a|b|c|d|e|f|g|h|i|j".getBytes("utf8"))
        Strand[] results = theStrand.split("|".getBytes("utf8"))
        Strand[] expected = [new Strand("a".getBytes("utf8")), new Strand("b".getBytes("utf8")), new Strand("c".getBytes("utf8")),
                             new Strand("d".getBytes("utf8")), new Strand("e".getBytes("utf8")), new Strand("f".getBytes("utf8")),
                             new Strand("g".getBytes("utf8")), new Strand("h".getBytes("utf8")), new Strand("i".getBytes("utf8")),
                             new Strand("j".getBytes("utf8"))]
        expect:
        results.length == expected.length
        for(int i = 0; i < expected.length; ++i) {
            assert results[i] == expected[i]
        }
    }

    def "Test split with more than batch size splits"() {
        Strand theStrand = new Strand("a|b|c|d|e|f|g|h|i|j|k|".getBytes("utf8"))
        Strand[] results = theStrand.split("|".getBytes("utf8"))
        Strand[] expected = [new Strand("a".getBytes("utf8")), new Strand("b".getBytes("utf8")), new Strand("c".getBytes("utf8")),
                             new Strand("d".getBytes("utf8")), new Strand("e".getBytes("utf8")), new Strand("f".getBytes("utf8")),
                             new Strand("g".getBytes("utf8")), new Strand("h".getBytes("utf8")), new Strand("i".getBytes("utf8")),
                             new Strand("j".getBytes("utf8")), new Strand("k".getBytes("utf8"))]
        expect:
        results.length == expected.length
        for(int i = 0; i < expected.length; ++i) {
            assert results[i] == expected[i]
        }
    }
    def "split with max num results"() {
        given:
        Strand theStrand = new Strand("ab\r\n\u05D0\r\ncd\r\n".getBytes("utf8"))
        expect:
        expectedResults == theStrand.split("\r\n".getBytes("utf8"), maxNumResults)
        where:
        maxNumResults   |   expectedResults
        1               |   [new Strand("ab\r\n\u05D0\r\ncd\r\n".getBytes("utf8"))]
        2               |   [new Strand("ab".getBytes("utf8")), new Strand("\u05D0\r\ncd\r\n".getBytes("utf8"))]
        3               |   [new Strand("ab".getBytes("utf8")), new Strand("\u05D0".getBytes("utf8")), new Strand("cd\r\n".getBytes("utf8"))]
        4               |   [new Strand("ab".getBytes("utf8")), new Strand("\u05D0".getBytes("utf8")), new Strand("cd".getBytes("utf8"))]
        5               |   [new Strand("ab".getBytes("utf8")), new Strand("\u05D0".getBytes("utf8")), new Strand("cd".getBytes("utf8"))]
    }

    def "Subsequence cutting off a 7-bit ASCII char"() {
        Strand theStrand = new Strand("\u0080ABCD\u0080E".getBytes("utf8"));
        CharSequence subsequence = theStrand.subSequence(1, 6);
        expect:
        5 == subsequence.length()
        expected == subsequence.charAt(index)
        where:
        expected    |   index
        'A'         |   0
        'B'         |   1
        'C'         |   2
        'D'         |   3
        '\u0080'    |   4
    }

    def "Starts with"() {
        Strand theStrand = new Strand("\u0080A\u0080B".getBytes("utf8"))
        expect:
        expected == theStrand.startsWith(prefix.getBytes("utf8"))
        where:
        expected    |   prefix
        false   |   "B"
        false   |   "A"
        true    |   "\u0080"
        true    |   "\u0080A"
    }

    def "Char at with offset"() {
        given: "Characters encoded in UTF8"
        Strand theStrand = new Strand("\u0080A\u0080B\u0080".getBytes("utf8"), false)
        expect:
        expected == theStrand.charAt(index, 2)
        where:
        index   |   expected
        0   |   'A'
        1   |   '\u0080'
        2   |   'B'
        3   |   '\u0080'

    }

    def "Test startsWithStartingFrom"() {
        given: "A strand"
        expect:
        expected == new Strand("ABCD".getBytes("utf8")).startsWithStartingFrom(prefix.getBytes("utf8"), startingAt)
        where:
        prefix  |   startingAt  | expected
        "AB"    |   0           |   true
        "BC"    |   1           |   true
        "AB"    |   1           |   false
        "ABCDE" |   0           |   false
    }

    def "Length with multi-byte chars"() {
        given: "Characters encoded in UTF8"
        expect:
        length == new Strand(str.getBytes("utf8"), false).length()
        where:
        length  |   str
        0   |   ""
        1   |   "A"
        2   |   "AB"
        2   |   "A\u0080"
        3   |   "A\u0080B"
        3   |   "A\u0080\u0080"
        4   |   "A\u0080\u0080B"

    }

    def "Length with start and end parameters"() {
        given:
        Strand theStrand = new Strand("A\u0080B\u0080C".getBytes("utf8"))
        expect:
        3 == theStrand.length(1, 6)

    }
    def "Num bytes per character"() {
        given: "Characters encoded in UTF8"
        expect:
        int value = theChar
        new Strand().utf8CharSize(value) == expectedNumBytes
        where:
        theChar | expectedNumBytes
        0x00 | 1
        0x79 | 1
        0xC0 | 2
        0xCF | 2
        0xE0 | 3
        0xEF | 3
        0xF0 | 4
        0xF7 | 4
        0xF8 | 5
        0xFB | 5
        0xFC | 6
        0xFF | 6
    }

    def "Test decoding utf-8 chars"() {
        given: "An array of bytes encoding characters in UTF8"
        expect:
        byte[] bytes = theChars.getBytes("utf8")
        expectedChar == new Strand().decodeUTF8Char(bytes, 0);
        where:
        theChars | expectedChar
        "A"      | "A"
        "\u007F" | "\u007F"
        "\u0080" | "\u0080"
        "\u07FF" | "\u07FF"
        "\u0800" | "\u0800"
        "\u1000" | "\u1000"
        "\u1FFF" | "\u1FFF"
    }

    def "Test of charAt in a 7-bit ASCII string"() {
        given: "An array of characters that are all 7-bit ASCII"
        String testString = "Hello World!\n";
        Strand str = new Strand(testString.getBytes("utf8"), false)
        expect:
        expected == str.charAt(index)
        where:
        index   |   expected
        0   |   'H'
        1   |   'e'
        2   |   'l'
        3   |   'l'
        4   |   'o'
        5   |   ' '
        6   |   'W'
        7   |   'o'
        8   |   'r'
        9   |   'l'
        10  |   'd'
        11  |   '!'
        12  |   '\n'
    }
    def "Test charAt in a string with mix of single and multi-byte chars"() {
        given: "An array of characters with a mix of single and multi-byte chars"
        String testString = "G\u007FH\u0080I\u007FJ\u0080K\u0800L\u0100M\u1FFFN";
        Strand str = new Strand(testString.getBytes("utf8"), false);
        expect:
        value == str.charAt(index)
        where:
        index   |   value
        0   |   'G'
        1   |   '\u007F'
        2   |   'H'
        3   |   '\u0080'
        4   |   'I'
        5   |   '\u007F'
        6   |   'J'
        7   |   '\u0080'
        8   |   'K'
        9   |   '\u0800'
        10  |   'L'
        11  |   '\u0100'
        12  |   'M'
        13  |   '\u1FFF'
        14  |   'N'
    }

    def "Test split with false start"() {
        given:
        Strand str = new Strand("ab|cd|ef||gh".getBytes("utf8"))
        Strand[] substrands = str.split("||".getBytes("utf8"))
        expect:
        substrands.length == 2
        substrands[0] == new Strand("ab|cd|ef".getBytes("utf8"))
        substrands[1] == new Strand("gh".getBytes("utf8"))
    }

    def "Test splitting into an entry"() {
        given:
        Strand str = new Strand("abc\r\nde\u05D0\r\nfgh\r\ni".getBytes("utf8"))
        Strand[] substrands = new Strand[4]
        expect:
        4 == str.nextSplit("\r\n".getBytes("utf8"), substrands, 0, 0)
        10 == str.nextSplit("\r\n".getBytes("utf8"), substrands, 1, 5)
        15 == str.nextSplit("\r\n".getBytes("utf8"), substrands, 2, 11)
        str.nextSplit("\r\n".getBytes("utf8"), substrands, 3, 16)

        substrands[0] == "abc"
        println "substrands[0]: " + substrands[0]
        substrands[1] == "de\u05D0"
        println "substrands[1]: " + substrands[1]
        substrands[2] == "fgh"
        println "substrands[2]: " + substrands[2]
        substrands[3] == "i"
        println "substrands[3]: " + substrands[3]
    }
}