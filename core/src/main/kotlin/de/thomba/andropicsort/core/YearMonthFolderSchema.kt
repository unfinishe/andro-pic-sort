package de.thomba.andropicsort.core

import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

object YearMonthFolderSchema {
    fun pathFor(date: LocalDateTime, locale: Locale): Pair<String, String> {
        val monthName = date.month.getDisplayName(TextStyle.FULL, locale)
            .replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(locale) else c.toString()
            }
        return date.year.toString() to String.format(locale, "%02d %s", date.monthValue, monthName)
    }
}

