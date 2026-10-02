package com.perso.videotheque.domain

import java.text.Normalizer

/**
 * Détecte les zones du corps dans le titre d'une vidéo (français et anglais)
 * pour présélectionner les tags correspondants.
 */
object BodyKeywords {
    const val HIP = "Hanche"
    const val SHOULDER = "Épaule"
    const val BACK = "Dos"
    const val UPPER_BODY = "Haut du corps"
    const val LOWER_BODY = "Bas du corps"

    val ALL = listOf(HIP, SHOULDER, BACK, UPPER_BODY, LOWER_BODY)

    // Texte normalisé : minuscules, sans accents.
    private val rules = mapOf(
        HIP to words("hanches?", "hips?", "bassin", "pelvis", "pelvic", "pelvien(ne)?s?", "psoas"),
        SHOULDER to words(
            "epaules?", "shoulders?", "omoplates?", "scapulas?", "trapezes?",
            "rotator cuff", "coiffe des rotateurs",
        ),
        BACK to Regex(
            words(
                "dos", "lombaires?", "lombalgies?", "dorsal(e|es|aux)?", "colonne vertebrale",
                "rachis", "spine", "spinal", "backbends?", "back pain", "lower back", "upper back",
            ).pattern +
                // "back" seul, sauf dans les tournures sans rapport ("welcome back", "back to"…).
                """|(?<!welcome )(?<!come )(?<!coming )(?<!get )(?<!go )(?<!bring )\bback\b(?! to\b)""",
        ),
        UPPER_BODY to words(
            "haut du corps", "upper body", "bras", "arms?", "nuque", "cou", "neck",
            "poitrine", "chest", "pectoraux", "poignets?", "wrists?",
        ),
        LOWER_BODY to words(
            "bas du corps", "lower body", "jambes?", "legs?", "fessiers?", "glutes?",
            "ischios?", "ischio-jambiers", "hamstrings?", "quadriceps", "quads?",
            "genoux", "genou", "knees?", "chevilles?", "ankles?", "mollets?", "calves",
            "cuisses?", "thighs?", "adducteurs?", "pieds?", "feet",
        ),
    )

    // Une hanche fait partie du bas du corps, une épaule du haut du corps.
    private val implied = mapOf(HIP to LOWER_BODY, SHOULDER to UPPER_BODY)

    fun detect(text: String): Set<String> {
        val normalized = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
            .replace(Regex("""[’'_|]"""), " ")
        val found = rules.filterValues { it.containsMatchIn(normalized) }.keys
        return found + found.mapNotNull { implied[it] }
    }

    private fun words(vararg alternatives: String) =
        Regex(alternatives.joinToString("|") { """\b(?:$it)\b""" })
}
