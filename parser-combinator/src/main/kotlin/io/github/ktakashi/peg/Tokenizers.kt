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
    val next = lazy { asLazyNode(iterator) }

    override fun iterator(): Iterator<T> = LazyNodeIterator(this)

    private class LazyNodeIterator<T>(private var node: Node<T>): Iterator<T> {
        override fun hasNext(): Boolean = node is LazyNode

        override fun next(): T {
            return when (node) {
                is LazyNode<T> -> {
                    val lazyNode = node as LazyNode<T>
                    val r = lazyNode.value
                    node = lazyNode.next.value
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


fun Reader.asCharSequence(): Sequence<Char> = asLazyNode(this.buffered().let {
    generateSequence { it.read().toChar() }
})
fun InputStream.asCharSequence() = InputStreamReader(this.buffered()).asCharSequence()
