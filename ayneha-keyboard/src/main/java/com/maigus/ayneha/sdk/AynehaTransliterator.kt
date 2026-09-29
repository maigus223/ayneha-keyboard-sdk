package com.maigus.ayneha.sdk

/**
 * Moteur de translittération latin → AYNEHA.
 *
 * Implémente, sans option ni variante, l'intégralité des règles de
 * transcription AYNEHA telles que définies dans le document de référence
 * (Règles de transcription AYNEHA) :
 *
 * 1. Consonnes simples
 * 2. Digraphes et trigraphe consonantiques (avec priorité et conditions)
 * 3. Voyelles simples
 * 4. Voyelles ouvertes (saisie directe)
 * 5. Voyelles nasalisées (saisie directe + construction voyelle+"n")
 * 6. Gémination (redoublement)
 * 7. Consonnes muettes
 * 8. Résolution du codepoint final
 * 9. Chiffres
 * 10. Ponctuation (dont miroir RTL)
 * 11. Segmentation et ordre d'affichage (RTL)
 *
 * C'est une fonction pure : `transliterate(texte)` ne dépend que de son
 * entrée. Aucune règle n'est désactivable — le texte d'entrée est censé
 * être du latin brut, non converti.
 */
object AynehaTransliterator {

    // ------------------------------------------------------------------
    // 3 & 4. Tables de lettres
    // ------------------------------------------------------------------

    /** Consonnes simples (règle 1), y compris les caractères IPA à saisie directe. */
    private val SIMPLE_CONSONANTS: Map<Char, String> = mapOf(
        'b' to "B", 'p' to "P", 'm' to "M", 'f' to "F", 'w' to "W", 'v' to "W",
        'd' to "D", 't' to "T", 'n' to "N", 'l' to "L", 'r' to "R", 's' to "S",
        'z' to "Z", 'c' to "TS", 'j' to "J", 'y' to "Y", 'g' to "G", 'k' to "K",
        'h' to "H", 'š' to "SH", 'ŋ' to "NG", 'ɲ' to "NY"
    )

    /** Digraphes prioritaires (règle 2), toujours testés avant les conditionnés. "ng" est prioritaire et gourmand. */
    private val PRIORITY_DIGRAPHS: List<Pair<String, String>> = listOf(
        "ng" to "NG", "sh" to "SH", "dj" to "DJ"
    )

    /** Digraphes conditionnés par une voyelle suivante (règle 2), avec leurs exclusions. */
    private data class ConditionedDigraph(val pattern: String, val key: String, val excludedIfFollowedBy: String?)
    private val CONDITIONED_DIGRAPHS: List<ConditionedDigraph> = listOf(
        ConditionedDigraph("gn", "NY", null),
        ConditionedDigraph("ny", "NY", null),
        ConditionedDigraph("ni", "NY", "nii"),
        ConditionedDigraph("di", "DJ", "dii")
    )

    private const val SIMPLE_VOWELS = "aeiou"

    /** Caractères comptant comme "voyelle" pour les conditions de digraphes/nasalisation. */
    private const val VOWEL_LOOKAHEAD_CHARS = "aeiouɛɔãẽĩõũ"

    private val DIRECT_NASAL_VOWELS: Map<Char, String> = mapOf(
        'ã' to "A", 'ẽ' to "E", 'ĩ' to "I", 'õ' to "O", 'ũ' to "U"
    )

    private fun isVowelLookahead(c: Char): Boolean = c.lowercaseChar() in VOWEL_LOOKAHEAD_CHARS

    // ------------------------------------------------------------------
    // Modèle interne d'un token une fois la phrase tokenisée
    // ------------------------------------------------------------------

    private enum class Category { VOWEL, CONSONANT, OTHER }
    private data class Token(val key: String, val category: Category, var accent: AynehaCodepoints.Accent?)

    // ------------------------------------------------------------------
    // 9. Chiffres
    // ------------------------------------------------------------------

