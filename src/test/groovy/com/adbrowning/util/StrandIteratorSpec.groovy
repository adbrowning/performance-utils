package com.adbrowning.util

import spock.lang.Specification


/**
 * Created by adam on 5/21/14.
 */
class StrandIteratorSpec extends Specification {

    def "nextChar should actually return next char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05d0cd".getBytes("utf8"), 0, 5)
        expect:
        true == it.hasNext()
        'a' == it.nextChar()
        true == it.hasNext()
        'b' == it.nextChar()
        true == it.hasNext()
        '\u05D0' == it.nextChar()
        true == it.hasNext()
        'c' == it.nextChar()
        true == it.hasNext()
        'd' == it.nextChar()
        false == it.hasNext()
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
    def "test starTokenBefore first char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        it.startTokenBeforeMostRecentChar()
        expect:
        'a' == it.nextChar()
        'b' == it.nextChar()
        it.endTokenAfterMostRecentChar() == "ab"
    }

    def "test startTokenAfter first char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        expect:
        'a' == it.nextChar()
        it.startTokenAfterMostRecentChar()
        'b' == it.nextChar()
        it.endTokenAfterMostRecentChar() == 'b'
    }

    def "testStartTokenBefore multi-byte Char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        expect:
        'a' == it.nextChar()
        'b' == it.nextChar()
        '\u05D0' == it.nextChar()
        it.startTokenBeforeMostRecentChar()
        'c' == it.nextChar()
        it.endTokenAfterMostRecentChar() == "\u05D0c"
    }

    def "testStartTokenAfter multi-byte char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        expect:
        'a' == it.nextChar()
        'b' == it.nextChar()
        '\u05D0' == it.nextChar()
        it.startTokenAfterMostRecentChar()
        'c' == it.nextChar()
        'd' == it.nextChar()
        it.endTokenAfterMostRecentChar() == 'cd'
    }

    def "testEndTokenBefore multi-byte char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        expect:
        'a' == it.nextChar()
        it.startTokenBeforeMostRecentChar()
        'b' == it.nextChar()
        '\u05D0' == it.nextChar()
        it.endTokenBeforeMostRecentChar() == 'ab'
    }

    def "testEndTokenAfter multi-byte char"() {
        given:
        StrandIterator it = new StrandIterator("ab\u05D0cd".getBytes("utf8"), 0, 5)
        expect:
        'a' == it.nextChar()
        it.startTokenBeforeMostRecentChar()
        'b' == it.nextChar()
        '\u05D0' == it.nextChar()
        it.endTokenAfterMostRecentChar() == 'ab\u05D0'
    }
}