package io.github.ktakashi.peg.examples.csv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParserTest {
    @Test
    fun parseCsv() {
        val parser1 = Parser()
        val r1 = parse(parser1, "/test1.csv")
        assertTrue(r1.header.isPresent)
        assertEquals(listOf("Field1", "Field2", "Field3"), r1.header.get())
        assertEquals(listOf(listOf("aaa", "bbb", "ccc"), listOf("zzz", "yyy", "xxx")), r1.record)

        val parser2 = Parser(parseHeader = false)
        val r1_1 = parse(parser2, "/test1.csv")
        assertTrue(r1_1.header.isEmpty)
        assertEquals(listOf(listOf("Field1", "Field2", "Field3"), listOf("aaa", "bbb", "ccc"), listOf("zzz", "yyy", "xxx")), r1_1.record)

        val r2 = parse(parser2, "/test2.csv")
        assertEquals(listOf(listOf("aaa", "bbb", "ccc"), listOf("zzz", "yyy", "xxx")), r2.record)

        val r3 = parse(parser2, "/test3.csv")
        assertEquals(listOf(listOf("aaa", "b\r\nbb", "ccc"), listOf("aaa", "b\"bb", "ccc"), listOf("zzz", "yyy", "xxx")), r3.record)
    }

    private fun parse(parser: Parser, file: String) =
        ParserTest::class.java.getResourceAsStream(file)?.use {
            parser.parse(it)
        } ?: throw Exception("something is wrong")

}
