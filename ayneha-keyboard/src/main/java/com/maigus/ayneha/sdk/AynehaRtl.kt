package com.maigus.ayneha.sdk

/**
 * AYNEHA s'écrit en RTL pur (lettres ET chiffres). Les codepoints de la zone privée
 * n'ont pas de classe bidirectionnelle Unicode propre : par défaut ils sont traités
 * comme gauche-à-droite, et un texte tapé glisserait donc vers la droite.
 *
 * Chaque glyphe saisi directement (widget [AynehaKeyboardView] ou clavier système)
 * est donc enveloppé en RLO + glyphe + PDF (U+202E ... U+202C), ce qui force son
 * affichage en RTL : le texte grandit de droite à gauche, chiffres compris, et
 * l'effacement retire le dernier glyphe tapé.
 *
 * Depuis la migration vers le plan 16, un glyphe occupe 2 unités UTF-16 : un
 * enveloppe complète fait donc 4 unités (RLO + paire de substitution + PDF).
 * Les textes anciens (U+E000-U+E061, enveloppe de 3 unités) restent gérés.
 */
object AynehaRtl {
    const val RLO = '‮' // Right-to-Left Override
    const val PDF = '‬' // Pop Directional Formatting

    /** Nombre maximal d'unités UTF-16 d'une enveloppe (à demander avant le curseur). */
    const val MAX_WRAPPER_UNITS = 4

    /** Enveloppe un glyphe AYNEHA pour forcer son affichage en RTL pur. */
    fun wrap(glyph: String): String = "$RLO$glyph$PDF"

    /**
     * Nombre d'unités UTF-16 à effacer pour UN retour arrière, d'après les (jusqu'à 4)
     * unités situées juste avant le curseur : 4 (enveloppe plan 16), 3 (enveloppe de
     * l'ancienne plage), 2 (paire de substitution isolée, p. ex. un emoji), sinon 1.
     */
    fun backspaceLength(textBeforeCursor: CharSequence): Int {
        val n = textBeforeCursor.length
        if (n >= 4 &&
            textBeforeCursor[n - 1] == PDF &&
            Character.isLowSurrogate(textBeforeCursor[n - 2]) &&
            Character.isHighSurrogate(textBeforeCursor[n - 3]) &&
            textBeforeCursor[n - 4] == RLO
        ) return 4
        if (n >= 3 && textBeforeCursor[n - 1] == PDF && textBeforeCursor[n - 3] == RLO) return 3
        if (n >= 2 &&
            Character.isLowSurrogate(textBeforeCursor[n - 1]) &&
            Character.isHighSurrogate(textBeforeCursor[n - 2])
        ) return 2
        return 1
    }
}
