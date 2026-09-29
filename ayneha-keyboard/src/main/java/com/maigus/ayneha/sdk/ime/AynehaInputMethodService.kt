package com.maigus.ayneha.sdk.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import com.maigus.ayneha.sdk.AynehaKeyboardView
import com.maigus.ayneha.sdk.InputConnectionTarget

/**
 * Clavier système AYNEHA. Une fois l'application hôte installée, l'utilisateur
 * doit l'activer manuellement dans Paramètres > Langues et saisie > Claviers
 * — comme pour n'importe quel clavier tiers — puis le sélectionner via le
 * sélecteur de méthode de saisie standard d'Android.
 *
 * Réutilise exactement le même [AynehaKeyboardView] que le mode widget
 * in-app : la disposition des touches et le rendu sont garantis identiques
 * entre les deux modes d'intégration.
 */
class AynehaInputMethodService : InputMethodService() {

    private var keyboardView: AynehaKeyboardView? = null

    override fun onCreateInputView(): View {
        val view = AynehaKeyboardView(this)
        view.attachTo(InputConnectionTarget { currentInputConnection })
        keyboardView = view
        return view
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboardView = null
    }
}
