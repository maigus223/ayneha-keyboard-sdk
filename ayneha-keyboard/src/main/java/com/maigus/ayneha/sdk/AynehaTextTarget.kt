package com.maigus.ayneha.sdk

import android.view.inputmethod.InputConnection
import android.widget.EditText

/**
 * Cible d'écriture abstraite : permet à [AynehaKeyboardView] et à
 * [com.maigus.ayneha.sdk.ime.AynehaInputMethodService] de partager
 * exactement la même logique de clavier, qu'ils écrivent dans un
 * [EditText] classique (mode widget in-app) ou dans un [InputConnection]
 * (mode clavier système).
 */
interface AynehaTextTarget {
    fun commitText(text: CharSequence)
    fun deleteBackward()
    fun commitNewline()
}

/** Cible = un [EditText] de l'application hôte (mode widget in-app). */
class EditTextTarget(private val editText: EditText) : AynehaTextTarget {
    override fun commitText(text: CharSequence) {
        val start = editText.selectionStart.coerceAtLeast(0)
        val end = editText.selectionEnd.coerceAtLeast(0)
        editText.text.replace(minOf(start, end), maxOf(start, end), text)
    }

    override fun deleteBackward() {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start != end) {
            editText.text.delete(minOf(start, end), maxOf(start, end))
        } else if (start > 0) {
            editText.text.delete(start - 1, start)
        }
    }

    override fun commitNewline() {
        commitText("\n")
    }
}

/** Cible = l'[InputConnection] courant fourni par le framework IME (mode clavier système). */
class InputConnectionTarget(private val provider: () -> InputConnection?) : AynehaTextTarget {
    override fun commitText(text: CharSequence) {
        provider()?.commitText(text, 1)
    }

    override fun deleteBackward() {
        val ic = provider() ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    override fun commitNewline() {
        provider()?.commitText("\n", 1)
    }
}
