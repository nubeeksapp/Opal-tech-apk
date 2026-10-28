package com.example.utils

/**
 * Utility for splitting prompt text into configured scene parts.
 *
 * Rules:
 * 1. Honors explicit scenes supplied by admin panel if present.
 * 2. Recognizes natural scene markers in the prompt (Scene 1:, Part 1:, Section 1:, delimiter lines ---, etc.).
 * 3. If no markers exist, partitions text into [targetCount] balanced, readable parts without splitting words.
 * 4. Ensures the entire prompt is preserved across parts without missing or duplicated content.
 * 5. Falls back safely to 1 complete part if scene count is unconfigured, invalid (<= 1), or content cannot be split.
 */
object SceneSplitter {

    fun splitIntoScenes(
        prompt: String,
        targetCount: Int = 0,
        explicitScenes: List<String> = emptyList()
    ): List<String> {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isEmpty()) return emptyList()

        // 1. Explicit scenes list provided by admin panel
        val validExplicit = explicitScenes.map { it.trim() }.filter { it.isNotEmpty() }
        if (validExplicit.isNotEmpty()) {
            return validExplicit
        }

        // 2. Safe fallback if target count is unconfigured or invalid
        if (targetCount <= 1) {
            // Check if prompt naturally has multiple scenes even if targetCount was 0
            val naturalScenes = detectNaturalScenes(trimmedPrompt)
            return if (naturalScenes.size > 1 && targetCount == 0) {
                naturalScenes
            } else {
                listOf(trimmedPrompt)
            }
        }

        // 3. Try natural scene boundary detection first
        val naturalScenes = detectNaturalScenes(trimmedPrompt)
        if (naturalScenes.size == targetCount) {
            return naturalScenes
        }

        // 4. Divide into targetCount balanced parts without splitting words
        return partitionTextBalanced(trimmedPrompt, targetCount)
    }

    /**
     * Detects explicit scene / part / section markers or horizontal delimiter lines in the text.
     */
    fun detectNaturalScenes(prompt: String): List<String> {
        // Pattern A: Horizontal rule delimiters e.g. --- or ===
        val delimiterSplit = prompt.split(Regex("""(?:\r?\n)+\s*(?:---+|===+)\s*(?:\r?\n)+"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (delimiterSplit.size > 1) {
            return delimiterSplit
        }

        // Pattern B: Explicit numbered markers like "Part 1:", "Scene 1:", "Section 1:"
        val markerRegex = Regex("""(?:^|\r?\n)\s*(?:###?\s*|\[)?(?:Scene|Part|Section)\s*(\d+)[:\]\s\-\.]*""", RegexOption.IGNORE_CASE)
        val matches = markerRegex.findAll(prompt).toList()
        if (matches.size > 1) {
            val extractedParts = mutableListOf<String>()
            for (i in matches.indices) {
                val startIndex = matches[i].range.first
                val endIndex = if (i + 1 < matches.size) matches[i + 1].range.first else prompt.length
                val partContent = prompt.substring(startIndex, endIndex).trim()
                if (partContent.isNotEmpty()) {
                    extractedParts.add(partContent)
                }
            }
            if (extractedParts.size > 1) {
                return extractedParts
            }
        }

        // Pattern C: Fallback to delimiter header split
        val headerPattern = Regex("""(?=(?:\r?\n)+\s*(?:###?\s*|\[)?(?:Scene|Part|Section)\s*\d+[:\]\s\-\.]*)""", RegexOption.IGNORE_CASE)
        val parts = prompt.split(headerPattern)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (parts.size > 1) {
            return parts
        }

        return emptyList()
    }

    /**
     * Removes leading "Part 1:", "Scene 1:", etc. heading from part content,
     * ensuring copying yields only the exact content inside that specific part.
     */
    fun cleanPartContent(raw: String): String {
        return raw
            .replaceFirst(Regex("""^(?:###?\s*|\[)?(?:Scene|Part|Section)\s*\d+[:\]\s\-\.]*\s*""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    /**
     * Partitions text into [k] balanced chunks without splitting words.
     * Tries paragraphs first, then sentences, then lines, then words.
     */
    fun partitionTextBalanced(text: String, k: Int): List<String> {
        if (k <= 1) return listOf(text.trim())

        // 1. Try paragraphs (split by double newlines)
        val paragraphs = text.split(Regex("""(?:\r?\n\s*){2,}"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (paragraphs.size >= k) {
            return distributeBalanced(paragraphs, delimiter = "\n\n", k = k)
        }

        // 2. Try sentences
        val sentenceRegex = Regex("""(?<=[.!?])\s+""")
        val sentences = text.split(sentenceRegex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (sentences.size >= k) {
            return distributeBalanced(sentences, delimiter = " ", k = k)
        }

        // 3. Try lines
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (lines.size >= k) {
            return distributeBalanced(lines, delimiter = "\n", k = k)
        }

        // 4. Try words without breaking words
        val words = text.trim().split(Regex("""\s+"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (words.size >= k) {
            return distributeBalanced(words, delimiter = " ", k = k)
        }

        // If content has fewer words than k, return 1 complete part as safe fallback
        return listOf(text.trim())
    }

    /**
     * Distributes [units] into exactly [k] non-empty chunks balancing character lengths.
     * Guarantees each chunk has at least 1 unit and all units are preserved in order.
     */
    private fun distributeBalanced(units: List<String>, delimiter: String, k: Int): List<String> {
        val n = units.size
        if (k <= 1 || n <= k) {
            if (n <= k) {
                // If exactly n == k, each unit becomes a part
                return units
            }
            return listOf(units.joinToString(delimiter))
        }

        val totalChars = units.sumOf { it.length }
        val targetPerChunk = totalChars.toDouble() / k

        val result = mutableListOf<String>()
        var currentIndex = 0

        for (chunkIndex in 0 until k) {
            val remainingChunks = k - chunkIndex
            val remainingUnits = n - currentIndex

            if (remainingChunks == 1) {
                // Last chunk gets all remaining units
                val chunk = units.subList(currentIndex, n).joinToString(delimiter)
                result.add(chunk)
                break
            }

            // Must leave at least 1 unit for each remaining chunk
            val maxUnitsForThisChunk = remainingUnits - (remainingChunks - 1)
            var bestCount = 1
            var accumulatedLength = units[currentIndex].length
            var bestDiff = Math.abs(accumulatedLength - targetPerChunk)

            for (count in 2..maxUnitsForThisChunk) {
                accumulatedLength += units[currentIndex + count - 1].length + delimiter.length
                val diff = Math.abs(accumulatedLength - targetPerChunk)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestCount = count
                } else if (accumulatedLength > targetPerChunk && diff > bestDiff) {
                    // Moving further away after exceeding target
                    break
                }
            }

            val chunk = units.subList(currentIndex, currentIndex + bestCount).joinToString(delimiter)
            result.add(chunk)
            currentIndex += bestCount
        }

        return result
    }
}
