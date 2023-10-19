package io.github.ktakashi.peg

import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExampleCsvTest {
    data class CsvFile(val header: Optional<List<String>>, val record: List<List<String>>)
    @Test
    fun csvParserTest() {
        // ANBF from https://datatracker.ietf.org/doc/html/rfc4180
        val cr = eq('\r')
        val lf = eq('\n')
        val crlf = token('\r', '\n') { "\r\n" }
        val comma = eq(',')
        val dquote = eq('"')
        val textSet = "~!@#$%^&*()_+-=[]\\;<>/.?abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val textdata = contains(textSet.toList())
        val escaped = bind(dquote, many(or(textdata, comma, cr, lf, seq(dquote, dquote, result('"')))), dquote) { _, v, _ ->
            result(v)
        }
        val nonEscaped = many(textdata)
        val field = bind(or(escaped, nonEscaped)) { v -> result(v.joinToString("")) }
        val name = field
        val header = bind(name, many(seq(comma, name))) { n, ns -> result(listOf(n) + ns) }
        val record = bind(field, many(seq(comma, field))) { f, fs -> result(listOf(f) + fs) }
        val file = bind(optional(bind(header, crlf) { h, _ ->
            result(h)
        })) { hs ->
            bind(record, many(seq(crlf, record)), optional(crlf)) { r, rs, _ ->
                result(CsvFile(hs, listOf(r) + rs))
            }
        }

        val csvData = "\"aaa\",\"bbb\",\"ccc\"\r\nzzz,yyy,xxx"
        val r = file(csvData.asSequence())
        assertTrue(r is SuccessResult)
        assertEquals(CsvFile(Optional.of(listOf("aaa", "bbb", "ccc")), listOf(listOf("zzz", "yyy", "xxx"))), r.value)
    }

}
