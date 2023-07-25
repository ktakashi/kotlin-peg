@file:JvmName("ParserCombinators")
package io.github.ktakashi.peg

import java.util.Optional

/**
 * End-of-sequence (EOS) object.
 *
 * This object is only returned when [eos] parser is used.
 */
object EosObject // special value, end-of-sequence object

/**
 * Returns [value] with [SuccessResult]
 *
 * @param value The value to be returned
 */
fun <T, U> result(value: U) = { next: Sequence<T> -> SuccessResult(value, next) }
fun <T, U> expected(message: String) = { next: Sequence<T> -> ExpectedResult<T, U>(message, next) }

/**
 * A parser which matches to any input unless it's EOS.
 *
 * @param l Sequence
 * @return The result
 */
fun <T> any(l: Sequence<T>) = if (l.any()) SuccessResult(l.first(), l.drop(1)) else UnexpectedResult("EOS", l)
// end of sequence
/**
 * A parser which matches to EOS.
 *
 * @param l Sequence
 * @return The result
 */
fun <T> eos(l: Sequence<T>) = if (l.any()) ExpectedResult("EOS", l) else SuccessResult(EosObject, l)

/**
 * Makes a parser which validates the input against [pred].
 *
 * @param pred A predicate
 * @return A parser
 */
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

