package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val shortMonths = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    /**
     * Formats an article creation or publish date into full real date representation:
     * e.g. "Oct 12, 2026, 11:12 AM" without truncating the year.
     * Consistently applied across all cards and detail screens.
     */
    fun formatArticleDate(raw: String?): String {
        if (raw.isNullOrBlank()) {
            val now = Calendar.getInstance()
            return formatComponents(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE)
            )
        }

        val trimmed = raw.trim()

        // Handle legacy or relative strings by resolving to real calendar dates
        if (trimmed.startsWith("Added today", ignoreCase = true)) {
            val now = Calendar.getInstance()
            val timePart = parseTimeFromString(trimmed)
            val hour = timePart?.first ?: now.get(Calendar.HOUR_OF_DAY)
            val minute = timePart?.second ?: now.get(Calendar.MINUTE)
            return formatComponents(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                hour,
                minute
            )
        }
        if (trimmed.startsWith("Yesterday", ignoreCase = true)) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
            val timePart = parseTimeFromString(trimmed)
            val hour = timePart?.first ?: cal.get(Calendar.HOUR_OF_DAY)
            val minute = timePart?.second ?: cal.get(Calendar.MINUTE)
            return formatComponents(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                hour,
                minute
            )
        }

        val parsed = parseDateComponents(trimmed)
        if (parsed != null) {
            return formatComponents(
                parsed.year,
                parsed.month,
                parsed.day,
                parsed.hour,
                parsed.minute
            )
        }

        return trimmed
    }

    data class ParsedDateTime(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int? = null,
        val minute: Int? = null
    )

    fun parseDateComponents(raw: String): ParsedDateTime? {
        // 1. Try ISO 8601: 2026-10-14T12:05:29Z or 2026-10-14 12:05:29 or 2026-10-14
        if (raw.contains("-")) {
            val datePart = raw.substringBefore("T").substringBefore(" ").trim()
            val parts = datePart.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: return null
                val month = parts[1].trimStart('0').toIntOrNull() ?: return null
                val day = parts[2].trimStart('0').toIntOrNull() ?: return null

                val timeStr = if (raw.contains("T")) {
                    raw.substringAfter("T").trimEnd('Z')
                } else if (raw.contains(" ")) {
                    raw.substringAfter(" ").trim()
                } else {
                    null
                }

                val time = timeStr?.let { parseTimeFromString(it) }
                return ParsedDateTime(year, month, day, time?.first, time?.second)
            }
        }

        // 2. Try text format: "October 14, 2026 11:35" or "Oct 12, 2026, 11:12 AM" or "October 14, 2026"
        val regex = Regex("""^([A-Za-z]+)\s+(\d{1,2}),?\s+(\d{4})(?:,?\s+(\d{1,2}):(\d{2})(?:\s*([AaPp][Mm]))?)?""")
        val match = regex.find(raw)
        if (match != null) {
            val monthStr = match.groupValues[1]
            val day = match.groupValues[2].toIntOrNull() ?: return null
            val year = match.groupValues[3].toIntOrNull() ?: return null
            val month = getMonthNumber(monthStr) ?: return null

            val hourRaw = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.toIntOrNull()
            val minuteRaw = match.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }?.toIntOrNull()
            val amPm = match.groupValues.getOrNull(6)?.takeIf { it.isNotBlank() }

            val hour = if (hourRaw != null && amPm != null) {
                when {
                    amPm.equals("PM", ignoreCase = true) && hourRaw < 12 -> hourRaw + 12
                    amPm.equals("AM", ignoreCase = true) && hourRaw == 12 -> 0
                    else -> hourRaw
                }
            } else {
                hourRaw
            }

            return ParsedDateTime(year, month, day, hour, minuteRaw)
        }

        // 3. Try day first format: "12 Oct 2026 11:12"
        val regexDayFirst = Regex("""^(\d{1,2})\s+([A-Za-z]+),?\s+(\d{4})(?:,?\s+(\d{1,2}):(\d{2})(?:\s*([AaPp][Mm]))?)?""")
        val matchDayFirst = regexDayFirst.find(raw)
        if (matchDayFirst != null) {
            val day = matchDayFirst.groupValues[1].toIntOrNull() ?: return null
            val monthStr = matchDayFirst.groupValues[2]
            val year = matchDayFirst.groupValues[3].toIntOrNull() ?: return null
            val month = getMonthNumber(monthStr) ?: return null

            val hourRaw = matchDayFirst.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.toIntOrNull()
            val minuteRaw = matchDayFirst.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }?.toIntOrNull()
            val amPm = matchDayFirst.groupValues.getOrNull(6)?.takeIf { it.isNotBlank() }

            val hour = if (hourRaw != null && amPm != null) {
                when {
                    amPm.equals("PM", ignoreCase = true) && hourRaw < 12 -> hourRaw + 12
                    amPm.equals("AM", ignoreCase = true) && hourRaw == 12 -> 0
                    else -> hourRaw
                }
            } else {
                hourRaw
            }

            return ParsedDateTime(year, month, day, hour, minuteRaw)
        }

        return null
    }

    private fun parseTimeFromString(str: String): Pair<Int, Int>? {
        val regex = Regex("""(\d{1,2}):(\d{2})(?:\s*([AaPp][Mm]))?""")
        val match = regex.find(str) ?: return null
        val hourRaw = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        val amPm = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }

        val hour = if (amPm != null) {
            when {
                amPm.equals("PM", ignoreCase = true) && hourRaw < 12 -> hourRaw + 12
                amPm.equals("AM", ignoreCase = true) && hourRaw == 12 -> 0
                else -> hourRaw
            }
        } else {
            hourRaw
        }

        return Pair(hour, minute)
    }

    fun formatComponents(year: Int, month: Int, day: Int, hour: Int?, minute: Int?): String {
        val monthIdx = (month - 1).coerceIn(0, 11)
        val monthStr = shortMonths[monthIdx]
        return if (hour != null && minute != null) {
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when (val h = hour % 12) {
                0 -> 12
                else -> h
            }
            val minStr = String.format(Locale.US, "%02d", minute)
            "$monthStr $day, $year, $displayHour:$minStr $amPm"
        } else {
            "$monthStr $day, $year"
        }
    }

    private fun getMonthNumber(monthStr: String): Int? {
        return when (monthStr.lowercase(Locale.ROOT)) {
            "january", "jan" -> 1
            "february", "feb" -> 2
            "march", "mar" -> 3
            "april", "apr" -> 4
            "may" -> 5
            "june", "jun" -> 6
            "july", "jul" -> 7
            "august", "aug" -> 8
            "september", "sep", "sept" -> 9
            "october", "oct" -> 10
            "november", "nov" -> 11
            "december", "dec" -> 12
            else -> null
        }
    }

    fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
