package io.github.ktakashi.peg

import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

class ParserCombinatorsTest {
    @Test
    fun result() {
        val sequence = "ignore this".asSequence()
        val r = result<Char, Char>('a')(sequence)
        assertEquals('a', r.value)
        assertEquals(sequence, r.next)
    }

    @Test
    fun any() {
        val sequence = "any".asSequence()
        val r = any(sequence)
        assertTrue(r is SuccessResult<Char, Char>)
        assertEquals('a', r.value)
        assertEquals(listOf('n', 'y'), r.next.toList())

        val eos = "".asSequence()
        val r1 = any(eos)
        assertTrue(r1 is UnexpectedResult<Char, Char>)
    }

    @Test
    fun eos() {
        val sequence = "not eos".asSequence()
        val r = eos(sequence)
        assertTrue(r is ExpectedResult<Char, EosObject>)

        val eos = "".asSequence()
        val r1 = eos(eos)
        assertTrue(r1 is SuccessResult<Char, EosObject>)
    }

    @Test
    fun satisfy() {
        val sequence = "satisfy".asSequence()
        val r = satisfy { c: Char -> c == 's' }(sequence)
        assertTrue(r is SuccessResult<Char, Char>)
        assertEquals('s', r.value)
        assertEquals(sequence.drop(1).toList(), r.next.toList())

        val r1 = satisfy { c: Char -> c == 'n' }(sequence)
        assertTrue(r1 is ExpectedResult<Char, Char>)

        val r2 = eq('s')(sequence)
        assertTrue(r2 is SuccessResult<Char, Char>)
        assertEquals('s', r2.value)

        val r3 = neq('!')(sequence)
        assertTrue(r3 is SuccessResult<Char, Char>)
        assertEquals('s', r3.value)
    }

    @Test
    fun many() {
        val sequence = "a".repeat(5).asSequence()
        val r = many(eq('a'))(sequence)

        assertTrue(r is SuccessResult<Char, List<Char>>)
        assertEquals(sequence.toList(), r.value)

        val r1 = optional(eq('a'))(sequence)
        assertTrue(r1 is SuccessResult<Char, Optional<Char>>)
        assertEquals(Optional.of('a'), r1.value)
    }

    @Test
    fun token() {
        val sequence = "token".asSequence()
        val r = token(sequence)("token is here".asSequence())
        assertTrue(r is SuccessResult)
        assertEquals(" is here", r.next.joinToString(""))
    }

    @Test
    fun token2() {
        val sequence = "token".asSequence()
        val r = token(sequence) { "token" }("token is here".asSequence())
        assertTrue(r is SuccessResult)
        assertEquals("token", r.value)
        assertEquals(" is here", r.next.joinToString(""))
    }
}
