package com.maigus.ayneha.sdk

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * TextWatcher optionnel qui translittère automatiquement, au fil de la
 * saisie, le texte latin tapé sur un clavier système standard vers les
 * glyphes AYNEHA — sans passer par [AynehaKeyboardView] ni par l'IME.
 *
 * Usage :
 * ```kotlin
 * val editText = findViewById<EditText>(R.id.monChamp)
 * editText.addTextChangedListener(AynehaTextWatcher(editText))
 * ```
 *
 * Pour désactiver la translittération automatique et laisser l'utilisateur
 * taper directement des glyphes AYNEHA (par ex. via [AynehaInputMethodService]),
 * ne posez simplement pas ce TextWatcher.
 *
 * ## Note d'implémentation — inversion RTL par bloc
 * [AynehaTransliterator] inverse chaque bloc (mot) caractère par caractère
 * une fois transcrit (règle 11 de la spécification), exactement comme le
 * fait `transcripteur_ayneha.html`. Un champ de saisie ne peut donc pas être
 * retraité tel quel à chaque frappe (le texte déjà converti et inversé
 * serait réinversé à répétition). Ce watcher conserve donc en interne un
 * tampon `rawBuffer` en latin brut (jamais affiché), et reconstruit ce
 * tampon par différence à chaque modification du champ affiché, avant de
 * le retranslittérer en entier via [AynehaTransliterator.transliterate].
 *
 * Ce mécanisme couvre correctement la frappe séquentielle normale (ajout en
 * fin de texte, retour arrière). L'édition en milieu de texte déjà validé
 * (ex. revenir corriger un mot plus tôt dans le paragraphe après avoir
 * continué à taper la suite) n'a pas de correspondance bi-univoque garantie
 * une fois un bloc inversé : dans ce cas, le watcher retombe sur un
 * remplacement au plus proche (bord de la zone modifiée) plutôt que sur une
 * réinterprétation phonologique exacte. Pour une conversion de texte déjà
 * complet (import, collage, traitement par lot), préférez un appel direct à
 * [AynehaTransliterator.transliterate] sur le texte source, comme le fait
 * `transcripteur_ayneha.html`.
 */
class AynehaTextWatcher(private val target: EditText) : TextWatcher {

    /** Tampon latin brut, jamais affiché — seule source de vérité pour la translittération. */
    private val rawBuffer = StringBuilder()

    /** Dernier texte affiché produit par ce watcher, pour calculer la différence au prochain passage. */
    private var previousDisplayed: String = ""

    private var selfEdit = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (selfEdit || s == null) return
        val displayed = s.toString()
        if (displayed == previousDisplayed) return

        val prefixLen = commonPrefixLength(previousDisplayed, displayed)
        val suffixLen = commonSuffixLength(previousDisplayed, displayed, prefixLen)
        val removedCount = (previousDisplayed.length - prefixLen - suffixLen).coerceAtLeast(0)
        val inserted = displayed.substring(prefixLen, displayed.length - suffixLen)

        if (removedCount > 0) {
            val toRemove = removedCount.coerceAtMost(rawBuffer.length)
            rawBuffer.delete(rawBuffer.length - toRemove, rawBuffer.length)
        }
        if (inserted.isNotEmpty()) {
            rawBuffer.append(inserted)
        }

        val converted = AynehaTransliterator.transliterate(rawBuffer.toString())
        previousDisplayed = converted
        if (converted == displayed) return

        selfEdit = true
        val cursor = target.selectionEnd
        s.replace(0, s.length, converted)
        target.setSelection(cursor.coerceIn(0, converted.length))
        selfEdit = false
    }

    /** Réinitialise le tampon brut (par ex. après avoir vidé le champ manuellement). */
    fun reset() {
        rawBuffer.setLength(0)
        previousDisplayed = ""
    }

    private fun commonPrefixLength(a: String, b: String): Int {
        val max = minOf(a.length, b.length)
        var i = 0
        while (i < max && a[i] == b[i]) i++
        return i
    }

    private fun commonSuffixLength(a: String, b: String, prefixLen: Int): Int {
        val maxA = a.length - prefixLen
        val maxB = b.length - prefixLen
        val max = minOf(maxA, maxB)
        var i = 0
        while (i < max && a[a.length - 1 - i] == b[b.length - 1 - i]) i++
        return i
    }
}
