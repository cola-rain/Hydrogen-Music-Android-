package com.hydrogen.padzero.data

import java.util.Locale
import kotlin.math.max

data class LyricLine(
    val timeMs: Long,
    val text: String,
)

private val lrcTimeRegex = Regex("""\[(\d{1,3})\s*[:：\.．。,，;；/\-_\s]\s*(\d{1,2})(?:\s*[:：\.．。,，;；/\-_\s]\s*(\d{1,3}))?\]""")
private val metaRegex = Regex("""^\[(ti|ar|al|by|offset|re|ve|length|album|artist|title|author|language|name):.*\]$""", RegexOption.IGNORE_CASE)

fun parseLrc(rawText: String?): List<LyricLine> {
    if (rawText.isNullOrBlank()) return emptyList()

    val normalized = rawText
        .replace("\r\n", "\n")
        .replace('\r', '\n')

    val result = mutableListOf<LyricLine>()
    for (rawLine in normalized.lineSequence()) {
        val line = rawLine.trim().removePrefix("\uFEFF")
        if (line.isBlank()) continue
        if (metaRegex.matches(line)) continue

        val tags = lrcTimeRegex.findAll(line).toList()
        if (tags.isEmpty()) continue

        val text = line.replace(lrcTimeRegex, "").trim()
        if (text.isBlank()) continue

        for (tag in tags) {
            val minutes = tag.groupValues.getOrNull(1)?.toLongOrNull() ?: 0L
            val seconds = tag.groupValues.getOrNull(2)?.toLongOrNull() ?: 0L
            val millisRaw = tag.groupValues.getOrNull(3).orEmpty()
            val millis = millisRaw.padEnd(3, '0').take(3).toLongOrNull() ?: 0L
            result += LyricLine(
                timeMs = minutes * 60_000L + seconds * 1_000L + millis,
                text = text,
            )
        }
    }

    return result
        .distinctBy { "${it.timeMs}:${it.text}" }
        .sortedBy { it.timeMs }
}

fun activeLyricIndex(lines: List<LyricLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1
    val safePosition = max(0L, positionMs)
    var best = -1
    for (index in lines.indices) {
        if (safePosition >= lines[index].timeMs) best = index else break
    }
    return best
}

fun formatLrcTime(positionMs: Long): String {
    val normalized = max(0L, positionMs)
    val totalSeconds = normalized / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
