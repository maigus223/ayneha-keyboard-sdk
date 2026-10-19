package com.maigus.ayneha.sdk

/**
 * Mapping public et complet des 98 glyphes de l'alphabet AYNEHA, bloc réservé
 * du plan 16 (Private Use Area-B) : U+10FF7C à U+10FFDD, tel que défini par la
 * Matrice Officielle de l'Alphabet AYNEHA. Utilisez ces constantes plutôt que
 * des codes en dur : si une révision future de la police déplace des
 * codepoints, seule cette classe aura besoin d'être mise à jour.
 *
 * ATTENTION - paires de substitution (UTF-16) : ces codepoints sont au-delà du
 * plan de base, donc chaque glyphe occupe 2 unités UTF-16 et ne tient plus dans
 * un [Char]. Toutes les valeurs sont donc des [String] d'UN glyphe (2 unités).
 * Ne jamais compter, découper ou inverser ces chaînes avec `length`, `[i]`,
 * `substring` ou `reversed()` : utiliser les fonctions « codepoint »
 * (`codePointCount`, `offsetByCodePoints`, `StringBuilder.reverse()`, ...).
 *
 * Directement affichables avec la police fournie (`ayneha_pro.ttf`, qui
 * contient aussi l'ancienne plage U+E000-U+E061 pour la transition).
 */
object AynehaCodepoints {

    /** Premier et dernier codepoint du bloc AYNEHA (98 glyphes ; réservé jusqu'à U+10FFFD). */
    const val PUA_START = 0x10FF7C
    const val PUA_END = 0x10FFDD

    /** Ancienne plage (versions du SDK antérieures à 1.1.0), conservée pour migration de textes existants. */
    const val LEGACY_PUA_START = 0xE000
    const val LEGACY_PUA_END = 0xE061

    /** Convertit un codepoint en String (1 ou 2 unités UTF-16). */
    private fun g(codePoint: Int): String = String(Character.toChars(codePoint))

    // --- 1. Chiffres (U+10FF7C – U+10FF85) ---
    val DIGITS: Map<Int, String> = (0..9).associateWith { g(PUA_START + it) }
    fun digit(d: Int): String {
        require(d in 0..9) { "digit doit être compris entre 0 et 9" }
        return g(PUA_START + d)
    }

    // --- 2. Consonnes de base (U+10FF86 – U+10FF9B) ---
    val CONSONANTS: Map<String, String> = linkedMapOf(
        "B" to g(0x10FF86), "P" to g(0x10FF87), "M" to g(0x10FF88),
        "F" to g(0x10FF89), "W" to g(0x10FF8A), "D" to g(0x10FF8B),
        "T" to g(0x10FF8C), "N" to g(0x10FF8D), "L" to g(0x10FF8E),
        "R" to g(0x10FF8F), "S" to g(0x10FF90), "Z" to g(0x10FF91),
        "SH" to g(0x10FF92),  // Σ — son /ʃ/
        "TS" to g(0x10FF93),  // TΣ — son /tʃ/
        "J" to g(0x10FF94), "DJ" to g(0x10FF95),
        "NY" to g(0x10FF96),  // Ɲ — son /ɲ/
        "Y" to g(0x10FF97), "G" to g(0x10FF98), "K" to g(0x10FF99),
        "H" to g(0x10FF9A),
        "NG" to g(0x10FF9B)   // Ŋ — son /ŋ/
    )

    // --- 3. Voyelles de base (U+10FF9C – U+10FFA0) ---
    val VOWELS: Map<String, String> = linkedMapOf(
        "A" to g(0x10FF9C), "E" to g(0x10FF9D), "I" to g(0x10FF9E),
        "O" to g(0x10FF9F), "U" to g(0x10FFA0)
    )

    /** Type d'accent applicable à une lettre de base pour former une combinaison. */
    enum class Accent { DOUBLE, NASALISE, MUET, OUVERT }

