package com.maigus.ayneha.sdk

/**
 * Mapping public et complet des 98 glyphes de l'alphabet AYNEHA, zone privée
 * Unicode U+E000 à U+E061, tel que défini par la Matrice Officielle de
 * l'Alphabet AYNEHA. Utilisez ces constantes plutôt que des codes en dur :
 * si une révision future de la police déplace des codepoints, seule cette
 * classe aura besoin d'être mise à jour.
 *
 * Toutes les valeurs sont des [Char] uniques (BMP, zone U+E000–U+E061),
 * directement affichables avec la police fournie (`ayneha_type.ttf`).
 */
object AynehaCodepoints {

    // --- 1. Chiffres (U+E000 – U+E009) ---
    val DIGITS: Map<Int, Char> = (0..9).associateWith { (0xE000 + it).toChar() }
    fun digit(d: Int): Char {
        require(d in 0..9) { "digit doit être compris entre 0 et 9" }
        return (0xE000 + d).toChar()
    }

    // --- 2. Consonnes de base (U+E00A – U+E01F) ---
    val CONSONANTS: Map<String, Char> = linkedMapOf(
        "B" to 0xE00A.toChar(), "P" to 0xE00B.toChar(), "M" to 0xE00C.toChar(),
        "F" to 0xE00D.toChar(), "W" to 0xE00E.toChar(), "D" to 0xE00F.toChar(),
        "T" to 0xE010.toChar(), "N" to 0xE011.toChar(), "L" to 0xE012.toChar(),
        "R" to 0xE013.toChar(), "S" to 0xE014.toChar(), "Z" to 0xE015.toChar(),
        "SH" to 0xE016.toChar(),  // Σ — son /ʃ/
        "TS" to 0xE017.toChar(),  // TΣ — son /tʃ/
        "J" to 0xE018.toChar(), "DJ" to 0xE019.toChar(),
        "NY" to 0xE01A.toChar(),  // Ɲ — son /ɲ/
        "Y" to 0xE01B.toChar(), "G" to 0xE01C.toChar(), "K" to 0xE01D.toChar(),
        "H" to 0xE01E.toChar(),
        "NG" to 0xE01F.toChar()   // Ŋ — son /ŋ/
    )

    // --- 3. Voyelles de base (U+E020 – U+E024) ---
    val VOWELS: Map<String, Char> = linkedMapOf(
        "A" to 0xE020.toChar(), "E" to 0xE021.toChar(), "I" to 0xE022.toChar(),
        "O" to 0xE023.toChar(), "U" to 0xE024.toChar()
    )

    /** Type d'accent applicable à une lettre de base pour former une combinaison. */
    enum class Accent { DOUBLE, NASALISE, MUET, OUVERT }

