package com.dsobko.gcp.utils

import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDateTime.toDate(timestampPattern: String): LocalDate {
	val dateFormatter = DateTimeFormatter.ofPattern(timestampPattern)

	return LocalDate.parse(this.toString().take(timestampPattern.length), dateFormatter)
}