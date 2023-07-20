package io.github.ktakashi.peg.examples.csv

import java.util.Optional

data class CsvFile(val header: Optional<List<String>>, val record: List<List<String>>)
