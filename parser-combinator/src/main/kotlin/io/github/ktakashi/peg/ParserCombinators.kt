@file:JvmName("ParserCombinators")
package io.github.ktakashi.peg

import java.util.Optional

sealed interface Result<T, U> {
    val next: Sequence<T>
}
data class SuccessResult<T, U>(val value: U, override val next: Sequence<T>): Result<T, U>
data class ExpectedResult<T, U>(val message: String, override val next: Sequence<T>): Result<T, U>
data class UnexpectedResult<T, U>(val message: String, override val next: Sequence<T>): Result<T, U>

object EosObject // special value, end-of-sequence object

typealias Parser<T, U> = (l: Sequence<T>) -> Result<T, U>
typealias Binder<T, U0, U1> = (v: U0) -> Parser<T, U1>

fun <T, U> result(v: U) = { next: Sequence<T> -> SuccessResult(v, next) }
fun <T, U> expected(message: String) = { next: Sequence<T> -> ExpectedResult<T, U>(message, next) }
fun <T> any(l: Sequence<T>) = if (l.any()) SuccessResult(l.first(), l.drop(1)) else UnexpectedResult("EOS", l)
// end of sequence
fun <T> eos(l: Sequence<T>) = if (l.any()) ExpectedResult("EOS", l) else SuccessResult(EosObject, l)

fun <T> satisfy(pred: (value: T) -> Boolean) = { l: Sequence<T> ->
    if (l.any()) {
        l.first().let {
            if (pred(it)) {
                SuccessResult(it, l.drop(1))
            } else {
                ExpectedResult("Satisfying $pred", l)
            }
        }
    } else {
        UnexpectedResult("EOS", l)
    }
}

fun <T> eq(value: T) = satisfy { v: T -> v == value }
fun <T> neq(value: T) = satisfy { v: T -> v != value }

fun <T, U> many(parser: Parser<T, U>, atLeast: Int = 0, atMost: Int = Int.MAX_VALUE) =  { ol: Sequence<T> ->
    tailrec fun loop(result: MutableList<U>, l: Sequence<T>, count: Int): Result<T, List<U>> {
        return if (count >= atMost) {
            SuccessResult(result, l)
        } else {
            val r = parser(l)
            when {
                r is SuccessResult<T, U> -> {
                    result.add(r.value)
                    loop(result, r.next, count + 1)
                }
                atLeast <= count -> SuccessResult(result, l)
                else -> ExpectedResult("At least $atLeast of $parser", ol)
            }
        }
    }
    loop(mutableListOf(), ol, 0)
}

fun <T, U> seq(p0: Parser<T, U>) = { l: Sequence<T> -> p0(l) }
fun <T, U0, U1> seq(p0: Parser<T, U0>, p1: Parser<T, U1>) = { l: Sequence<T> ->
    when (val r = seq(p0)(l)) {
        is SuccessResult<T, U0> -> p1(r.next)
        else -> ExpectedResult("$p0 is expected", l)
    }
}
fun <T, U0, U1, U2> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1)(l)) {
        is SuccessResult<T, U1> -> p2(r.next)
        else -> ExpectedResult("$p1 is expected", l)
    }
}
fun <T, U0, U1, U2, U3> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2)(l)) {
        is SuccessResult<T, U2> -> p3(r.next)
        else -> ExpectedResult("$p2 is expected", l)
    }
}
fun <T, U0, U1, U2, U3, U4> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>, p4: Parser<T, U4>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2, p3)(l)) {
        is SuccessResult<T, U3> -> p4(r.next)
        else -> ExpectedResult("$p3 is expected", l)
    }
}
fun <T, U0, U1, U2, U3, U4, U5> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>, p4: Parser<T, U4>, p5: Parser<T, U5>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2, p3, p4)(l)) {
        is SuccessResult<T, U4> -> p5(r.next)
        else -> ExpectedResult("$p4 is expected", l)
    }
}

fun <T, U> or(vararg pn: Parser<T, U>) = { l: Sequence<T> ->
    tailrec fun find(i: Int): Result<T, U> {
        if (i < pn.size) {
            return when(val r = pn[i](l)) {
                is SuccessResult<T, U> -> r
                else -> find(i + 1)
            }
        }
        return ExpectedResult("One of $pn", l)
    }
    find(0)
}

fun <T, U0, U1> bind(p: Parser<T, U0>, f0: Binder<T, U0, U1>) = { l: Sequence<T> ->
    when (val r = p(l)) {
        is SuccessResult<T, U0> -> f0(r.value)(r.next)
        else -> ExpectedResult("$p is expected", l)
    }
}
fun <T, U0, U1, U2> bind(p: Parser<T, U0>, f0: Binder<T, U0, U1>, f1: Binder<T, U1, U2>) = { l: Sequence<T> ->
    when (val r = bind(p, f0)(l)) {
        is SuccessResult<T, U1> -> f1(r.value)(r.next)
        else -> ExpectedResult("$f0 is expected", l)
    }
}

fun <T, U : Any> optional(parser: Parser<T, U>) =
    bind(many(parser, 0, 1)) { v ->
        if (v.isEmpty()) {
            result(Optional.empty())
        } else {
            result(Optional.of(v[0]))
        }
    }

fun <T, U> debug(parser: Parser<T, U>, consumer: (Result<T, U>) -> Unit = ::println) = { l: Sequence<T> ->
    val r = parser(l)
    consumer(r)
    r
}
