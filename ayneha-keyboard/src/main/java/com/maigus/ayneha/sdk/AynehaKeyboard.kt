package com.maigus.ayneha.sdk

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Point d'entrée simple du SDK AYNEHA Keyboard. Regroupe les deux modes
 * d'intégration proposés :
 *
 * 1. **Saisie directe AYNEHA** — via [AynehaKeyboardView] (widget in-app,
 *    voir le layout) ou via le clavier système
 *    [com.maigus.ayneha.sdk.ime.AynehaInputMethodService] (voir [openSystemKeyboardSettings]).
 * 2. **Translittération automatique latin → AYNEHA** — via [AynehaTextWatcher],
 *    posé sur n'importe quel [EditText] standard, sans clavier custom.
 *
 * Le développeur intégrateur choisit librement l'un, l'autre, ou les deux
 * (par ex. clavier système AYNEHA disponible en option, avec repli sur
 * translittération automatique pour les utilisateurs qui tapent en latin).
 */
object AynehaKeyboard {

    /** Version du SDK, à des fins de diagnostic/rapport de bug. */
    const val VERSION = "1.0.0"

    /**
     * Active la translittération automatique latin → AYNEHA en temps réel
     * sur [editText], selon les règles de [AynehaTransliterator] (non
     * configurables — un seul jeu de règles, conforme à la spécification
     * AYNEHA). Retourne le [AynehaTextWatcher] posé, à retirer via
     * `editText.removeTextChangedListener(...)` si besoin.
     */
    fun enableTransliteration(editText: EditText): AynehaTextWatcher {
        val watcher = AynehaTextWatcher(editText)
        editText.addTextChangedListener(watcher)
        return watcher
    }

    /**
     * Convertit un texte latin en AYNEHA sans toucher à l'UI (utilisable côté
     * traitement de données, export, etc.). Voir [AynehaTransliterator] pour
     * le détail des règles appliquées.
     */
    fun transliterate(latin: String): String = AynehaTransliterator.transliterate(latin)

    /** Un [AynehaTextTarget] prêt à l'emploi pour piloter un [EditText] avec des caractères AYNEHA bruts. */
    fun target(editText: EditText): AynehaTextTarget = EditTextTarget(editText)

    /**
     * Indique si le clavier système AYNEHA est actuellement activé dans les
     * paramètres Android (installé + coché dans la liste des claviers).
     * Ne préjuge pas qu'il soit le clavier *actif* à l'instant T.
     */
    fun isSystemKeyboardEnabled(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            ?: return false
        val pkg = context.packageName
        return imm.enabledInputMethodList.any { it.packageName == pkg }
    }

    /**
     * Ouvre l'écran système "Langues et saisie > Claviers" où l'utilisateur
     * peut activer le clavier AYNEHA. Android n'autorise pas l'activation
     * programmatique directe d'un IME pour des raisons de sécurité — c'est
     * le flux standard utilisé par tous les claviers tiers (Gboard, SwiftKey…).
     */
    fun openSystemKeyboardSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Ouvre le sélecteur de méthode de saisie standard (pour changer de clavier actif). */
    fun showInputMethodPicker(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }
}
