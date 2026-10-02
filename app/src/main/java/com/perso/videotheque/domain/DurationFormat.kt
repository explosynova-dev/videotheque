package com.perso.videotheque.domain

object DurationFormat {

    /** 1125 -> "18 min 45 s", 500 -> "08 min 20 s", 3725 -> "1 h 02 min 05 s". */
    fun format(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d h %02d min %02d s".format(h, m, s)
        else "%02d min %02d s".format(m, s)
    }

    /** Saisie manuelle : "12:34", "1:02:05" ou "12" (minutes). */
    fun parseInput(text: String): Int? {
        val parts = text.trim().split(":")
        if (parts.any { it.isBlank() || !it.all(Char::isDigit) }) return null
        val n = parts.map { it.toInt() }
        return when (n.size) {
            1 -> n[0] * 60
            2 -> if (n[1] < 60) n[0] * 60 + n[1] else null
            3 -> if (n[1] < 60 && n[2] < 60) n[0] * 3600 + n[1] * 60 + n[2] else null
            else -> null
        }
    }

    /** Valeur affichée dans le champ de saisie : "12:34" ou "1:02:05". */
    fun toInput(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    /** ISO 8601 (ex. "PT18M45S") -> secondes. */
    fun parseIso8601(value: String): Int? {
        val match = Regex("""^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$""").find(value) ?: return null
        val (h, m, s) = match.destructured
        if (h.isEmpty() && m.isEmpty() && s.isEmpty()) return null
        return (h.toIntOrNull() ?: 0) * 3600 + (m.toIntOrNull() ?: 0) * 60 + (s.toIntOrNull() ?: 0)
    }
}
