# Module parser-combinator

## Yet Another Parser Combinator for Kotlin

This module provides parser combinators for Kotlin. 
The functions allow users to write parsers in BNF style.

The design is highly inspired `(peg)` library of [Sagittarius Scheme](https://bitbucket.org/ktakashi/sagittarius-scheme)

### Example code

Below example shows how to write CSV parser. The ABNF definition
is taken from [RFC 4180](https://datatracker.ietf.org/doc/html/rfc4180).

```abnf
file = [header CRLF] record *(CRLF record) [CRLF]
header = name *(COMMA name)
record = field *(COMMA field)
name = field
field = (escaped / non-escaped)
escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE
non-escaped = *TEXTDATA
COMMA = %x2C
CR = %x0D
DQUOTE =  %x22
LF = %x0A
CRLF = CR LF
TEXTDATA =  %x20-21 / %x23-2B / %x2D-7E
```

```kotlin
val cr = eq('\r')        // CR
val lf = eq('\n')        // LF
val dquote = eq('"')     // DQUOTE
val comma = eq(',')      // COMMA
val crlf = seq(cr, lf)   // CRLF
val textSet = "~!@#$%^&*()_+-=[]\\;<>/.?abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ"
val textdata = contains(textSet.toList()) // TEXTDATA
val escaped = bind(dquote, bind(many(or(textdata, comma, cr, lf, seq(dquote, dquote)))), dquote { _, v, _ ->
    result(v)
})
val nonEscaped = many(textdata)
val field = bind(or(escaped, nonEscaped)) { v -> result(v.joinToString("")) }
val name = field
val record = bind(field, many(seq(comma, field))) { r, rs -> result(listOf(r) + rs) }
val header = bind(name, many(seq(comma, name))) { h, hs -> result(listOf(h) + hs) }
val file = bind(optional(bind(header, crlf) { h, _ -> 
    result(h)
})) { h ->
    bind(record, many(seq(crlf, record)), crlf) { r, rs, _ ->
        result(h to listOf(r) + rs)
    }
}
```