/**
 * Returns a parser which accepts input if it's equal to [value].
 *
 * This method is equivalent to
 * ```kotlin
 * val value = /* ... */;
 * satisfy { v -> v == value }
 * ```
 *
 * @param value The target value
 * @return A parser
 */
fun <T> eq(value: T) = satisfy { v: T -> v == value }
/**
 * Returns a parser which accepts input if it isn't equal to [value].
 *
 * This method is equivalent to
 * ```kotlin
 * val value = /* ... */
 * satisfy { v -> v != value }
 * ```
 *
 * @param value The target value
 * @return A parser
 */
fun <T> neq(value: T) = satisfy { v: T -> v != value }

/**
 * Returns a parser which accepts if the input is in the [values]
 *
 * @param values Expected values
 * @return A parser
 */
fun <T> contains(vararg values: T) = satisfy { v: T -> values.contains(v) }
fun <T> contains(values: Collection<T>) = satisfy { v: T -> values.contains(v) }

/**
 * Makes a parser which tries to match multiple time to [parser].
 *
 * Examples:
 * ```kotlin
 * val r = many(::any)("aaa".asSequence())
 * r.value == listOf("a", "a", "a")
 * ```
 * [atLeast] and [atMost] control the number of matches.
 *
 * @param parser The target parser to match
 * @param atLeast Minimum number of match, default 0
 * @param atMost Maximum number of match, default [Int.MAX_VALUE]
 */
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

/**
 * Sequential match.
 *
 * @param p0 A parser
 * @return A parser
 */
fun <T, U> seq(p0: Parser<T, U>) = p0

/**
 * Sequential match.
 *
 * The parser returns success when both [p0] and [p1] match.
 *
 * @param p0 first parser
 * @param p1 second parser
 * @return A parser
 */
fun <T, U0, U1> seq(p0: Parser<T, U0>, p1: Parser<T, U1>) = { l: Sequence<T> ->
    when (val r = seq(p0)(l)) {
        is SuccessResult<T, U0> -> p1(r.next)
        else -> ExpectedResult("$p0 is expected", l)
    }
}

/**
 * Sequential match
 *
 * The parser returns success when [p0], [p1] and [p2] match.
 *
 * @param p0 first parser
 * @param p1 second parser
 * @param p2 third parser
 * @return A parser
 */
fun <T, U0, U1, U2> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1)(l)) {
        is SuccessResult<T, U1> -> p2(r.next)
        else -> ExpectedResult("$p1 is expected", l)
    }
}

/**
 * Sequential match
 *
 * The parser returns success when [p0], [p1], [p2] and [p3] match.
 *
 * @param p0 first parser
 * @param p1 second parser
 * @param p2 third parser
 * @param p3 forth parser
 * @return A parser
 */
fun <T, U0, U1, U2, U3> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2)(l)) {
        is SuccessResult<T, U2> -> p3(r.next)
        else -> ExpectedResult("$p2 is expected", l)
    }
}

/**
 * Sequential match
 *
 * The parser returns success when [p0], [p1], [p2], [p3] and [p4] match.
 *
 * @param p0 first parser
 * @param p1 second parser
 * @param p2 third parser
 * @param p3 forth parser
 * @param p4 fifth parser
 * @return A parser
 */
fun <T, U0, U1, U2, U3, U4> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>, p4: Parser<T, U4>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2, p3)(l)) {
        is SuccessResult<T, U3> -> p4(r.next)
        else -> ExpectedResult("$p3 is expected", l)
    }
}

/**
 * Sequential match
 *
 * The parser returns success when [p0], [p1], [p2], [p3], [p4] and [p5] match.
 *
 * @param p0 first parser
 * @param p1 second parser
 * @param p2 third parser
 * @param p3 forth parser
 * @param p4 fifth parser
 * @param p5 sixth parser
 * @return A parser
 */
fun <T, U0, U1, U2, U3, U4, U5> seq(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>, p4: Parser<T, U4>, p5: Parser<T, U5>) = { l: Sequence<T> ->
    when (val r = seq(p0, p1, p2, p3, p4)(l)) {
        is SuccessResult<T, U4> -> p5(r.next)
        else -> ExpectedResult("$p4 is expected", l)
    }
}

/**
 * Returns a conditional parser.
 *
 * [or] accepts variable length parsers, all the parsers must
 * accept the same input type and return the same result type.
 *
 * @param pn Variable length parsers
 * @return A parser
 */
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

/**
 * Bind the result of [parser] and applies to the [receiver].
 *
 * This parser is useful when the subsequent parsers require
 * the result of the precedent parser.
 *
 * Example:
 * ```kotlin
 * bind(::any) { v1 -> seq(eq(v1)) }
 * ```
 * @param parser A parser
 * @param receiver A function takes the result of [parser] and returna a parser
 * @return A parser
 */
fun <T, U0, U1> bind(parser: Parser<T, U0>, receiver: Binder1<T, U0, U1>) = { l: Sequence<T> ->
    when (val r = parser(l)) {
        is SuccessResult<T, U0> -> receiver(r.value)(r.next)
        else -> ExpectedResult("$parser is expected", l)
    }
}

/**
 * Binds the result of [p0] and [p1] and applies to the [receiver]
 *
 * Example:
 * ```kotlin
 * bind(::any, ::any) { v1, v2 -> result(listOf(v1, v2) }
 * ```
 */
fun <T, U0, U1, U2> bind(p0: Parser<T, U0>, p1: Parser<T, U1>, receiver: Binder2<T, U0, U1, U2>) = { l: Sequence<T> ->
    when (val r0 = p0(l)) {
        is SuccessResult<T, U0> -> when (val r1 = p1(r0.next)) {
            is SuccessResult<T, U1> -> receiver(r0.value, r1.value)(r1.next)
            else -> ExpectedResult("$p1 is expected", l)
        }
        else -> ExpectedResult("$p0 is expected", l)
    }
}

/**
 * Binds the result of [p0], [p1] and [p2] and applies to the [receiver]
 */
fun <T, U0, U1, U2, U3> bind(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, receiver: Binder3<T, U0, U1, U2, U3>) = { l: Sequence<T> ->
    when (val r0 = p0(l)) {
        is SuccessResult<T, U0> -> when (val r1 = p1(r0.next)) {
            is SuccessResult<T, U1> -> when (val r2 = p2(r1.next)) {
                is SuccessResult<T, U2> -> receiver(r0.value, r1.value, r2.value)(r2.next)
                else -> ExpectedResult("$p2 is expected", l)
            }
            else -> ExpectedResult("$p1 is expected", l)
        }
        else -> ExpectedResult("$p0 is expected", l)
    }
}

/**
 * Binds the result of [p0], [p1], [p2] and [p3] and applies to the [receiver]
 */
fun <T, U0, U1, U2, U3, U4> bind(p0: Parser<T, U0>, p1: Parser<T, U1>, p2: Parser<T, U2>, p3: Parser<T, U3>, receiver: Binder4<T, U0, U1, U2, U3, U4>) = { l: Sequence<T> ->
    when (val r0 = p0(l)) {
        is SuccessResult<T, U0> -> when (val r1 = p1(r0.next)) {
            is SuccessResult<T, U1> -> when (val r2 = p2(r1.next)) {
                is SuccessResult<T, U2> -> when (val r3 = p3(r2.next)) {
                    is SuccessResult<T, U3> -> receiver(r0.value, r1.value, r2.value, r3.value)(r3.next)
                    else -> ExpectedResult("$p3 is expected", l)
                }
                else -> ExpectedResult("$p2 is expected", l)
            }
            else -> ExpectedResult("$p1 is expected", l)
        }
        else -> ExpectedResult("$p0 is expected", l)
    }
}

/**
 * Optional.
 *
 * The result of this parser returns [java.util.Optional].
 *
 * @param parser A parser
 * @return A parser
 */
fun <T, U : Any> optional(parser: Parser<T, U>) =
    bind(many(parser, 0, 1)) { v ->
        if (v.isEmpty()) {
            result(Optional.empty())
        } else {
            result(Optional.of(v[0]))
        }
    }

/**
 * Exact number match.
 *
 * The returning parser results successful, when [parser] matches exactly [count] times.
 *
 * This parser is equivalent to below
 * ```kotlin
 * many(parser, atLeast = count, atMost = count)
 * ```
 * @param parser A parser
 * @param count Number of times to match
 * @return A parser
 */
fun <T, U> repeat(parser: Parser<T, U>, count: Int) = many(parser, atLeast = count, atMost = count)

/**
 * Peeks the next input if it matches.
 *
 * The returning parser doesn't consume the input.
 *
 * @param parser A parser
 * @return A parser
 */
fun <T, U> peek(parser: Parser<T, U>) = { l: Sequence<T> ->
    when (val r = parser(l)) {
        is SuccessResult<T, U> -> SuccessResult(r.value, l)
        else -> r
    }
}

/**
 * Peeks the next input if it doesn't match.
 *
 * The returning parser doesn't consume the input.
 *
 * The successful result's value contains the first element of the input.
 *
 * @param parser A parser
 * @return A parser
 */
fun <T, U> not(parser: Parser<T, U>) = peek(parser).let { p ->
    { l: Sequence<T> ->
        when (val r = p(l)) {
            is SuccessResult<T, U> -> UnexpectedResult("Not $parser is expected", r.next)
            else -> SuccessResult(l.first(), l)
        }
    }
}

/**
 * Debugging parser
 *
 * The returning parser calls [consumer] to any result of the [parser]
 *
 * @param parser A parser
 * @param consumer A consumer to check the result, default [println]
 * @return A parser
 */
fun <T, U> debug(parser: Parser<T, U>, consumer: (Result<T, U>) -> Unit = ::println) = { l: Sequence<T> ->
    val r = parser(l)
    consumer(r)
    r
}
