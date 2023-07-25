package io.github.ktakashi.peg.examples.csv

import io.github.ktakashi.peg.SuccessResult
import io.github.ktakashi.peg.asCharSequence
import io.github.ktakashi.peg.bind
import io.github.ktakashi.peg.debug
import io.github.ktakashi.peg.eq
import io.github.ktakashi.peg.expected
import io.github.ktakashi.peg.many
import io.github.ktakashi.peg.optional
import io.github.ktakashi.peg.or
import io.github.ktakashi.peg.result
import io.github.ktakashi.peg.satisfy
import io.github.ktakashi.peg.seq
import java.io.InputStream
import java.io.Reader
import java.text.ParseException
import java.util.Optional

// ABNF from https://datatracker.ietf.org/doc/html/rfc4180
class Parser(separator: Char = ',', parseHeader: Boolean = true) {
    private val cr = eq('\r')
    private val lf = eq('\n')
    private val crlf = or(seq(cr, lf), lf) // accept \n as EOL
    private val comma = eq(separator)
    private val dquote = eq('"')
    private val textdata = satisfy { c: Char ->
        c != '"' && c != ',' && Charsets.US_ASCII.newEncoder().canEncode(c) && !Character.isISOControl(c)
    }
    private val escaped = bind(seq(dquote, many(or(textdata, comma, cr, lf, seq(dquote, dquote, result('"')))))) { r ->
        seq(dquote, result(r.joinToString("")))
    }
    private val nonEscaped = bind(many(textdata)) { r -> result(r.joinToString("")) }
    private val field = or(escaped, nonEscaped)
    private val name = field
    private val header = bind(name, many(seq(comma, name))) { n, ns -> result(listOf(n) + ns)}
    private val record = bind(field, many(seq(comma, field))) { f, fs -> result(listOf(f) + fs) }
    private val headerLine = if (parseHeader) {
        optional(bind(header) { h -> seq(crlf, result(h)) })
    } else {
        result(Optional.empty())
    }

    private val file = bind(headerLine, record, many(seq(crlf, record))) { hs, r, rs ->
        seq(debug(optional(crlf)), result(CsvFile(hs, listOf(r) + rs.dropLastWhile { it.size == 1 && it[0] == "" })))
    }

    fun parse(csvString: String) = parse(csvString.asSequence())
    fun parse(inputStream: InputStream) = parse(inputStream.asCharSequence())
    fun parse(reader: Reader) = parse(reader.asCharSequence())
    fun parse(input: Sequence<Char>) =
        when (val r = file(input)) {
            is SuccessResult -> r.value
            else -> throw ParseException("Failed to parse CSV", 0);
        }
}
