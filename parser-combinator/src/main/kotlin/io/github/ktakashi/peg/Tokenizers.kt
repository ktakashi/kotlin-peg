@file:JvmName("Tokenizers")
package io.github.ktakashi.peg

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

fun Reader.asCharSequence() = generateSequence { this.read().toChar() }
fun InputStream.asCharSequence() = InputStreamReader(this).asCharSequence()
