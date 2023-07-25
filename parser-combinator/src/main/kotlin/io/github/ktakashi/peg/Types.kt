@file:JvmName("Types")
package io.github.ktakashi.peg

typealias Parser<T, U> = (l: Sequence<T>) -> Result<T, U>
typealias Binder1<T, U0, U1> = (v: U0) -> Parser<T, U1>
typealias Binder2<T, U0, U1, U2> = (v1: U0, v2: U1) -> Parser<T, U2>
typealias Binder3<T, U0, U1, U2, U3> = (v1: U0, v2: U1, v3: U2) -> Parser<T, U3>
typealias Binder4<T, U0, U1, U2, U3, U4> = (v1: U0, v2: U1, v3: U2, v4: U3) -> Parser<T, U4>


/**
 * Result interface.
 *
 * All the parsing result implement this interface.
 * The [next] property holds next input of the
 * result.
 */
sealed interface Result<T, U> {
    /**
     * Holds the rest of the input.
     * @property next
     */
    val next: Sequence<T>
}

/**
 * The successful result
 *
 * @property value the successful value
 * @property next next input
 */
data class SuccessResult<T, U>(val value: U, override val next: Sequence<T>): Result<T, U>

/**
 * Unsuccessful result.
 *
 * This class notifies the parser that given input does not contain
 * expected input.
 *
 * @property message some descriptive message
 * @property next next input
 */
data class ExpectedResult<T, U>(val message: String, override val next: Sequence<T>): Result<T, U>

/**
 * Unsuccessful result.
 *
 * This class notifies the parser that given input contains
 * unexpected input.
 *
 * @property message some descriptive message
 * @property next next input
 */
data class UnexpectedResult<T, U>(val message: String, override val next: Sequence<T>): Result<T, U>
