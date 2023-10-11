@file:JvmName("Tokenizers")
package io.github.ktakashi.peg

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

private sealed interface Node<T>: Sequence<T>
private data object NilNode: Node<Any> {
    override fun iterator(): Iterator<Any> = emptySequence<Any>().iterator()
}
private class LazyNode<T>(val value: T, private val iterator: Iterator<T>): Node<T> {
    val next by lazy { asLazyNode(iterator) }

    override fun iterator(): Iterator<T> = LazyNodeIterator(this)

    private class LazyNodeIterator<T>(private var node: Node<T>): Iterator<T> {
        override fun hasNext(): Boolean = node is LazyNode

        override fun next(): T {
            return when (node) {
                is LazyNode<T> -> {
                    val lazyNode = node as LazyNode<T>
                    val r = lazyNode.value
                    node = lazyNode.next
                    r
                }
                else -> throw NoSuchElementException("No next")
            }
        }
    }
}

private fun <T> asLazyNode(iterator: Iterator<T>) =
    if (iterator.hasNext()) {
        LazyNode(iterator.next(), iterator)
    } else {
        NilNode as Node<T>
    }
private fun <T> asLazyNode(sequence: Sequence<T>) = asLazyNode(sequence.iterator())

/**
 * Converts a [Reader] to sequence.
 *
 * The returning sequence can be reused multiple times.
 * (Not [constrainOnce])
 */
fun Reader.asCharSequence(): Sequence<Char> = asLazyNode(this.buffered().let {
    generateSequence { it.read().toChar() }
})

/**
 * Converts an [InputStream] to sequence.
 *
 * The returning sequence can be reused multiple times.
 * (Not [constrainOnce])
 */
fun InputStream.asCharSequence() = InputStreamReader(this.buffered()).asCharSequence()
