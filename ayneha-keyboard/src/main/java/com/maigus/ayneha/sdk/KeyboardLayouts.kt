package com.maigus.ayneha.sdk

/**
 * Description d'une touche du clavier AYNEHA : le caractère AYNEHA qu'elle
 * émet, plus une étiquette latine facultative affichée en mode
 * translittération (pour aider un utilisateur non encore familier du script).
 */
data class AynehaKey(val char: Char, val latinLabel: String)

/**
 * Dispositions de touches partagées par [AynehaKeyboardView] (widget in-app)
 * et [com.maigus.ayneha.sdk.ime.AynehaInputMethodService] (clavier système),
 * pour garantir un comportement strictement identique entre les deux modes
 * d'intégration.
 *
 * Organisation en rangées façon clavier Gboard/Keyman (format 10-10-7),
 * complétée d'une rangée de chiffres et d'une rangée de contrôle.
 */
object KeyboardLayouts {

    val digitsRow: List<AynehaKey> = (0..9).map {
        AynehaKey(AynehaCodepoints.digit(it), it.toString())
    }

    // Rangée 1 (10 touches) : consonnes labiales/dentales + voyelles fréquentes
    val row1: List<AynehaKey> = listOf(
        "B", "P", "M", "F", "W", "D", "T", "N", "L", "R"
    ).map { AynehaKey(AynehaCodepoints.CONSONANTS.getValue(it), it) }

    // Rangée 2 (10 touches) : consonnes sifflantes/palatales + occlusives
    val row2: List<AynehaKey> = listOf(
        "S", "Z", "SH", "TS", "J", "DJ", "NY", "Y", "G", "K"
    ).map { AynehaKey(AynehaCodepoints.CONSONANTS.getValue(it), it) }

    // Rangée 3 (7 touches) : consonnes restantes + les 5 voyelles de base
    val row3: List<AynehaKey> = listOf("H", "NG").map {
        AynehaKey(AynehaCodepoints.CONSONANTS.getValue(it), it)
    } + listOf("A", "E", "I", "O", "U").map {
        AynehaKey(AynehaCodepoints.VOWELS.getValue(it), it)
    }

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
