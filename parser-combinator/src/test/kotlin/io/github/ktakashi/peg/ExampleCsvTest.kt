package io.github.ktakashi.peg

import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExampleCsvTest {
    data class CsvFile(val header: Optional<List<String>>, val record: List<List<String>>)
    @Test
    fun csvParserTest() {
        fun <T, U> debugPrint(r: Result<T, U>) {
            println(r.next.toList())
            println(r)
        }
        // ANBF from https://datatracker.ietf.org/doc/html/rfc4180
        val cr = eq('\r')
        val lf = eq('\n')
        val crlf = seq(cr, lf)
        val comma = eq(',')
        val dquote = eq('"')
        val textdata = satisfy { c: Char ->
            c != '"' && c != ',' && Charsets.US_ASCII.newEncoder().canEncode(c) && !Character.isISOControl(c)
        }
        val escaped = bind(seq(dquote, many(or(textdata, comma, cr, lf, seq(dquote, dquote, result('"')))))) { r ->
            seq(dquote, result(r.joinToString("")))
        }
        val nonEscaped = bind(many(textdata)) { r -> result(r.joinToString("")) }
        val field = or(escaped, nonEscaped)
        val name = field
        val header = bind(name) { n ->
            bind(many(seq(comma, name))) { ns ->
                result(listOf(n) + ns)
            }
        }
        val record = bind(field) { f ->
            bind(many(seq(comma, field))) { fs ->
                result(listOf(f) + fs)
            }
        }
        val file = bind(optional(bind(debug(header)) { h ->
            seq(crlf, result(h))
        })) { hs ->
            bind(record) { r ->
                bind(many(seq(crlf, record))) { rs ->
                    seq(optional(crlf), result(CsvFile(hs, listOf(r) + rs)))
                }
            }
        }

        val csvData = "\"aaa\",\"bbb\",\"ccc\"\r\nzzz,yyy,xxx"
        val r = file(csvData.asSequence())
        assertTrue(r is SuccessResult)
        assertEquals(CsvFile(Optional.of(listOf("aaa", "bbb", "ccc")), listOf(listOf("zzz", "yyy", "xxx"))), r.value)
    }

}
