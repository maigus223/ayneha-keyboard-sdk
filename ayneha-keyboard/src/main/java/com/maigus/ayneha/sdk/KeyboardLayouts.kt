package com.maigus.ayneha.sdk

/**
 * Description d'une touche du clavier AYNEHA : le glyphe AYNEHA qu'elle
 * émet (un String d'un seul codepoint = 2 unités UTF-16 depuis la migration
 * vers le plan 16), plus une étiquette latine facultative affichée en mode
 * translittération (pour aider un utilisateur non encore familier du script).
 */
data class AynehaKey(val glyph: String, val latinLabel: String)

/**
 * Dispositions de touches partagées par [AynehaKeyboardView] (widget in-app)
 * et [com.maigus.ayneha.sdk.ime.AynehaInputMethodService] (clavier système),
 * pour garantir un comportement strictement identique entre les deux modes
 * d'intégration.
 *
 * Organisation en rangées façon clavier Gboard/Keyman (format 10-10-7) dans l'ordre
 * alphabétique AYNEHA (voyelles puis consonnes),
 * complétée d'une rangée de chiffres et d'une rangée de contrôle.
 */
object KeyboardLayouts {

    val digitsRow: List<AynehaKey> = (0..9).map {
        AynehaKey(AynehaCodepoints.digit(it), it.toString())
    }

    /**
     * Ordre alphabétique AYNEHA : d'abord les 5 voyelles (A E I O U), ensuite les 22
     * consonnes, dans l'ordre exact de [AynehaCodepoints.VOWELS] puis de
     * [AynehaCodepoints.CONSONANTS] (ordre de la Matrice Officielle). Les touches sont
     * simplement découpées en rangées 10-10-7 ; en RTL, la première touche de chaque
     * rangée est à l'extrême droite.
     */
    private val alphabetKeys: List<AynehaKey> =
        AynehaCodepoints.VOWELS.map { (label, glyph) -> AynehaKey(glyph, label) } +
        AynehaCodepoints.CONSONANTS.map { (label, glyph) -> AynehaKey(glyph, label) }

    // Rangée 1 (10 touches) : les 5 voyelles + les 5 premières consonnes
    val row1: List<AynehaKey> = alphabetKeys.subList(0, 10)

    // Rangée 2 (10 touches) : consonnes suivantes
    val row2: List<AynehaKey> = alphabetKeys.subList(10, 20)

    // Rangée 3 (7 touches) : dernières consonnes
    val row3: List<AynehaKey> = alphabetKeys.subList(20, 27)

    /**
     * Rangée d'accents : chaque touche applique un accent à la DERNIÈRE
     * lettre saisie (transforme le dernier caractère en sa forme combinée
     * si elle existe dans [AynehaCodepoints.COMBINATIONS], sinon insère
     * l'accent isolé correspondant).
     */
    enum class AccentKeyType { DOUBLE, NASALISE, MUET, OUVERT }

    val accentRow: List<Pair<AccentKeyType, String>> = listOf(
        AccentKeyType.DOUBLE to "˝",
        AccentKeyType.NASALISE to "˜",
        AccentKeyType.MUET to "̥",
        AccentKeyType.OUVERT to "˘"
    )
}