    /**
     * 4. Combinaisons pré-composées (U+E025 – U+E056), lettre de base + accent.
     * Toutes les combinaisons de la matrice officielle ne sont pas définies
     * pour chaque lettre (ex. MUET n'existe que pour les consonnes, OUVERT
     * seulement pour E et O) — voir la matrice officielle pour le détail.
     */
    val COMBINATIONS: Map<Pair<String, Accent>, Char> = linkedMapOf(
        ("A" to Accent.DOUBLE) to 0xE025.toChar(), ("A" to Accent.NASALISE) to 0xE026.toChar(),
        ("E" to Accent.DOUBLE) to 0xE027.toChar(), ("E" to Accent.NASALISE) to 0xE028.toChar(),
        ("E" to Accent.OUVERT) to 0xE029.toChar(),
        ("I" to Accent.DOUBLE) to 0xE02A.toChar(), ("I" to Accent.NASALISE) to 0xE02B.toChar(),
        ("O" to Accent.DOUBLE) to 0xE02C.toChar(), ("O" to Accent.NASALISE) to 0xE02D.toChar(),
        ("O" to Accent.OUVERT) to 0xE02E.toChar(),
        ("U" to Accent.DOUBLE) to 0xE02F.toChar(), ("U" to Accent.NASALISE) to 0xE030.toChar(),
        ("B" to Accent.DOUBLE) to 0xE031.toChar(), ("B" to Accent.MUET) to 0xE032.toChar(),
        ("P" to Accent.DOUBLE) to 0xE033.toChar(), ("P" to Accent.MUET) to 0xE034.toChar(),
        ("M" to Accent.DOUBLE) to 0xE035.toChar(), ("M" to Accent.MUET) to 0xE036.toChar(),
        ("F" to Accent.DOUBLE) to 0xE037.toChar(), ("F" to Accent.MUET) to 0xE038.toChar(),
        ("W" to Accent.DOUBLE) to 0xE039.toChar(), ("W" to Accent.MUET) to 0xE03A.toChar(),
        ("D" to Accent.DOUBLE) to 0xE03B.toChar(), ("D" to Accent.MUET) to 0xE03C.toChar(),
        ("T" to Accent.DOUBLE) to 0xE03D.toChar(), ("T" to Accent.MUET) to 0xE03E.toChar(),
        ("N" to Accent.DOUBLE) to 0xE03F.toChar(), ("N" to Accent.MUET) to 0xE040.toChar(),
        ("L" to Accent.DOUBLE) to 0xE041.toChar(), ("L" to Accent.MUET) to 0xE042.toChar(),
        ("R" to Accent.DOUBLE) to 0xE043.toChar(), ("R" to Accent.MUET) to 0xE044.toChar(),
        ("S" to Accent.DOUBLE) to 0xE045.toChar(), ("S" to Accent.MUET) to 0xE046.toChar(),
        ("Z" to Accent.DOUBLE) to 0xE047.toChar(), ("Z" to Accent.MUET) to 0xE048.toChar(),
        ("SH" to Accent.DOUBLE) to 0xE049.toChar(),
        ("TS" to Accent.DOUBLE) to 0xE04A.toChar(),
        ("J" to Accent.DOUBLE) to 0xE04B.toChar(),
        ("DJ" to Accent.DOUBLE) to 0xE04C.toChar(), ("DJ" to Accent.MUET) to 0xE04D.toChar(),
        ("NY" to Accent.DOUBLE) to 0xE04E.toChar(),
        ("Y" to Accent.DOUBLE) to 0xE04F.toChar(), ("Y" to Accent.MUET) to 0xE050.toChar(),
        ("G" to Accent.DOUBLE) to 0xE051.toChar(), ("G" to Accent.MUET) to 0xE052.toChar(),
        ("K" to Accent.DOUBLE) to 0xE053.toChar(), ("K" to Accent.MUET) to 0xE054.toChar(),
        ("NG" to Accent.DOUBLE) to 0xE055.toChar(), ("NG" to Accent.MUET) to 0xE056.toChar()
    )

    // --- 5. Accents isolés et ponctuation (U+E057 – U+E061) ---
    val ACCENT_NASALISE_A = 0xE057.toChar()
    val ACCENT_NASALISE_E = 0xE058.toChar()
    val ACCENT_NASALISE_I = 0xE059.toChar()
    val ACCENT_NASALISE_O = 0xE05A.toChar()
    val ACCENT_NASALISE_U = 0xE05B.toChar()
    val ACCENT_OUVERT_E = 0xE05C.toChar()
    val ACCENT_OUVERT_O = 0xE05D.toChar()
    val ACCENT_DOUBLE_GENERIC = 0xE05E.toChar()
    val ACCENT_MUET_GENERIC = 0xE05F.toChar()
    val VIRGULE = 0xE060.toChar()        // virgule AYNEHA (RTL)
    val POINT_VIRGULE = 0xE061.toChar()  // point-virgule AYNEHA (RTL)

    /** Tous les codepoints de la zone privée AYNEHA, du premier au dernier. */
    const val PUA_START = 0xE000
    const val PUA_END = 0xE061

    fun isAynehaCodepoint(c: Char): Boolean = c.code in PUA_START..PUA_END
}
