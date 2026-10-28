package com.example.utils

object ArticleSanitizer {

    /**
     * Removes unwanted opening words, codes, or labels such as
     * "SIM2:-", "SIM2:", "SIM2", "CALL 2:-", "CALL 2:", "CALL 2",
     * "OR2:-", "OR2:", "OR2", "PHO1:-", "PHO1:", "PHO1",
     * or similar patterns like "TAG1:-", "APP 1:", etc., from the beginning of titles or content.
     */
    fun cleanPrefix(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val prefixRegex = Regex(
            """^(?:SIM\s*\d+|CALL\s*\d+|OR\s*\d+|PHO\s*\d+|TAG\s*\d+|APP\s*\d+|[A-Z]{2,6}\s*\d+)\s*(?:[:\-\–\—]+\s*|\s+)""",
            RegexOption.IGNORE_CASE
        )
        var cleaned = input.trim()
        while (prefixRegex.containsMatchIn(cleaned)) {
            cleaned = cleaned.replace(prefixRegex, "").trim()
        }
        return cleaned
    }

    fun cleanList(list: List<String>): List<String> {
        return list.map { cleanPrefix(it) }
    }
}
