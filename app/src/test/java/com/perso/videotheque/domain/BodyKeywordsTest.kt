package com.perso.videotheque.domain

import com.perso.videotheque.domain.BodyKeywords.BACK
import com.perso.videotheque.domain.BodyKeywords.HIP
import com.perso.videotheque.domain.BodyKeywords.LOWER_BODY
import com.perso.videotheque.domain.BodyKeywords.SHOULDER
import com.perso.videotheque.domain.BodyKeywords.UPPER_BODY
import org.junit.Assert.assertEquals
import org.junit.Test

class BodyKeywordsTest {

    private fun detect(title: String) = BodyKeywords.detect(title)

    @Test fun french() {
        assertEquals(setOf(HIP, LOWER_BODY), detect("Yoga doux pour ouvrir les HANCHES"))
        assertEquals(setOf(SHOULDER, UPPER_BODY), detect("Détendre les épaules et la nuque"))
        assertEquals(setOf(HIP, BACK, LOWER_BODY), detect("Séance pour libérer le bassin et le dos"))
        assertEquals(setOf(BACK), detect("Soulager les douleurs lombaires"))
        assertEquals(setOf(UPPER_BODY), detect("Renforcement du haut du corps"))
        assertEquals(setOf(LOWER_BODY), detect("Yoga bas du corps : jambes et fessiers"))
    }

    @Test fun english() {
        assertEquals(setOf(HIP, LOWER_BODY), detect("Yin Yoga for Tight Hips"))
        assertEquals(setOf(SHOULDER, UPPER_BODY), detect("Shoulder & Neck Release"))
        assertEquals(setOf(BACK), detect("Yoga for Lower Back Pain"))
        assertEquals(setOf(BACK), detect("15 min back stretch"))
        assertEquals(setOf(UPPER_BODY), detect("Upper Body Flow"))
        assertEquals(setOf(LOWER_BODY), detect("Lower body stretch for hamstrings"))
    }

    @Test fun combinedAndAccentInsensitive() {
        assertEquals(
            setOf(HIP, SHOULDER, BACK, UPPER_BODY, LOWER_BODY),
            detect("Epaules, dos et hanches – full body"),
        )
    }

    @Test fun ignoresUnrelatedWords() {
        assertEquals(emptySet<String>(), detect("Welcome back! Back to basics yoga"))
        assertEquals(emptySet<String>(), detect("Yoga doux du matin pour débutants"))
        assertEquals(emptySet<String>(), detect("Coucou, séance couleur"))
        assertEquals(emptySet<String>(), detect("Dossier spécial hiphop"))
    }
}