    private fun transliterateDigits(run: String): String {
        val sb = StringBuilder(run.length)
        for (c in run) sb.append(AynehaCodepoints.digit(c - '0'))
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // 10. Ponctuation (dont miroir RTL)
    // ------------------------------------------------------------------

    private val MIRROR_PAIRS: Map<Char, Char> = mapOf(
        '(' to ')', ')' to '(', '[' to ']', ']' to '[', '{' to '}', '}' to '{'
    )
    private const val ARABIC_QUESTION_MARK = '\u061F'

    private fun transliterateOther(run: String): String {
        val sb = StringBuilder(run.length)
        for (c in run) {
            when {
                c == ',' -> sb.append(AynehaCodepoints.VIRGULE)
                c == ';' -> sb.append(AynehaCodepoints.POINT_VIRGULE)
                c == '?' -> sb.append(ARABIC_QUESTION_MARK)
                MIRROR_PAIRS.containsKey(c) -> sb.append(MIRROR_PAIRS.getValue(c))
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // 1, 2, 3, 4, 5, 6, 7. Tokenisation d'un mot (run de lettres)
    // ------------------------------------------------------------------

    private fun tokenizeWord(word: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        val lower = word.lowercase()
        val n = word.length
        var i = 0

        fun matches(pattern: String, at: Int): Boolean =
            at + pattern.length <= n && lower.regionMatches(at, pattern, 0, pattern.length)

        while (i < n) {
            val c = lower[i]

            // Règle 2 — trigraphe "tch" (le plus long, testé en premier)
            if (matches("tch", i)) {
                var len = 3
                var accent: AynehaCodepoints.Accent? = null
                if (matches("tch", i + 3)) { len = 6; accent = AynehaCodepoints.Accent.DOUBLE }
                tokens += Token("TS", Category.CONSONANT, accent)
                i += len
                continue
            }

            // Règle 2 — digraphes prioritaires : ng (toujours prioritaire et gourmand), sh, dj
            val priorityMatch = PRIORITY_DIGRAPHS.firstOrNull { (pattern, _) -> matches(pattern, i) }
            if (priorityMatch != null) {
                val (pattern, key) = priorityMatch
                var len = pattern.length
                var accent: AynehaCodepoints.Accent? = null
                if (matches(pattern, i + pattern.length)) { len += pattern.length; accent = AynehaCodepoints.Accent.DOUBLE }
                tokens += Token(key, Category.CONSONANT, accent)
                i += len
                continue
            }

            // Règle 2 — digraphes conditionnés par une voyelle suivante : gn, ny, ni (sauf nii), di (sauf dii)
            val conditionedMatch = CONDITIONED_DIGRAPHS.firstOrNull { cd ->
                matches(cd.pattern, i) &&
                    (i + cd.pattern.length) < n && isVowelLookahead(lower[i + cd.pattern.length]) &&
                    (cd.excludedIfFollowedBy == null || !matches(cd.excludedIfFollowedBy, i))
            }
            if (conditionedMatch != null) {
                tokens += Token(conditionedMatch.key, Category.CONSONANT, null)
                i += conditionedMatch.pattern.length
                continue
            }

            // Règle 1 — consonnes IPA à saisie directe : š, ŋ, ɲ (avec gémination possible)
            if (c == 'š' || c == 'ŋ' || c == 'ɲ') {
                val key = SIMPLE_CONSONANTS.getValue(c)
                var len = 1
                var accent: AynehaCodepoints.Accent? = null
                if (i + 1 < n && lower[i + 1] == c) { len = 2; accent = AynehaCodepoints.Accent.DOUBLE }
                tokens += Token(key, Category.CONSONANT, accent)
                i += len
                continue
            }

            // Règle 4 — voyelles ouvertes à saisie directe : ɛ, ɔ (doublée -> repli sur la forme simple, sans glyphe dédié)
            if (c == 'ɛ' || c == 'ɔ') {
                val key = if (c == 'ɛ') "E" else "O"
                var len = 1
                if (i + 1 < n && lower[i + 1] == c) len = 2
                tokens += Token(key, Category.VOWEL, AynehaCodepoints.Accent.OUVERT)
                i += len
                continue
            }

            // Règle 5a — voyelles nasalisées à saisie directe : ã, ẽ, ĩ, õ, ũ
            val directNasalKey = DIRECT_NASAL_VOWELS[c]
            if (directNasalKey != null) {
                tokens += Token(directNasalKey, Category.VOWEL, AynehaCodepoints.Accent.NASALISE)
                i += 1
                continue
            }

            // Règles 3, 5b, 6 — voyelle simple : gémination, puis nasalisation par "n", sinon glyphe de base
            if (c in SIMPLE_VOWELS) {
                val key = c.uppercaseChar().toString()

                // Règle 6 — gémination : même voyelle répétée
                if (i + 1 < n && lower[i + 1] == c) {
                    tokens += Token(key, Category.VOWEL, AynehaCodepoints.Accent.DOUBLE)
                    i += 2
                    continue
                }

                // Règle 5b — nasalisation par construction voyelle + "n"
                if (i + 1 < n && lower[i + 1] == 'n') {
                    val nPos = i + 1
                    val nIsDoubled = nPos + 1 < n && lower[nPos + 1] == 'n'
                    val nFollowedByVowel = nPos + 1 < n && isVowelLookahead(lower[nPos + 1])
                    // "voyelle + ng" (ang, ing, ong) n'est jamais une nasalisation : "ng" est
                    // capté en priorité comme consonne Ŋ séparée (règle 2), donc le "n" doit
                    // rester disponible pour être ré-évalué comme début du digraphe "ng".
                    val nFollowedByG = nPos + 1 < n && lower[nPos + 1] == 'g'
                    val nIsWordFinal = nPos == n - 1
                    val wordFinalException = nIsWordFinal && n > 3
                    val excluded = nIsDoubled || nFollowedByVowel || nFollowedByG || wordFinalException
                    if (!excluded) {
                        tokens += Token(key, Category.VOWEL, AynehaCodepoints.Accent.NASALISE)
                        i += 2
                        continue
                    }
                }

                tokens += Token(key, Category.VOWEL, null)
                i += 1
                continue
            }

            // Règle 1 — consonne simple restante (dont "c" isolé -> TS, "v" -> W), avec gémination
            val simpleKey = SIMPLE_CONSONANTS[c]
            if (simpleKey != null) {
                var len = 1
                var accent: AynehaCodepoints.Accent? = null
                if (i + 1 < n && lower[i + 1] == c) { len = 2; accent = AynehaCodepoints.Accent.DOUBLE }
                tokens += Token(simpleKey, Category.CONSONANT, accent)
                i += len
                continue
            }

            // Repli : lettre non reconnue par l'alphabet AYNEHA -> recopiée telle quelle
            tokens += Token(word[i].toString(), Category.OTHER, null)
            i += 1
        }
        return tokens
    }

    // ------------------------------------------------------------------
    // Règle 7 — consonnes muettes (2e passe, après tokenisation)
    // ------------------------------------------------------------------

    private fun applyMuteConsonants(tokens: MutableList<Token>) {
        for (idx in tokens.indices) {
            val t = tokens[idx]
            if (t.category == Category.CONSONANT && t.accent == null) {
                val nextIsVowel = idx + 1 < tokens.size && tokens[idx + 1].category == Category.VOWEL
                if (!nextIsVowel) t.accent = AynehaCodepoints.Accent.MUET
            }
        }
    }

    // ------------------------------------------------------------------
    // Règle 8 — résolution du codepoint final
    // ------------------------------------------------------------------

    private fun resolveToken(t: Token): Char? {
        return when (t.category) {
            Category.OTHER -> t.key.firstOrNull()
            Category.VOWEL -> when (t.accent) {
                AynehaCodepoints.Accent.DOUBLE ->
                    AynehaCodepoints.COMBINATIONS[t.key to AynehaCodepoints.Accent.DOUBLE] ?: AynehaCodepoints.VOWELS[t.key]
                AynehaCodepoints.Accent.NASALISE ->
                    AynehaCodepoints.COMBINATIONS[t.key to AynehaCodepoints.Accent.NASALISE] ?: AynehaCodepoints.VOWELS[t.key]
                AynehaCodepoints.Accent.OUVERT ->
                    AynehaCodepoints.COMBINATIONS[t.key to AynehaCodepoints.Accent.OUVERT] ?: AynehaCodepoints.VOWELS[t.key]
                else -> AynehaCodepoints.VOWELS[t.key]
            }
            Category.CONSONANT -> when (t.accent) {
                AynehaCodepoints.Accent.DOUBLE ->
                    AynehaCodepoints.COMBINATIONS[t.key to AynehaCodepoints.Accent.DOUBLE] ?: AynehaCodepoints.CONSONANTS[t.key]
                AynehaCodepoints.Accent.MUET ->
                    AynehaCodepoints.COMBINATIONS[t.key to AynehaCodepoints.Accent.MUET] ?: AynehaCodepoints.CONSONANTS[t.key]
                else -> AynehaCodepoints.CONSONANTS[t.key]
            }
        }
    }

    private fun transliterateWord(word: String): String {
        val tokens = tokenizeWord(word)
        applyMuteConsonants(tokens)
        val sb = StringBuilder(word.length)
        for (t in tokens) {
            resolveToken(t)?.let { sb.append(it) }
        }
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // Règle 11 — segmentation en runs homogènes + inversion RTL par bloc
    // ------------------------------------------------------------------

    private enum class RunKind { LETTER, DIGIT, OTHER }

    private fun runKindOf(c: Char): RunKind = when {
        c.isDigit() -> RunKind.DIGIT
        c.isLetter() -> RunKind.LETTER
        else -> RunKind.OTHER
    }

    /** Transcrit un bloc (suite de caractères sans espace), puis l'inverse caractère par caractère (RTL). */
    private fun transliterateBlock(block: String): String {
        if (block.isEmpty()) return block
        val sb = StringBuilder(block.length)
        var i = 0
        while (i < block.length) {
            val kind = runKindOf(block[i])
            var j = i + 1
            while (j < block.length && runKindOf(block[j]) == kind) j++
            val run = block.substring(i, j)
            sb.append(
                when (kind) {
                    RunKind.DIGIT -> transliterateDigits(run)
                    RunKind.LETTER -> transliterateWord(run)
                    RunKind.OTHER -> transliterateOther(run)
                }
            )
            i = j
        }
        // Inversion visuelle RTL du bloc entier, glyphe par glyphe (règle 11).
        return sb.reverse().toString()
    }

    // ------------------------------------------------------------------
    // Point d'entrée public
    // ------------------------------------------------------------------

    /**
     * Translittère un texte latin brut en AYNEHA, en appliquant l'ensemble
     * des règles de transcription : conversion lettre/mot, chiffres,
     * ponctuation, et segmentation/inversion RTL par bloc.
     *
     * Le texte est découpé en paragraphes (retours à la ligne), puis en
     * blocs (espaces) : l'ordre des paragraphes et des blocs n'est jamais
     * modifié — seul le contenu de chaque bloc est inversé après
     * transcription (règle 11).
     */
    fun transliterate(text: String): String {
        val paragraphs = text.split("\n")
        val transformed = paragraphs.map { paragraph ->
            paragraph.split(" ").joinToString(" ") { block -> transliterateBlock(block) }
        }
        return transformed.joinToString("\n")
    }
}
