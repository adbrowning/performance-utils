package com.adbrowning.util

import spock.lang.Specification

/**
 * Created by adam on 5/19/14.
 */
class SubstrandSpec extends Specification {
    def "Test startsWith"() {
        given: "A Substrand"
        expect:
        expected == new Substrand("AABCDE".getBytes("utf8"), 1, 5).startsWith(prefix.getBytes("utf8"))
        where:
        prefix  | expected
        "AB"    | true
        "ABCD"  | true
        "AABCD" | false
        "ABCDE" | false
    }

    def "Length with multi-byte chars"() {
        given: "Characters encoded in UTF8"
        expect:
        byte[] bytes = str.getBytes("utf8")
        length == new Substrand(bytes, 1, bytes.length-1).length()
        where:
        length | str
        1      | "A\u0080B"
        2      | "A\u0080BC"
        0      | "AB"

    }


    def "Test of charAt in a 7-bit ASCII string"() {
        given: "An array of characters that are all 7-bit ASCII"
        String testString = "Hello World!\n";
        Substrand str = new Substrand(testString.getBytes("utf8"), 1, 12)
        expect:
        expected == str.charAt(index)
        where:
        index | expected
        0     | 'e'
        1     | 'l'
        2     | 'l'
        3     | 'o'
        4     | ' '
        5     | 'W'
        6     | 'o'
        7     | 'r'
        8     | 'l'
        9     | 'd'
        10    | '!'
    }

    def "Test charAt in a string with mix of single and multi-byte chars"() {
        given: "An array of characters with a mix of single and multi-byte chars"
        String testString = "G\u007FH\u0080I\u007FJ\u0080K\u0800L\u0100M\u1FFFN";
        Substrand str = new Substrand(testString.getBytes("utf8"), 1, 14);
        expect:
        value == str.charAt(index)
        where:
        index | value
        0     | '\u007F'
        1     | 'H'
        2     | '\u0080'
        3     | 'I'
        4     | '\u007F'
        5     | 'J'
        6     | '\u0080'
        7     | 'K'
        8     | '\u0800'
        9     | 'L'
        10    | '\u0100'
        11    | 'M'
        12    | '\u1FFF'
    }
}