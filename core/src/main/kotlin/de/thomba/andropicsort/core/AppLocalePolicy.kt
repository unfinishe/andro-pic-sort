package de.thomba.andropicsort.core

import java.util.Locale

object AppLocalePolicy {
    fun effectiveLocale(systemLocale: Locale): Locale {
        return if (systemLocale.language.equals(Locale.GERMAN.language, ignoreCase = true)) {
            Locale.GERMAN
        } else {
            Locale.ENGLISH
        }
    }
}