    /**
     * 4. Combinaisons pré-composées (U+10FFA1 – U+10FFD2), lettre de base + accent.
     * Toutes les combinaisons de la matrice officielle ne sont pas définies
     * pour chaque lettre (ex. MUET n'existe que pour les consonnes, OUVERT
     * seulement pour E et O) — voir la matrice officielle pour le détail.
     */
    val COMBINATIONS: Map<Pair<String, Accent>, String> = linkedMapOf(
        ("A" to Accent.DOUBLE) to g(0x10FFA1), ("A" to Accent.NASALISE) to g(0x10FFA2),
        ("E" to Accent.DOUBLE) to g(0x10FFA3), ("E" to Accent.NASALISE) to g(0x10FFA4),
        ("E" to Accent.OUVERT) to g(0x10FFA5),
        ("I" to Accent.DOUBLE) to g(0x10FFA6), ("I" to Accent.NASALISE) to g(0x10FFA7),
        ("O" to Accent.DOUBLE) to g(0x10FFA8), ("O" to Accent.NASALISE) to g(0x10FFA9),
        ("O" to Accent.OUVERT) to g(0x10FFAA),
        ("U" to Accent.DOUBLE) to g(0x10FFAB), ("U" to Accent.NASALISE) to g(0x10FFAC),
        ("B" to Accent.DOUBLE) to g(0x10FFAD), ("B" to Accent.MUET) to g(0x10FFAE),
        ("P" to Accent.DOUBLE) to g(0x10FFAF), ("P" to Accent.MUET) to g(0x10FFB0),
        ("M" to Accent.DOUBLE) to g(0x10FFB1), ("M" to Accent.MUET) to g(0x10FFB2),
        ("F" to Accent.DOUBLE) to g(0x10FFB3), ("F" to Accent.MUET) to g(0x10FFB4),
        ("W" to Accent.DOUBLE) to g(0x10FFB5), ("W" to Accent.MUET) to g(0x10FFB6),
        ("D" to Accent.DOUBLE) to g(0x10FFB7), ("D" to Accent.MUET) to g(0x10FFB8),
        ("T" to Accent.DOUBLE) to g(0x10FFB9), ("T" to Accent.MUET) to g(0x10FFBA),
        ("N" to Accent.DOUBLE) to g(0x10FFBB), ("N" to Accent.MUET) to g(0x10FFBC),
        ("L" to Accent.DOUBLE) to g(0x10FFBD), ("L" to Accent.MUET) to g(0x10FFBE),
        ("R" to Accent.DOUBLE) to g(0x10FFBF), ("R" to Accent.MUET) to g(0x10FFC0),
        ("S" to Accent.DOUBLE) to g(0x10FFC1), ("S" to Accent.MUET) to g(0x10FFC2),
        ("Z" to Accent.DOUBLE) to g(0x10FFC3), ("Z" to Accent.MUET) to g(0x10FFC4),
        ("SH" to Accent.DOUBLE) to g(0x10FFC5),
        ("TS" to Accent.DOUBLE) to g(0x10FFC6),
        ("J" to Accent.DOUBLE) to g(0x10FFC7),
        ("DJ" to Accent.DOUBLE) to g(0x10FFC8), ("DJ" to Accent.MUET) to g(0x10FFC9),
        ("NY" to Accent.DOUBLE) to g(0x10FFCA),
        ("Y" to Accent.DOUBLE) to g(0x10FFCB), ("Y" to Accent.MUET) to g(0x10FFCC),
        ("G" to Accent.DOUBLE) to g(0x10FFCD), ("G" to Accent.MUET) to g(0x10FFCE),
        ("K" to Accent.DOUBLE) to g(0x10FFCF), ("K" to Accent.MUET) to g(0x10FFD0),
        ("NG" to Accent.DOUBLE) to g(0x10FFD1), ("NG" to Accent.MUET) to g(0x10FFD2)
    )

    // --- 5. Accents isolés et ponctuation (U+10FFD3 – U+10FFDD) ---
    val ACCENT_NASALISE_A = g(0x10FFD3)
    val ACCENT_NASALISE_E = g(0x10FFD4)
    val ACCENT_NASALISE_I = g(0x10FFD5)
    val ACCENT_NASALISE_O = g(0x10FFD6)
    val ACCENT_NASALISE_U = g(0x10FFD7)
    val ACCENT_OUVERT_E = g(0x10FFD8)
    val ACCENT_OUVERT_O = g(0x10FFD9)
    val ACCENT_DOUBLE_GENERIC = g(0x10FFDA)
    val ACCENT_MUET_GENERIC = g(0x10FFDB)
    val VIRGULE = g(0x10FFDC)        // virgule AYNEHA (RTL)
    val POINT_VIRGULE = g(0x10FFDD)  // point-virgule AYNEHA (RTL)

    /** Vrai si [codePoint] appartient au bloc AYNEHA actuel (U+10FF7C - U+10FFDD). */
    fun isAynehaCodepoint(codePoint: Int): Boolean = codePoint in PUA_START..PUA_END

    /** Vrai si [codePoint] appartient à l'ancienne plage U+E000 - U+E061 (textes créés avant la migration). */
    fun isLegacyAynehaCodepoint(codePoint: Int): Boolean = codePoint in LEGACY_PUA_START..LEGACY_PUA_END
}
