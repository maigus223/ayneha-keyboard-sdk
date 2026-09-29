package com.maigus.ayneha.sdk

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

/**
 * Widget clavier AYNEHA autonome, à poser directement dans le XML d'une
 * application hôte (au-dessus ou en overlay d'un [android.widget.EditText],
 * par exemple), sans passer par le mécanisme d'IME système.
 *
 * Usage minimal :
 * ```xml
 * <com.maigus.ayneha.sdk.AynehaKeyboardView
 *     android:id="@+id/aynehaKeyboard"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 * ```kotlin
 * findViewById<AynehaKeyboardView>(R.id.aynehaKeyboard).attachTo(monEditText)
 * ```
 *
 * Personnalisation via attributs XML (`ak_keyBackgroundColor`,
 * `ak_keyTextColor`, `ak_accentColor`, `ak_backgroundColor`,
 * `ak_showDigitRow`, `ak_startInTransliterationMode`) — voir `attrs.xml`.
 */
class AynehaKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var target: AynehaTextTarget? = null
    private var showLatinLabels: Boolean
    private var showDigitRow: Boolean
    private val aynehaFont: Typeface
    private val keyTextColor: Int
    private val accentColor: Int
    private val allKeyButtons = mutableListOf<Pair<Button, AynehaKey>>()

    /** Écouteur facultatif notifié à chaque caractère inséré (utile pour analytics/validation). */
    var onCharInserted: ((Char) -> Unit)? = null

    init {
        orientation = VERTICAL
        layoutDirection = LAYOUT_DIRECTION_RTL
        setBackgroundColor(ContextCompat.getColor(context, R.color.ayneha_night))

        val a = context.obtainStyledAttributes(attrs, R.styleable.AynehaKeyboardView)
        val bg = a.getColor(R.styleable.AynehaKeyboardView_ak_backgroundColor, ContextCompat.getColor(context, R.color.ayneha_night))
        keyTextColor = a.getColor(R.styleable.AynehaKeyboardView_ak_keyTextColor, ContextCompat.getColor(context, R.color.ayneha_text))
        accentColor = a.getColor(R.styleable.AynehaKeyboardView_ak_accentColor, ContextCompat.getColor(context, R.color.ayneha_ochre))
        showLatinLabels = a.getBoolean(R.styleable.AynehaKeyboardView_ak_startInTransliterationMode, false)
        showDigitRow = a.getBoolean(R.styleable.AynehaKeyboardView_ak_showDigitRow, true)
        a.recycle()
        setBackgroundColor(bg)

        aynehaFont = ResourcesCompat.getFont(context, R.font.ayneha_type) ?: Typeface.DEFAULT

        buildRows()
    }

    /** Relie ce clavier à un champ de saisie standard de l'application hôte. */
    fun attachTo(editText: android.widget.EditText) {
        target = EditTextTarget(editText)
    }

    /** Relie ce clavier à une cible personnalisée (utilisé en interne par l'IME). */
    fun attachTo(customTarget: AynehaTextTarget) {
        target = customTarget
    }

    /** Bascule l'affichage des étiquettes latines d'aide sur chaque touche (n'affecte pas le caractère inséré, toujours AYNEHA). */
    fun setShowLatinLabels(show: Boolean) {
        showLatinLabels = show
        allKeyButtons.forEach { (btn, key) -> applyKeyLabel(btn, key) }
    }

    private fun applyKeyLabel(button: Button, key: AynehaKey) {
        if (showLatinLabels) {
            button.typeface = Typeface.DEFAULT_BOLD
            button.text = key.latinLabel
            button.textSize = 14f
        } else {
            button.typeface = aynehaFont
            button.text = key.char.toString()
            button.textSize = 20f
        }
    }

    private fun buildRows() {
        removeAllViews()
        allKeyButtons.clear()

        if (showDigitRow) addKeyRow(KeyboardLayouts.digitsRow)
        addKeyRow(KeyboardLayouts.row1)
        addKeyRow(KeyboardLayouts.row2)
        addKeyRow(KeyboardLayouts.row3)
        addControlRow()
    }

    private fun addKeyRow(keys: List<AynehaKey>) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutDirection = LAYOUT_DIRECTION_RTL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        for (key in keys) {
            val btn = Button(context).apply {
                layoutParams = LayoutParams(0, dp(46), 1f).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
                background = ContextCompat.getDrawable(context, R.drawable.ak_key_selector)
                setTextColor(keyTextColor)
                includeFontPadding = false
                minWidth = 0
                minHeight = 0
                minimumWidth = 0
                minimumHeight = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                isAllCaps = false
                stateListAnimator = null
                elevation = 0f
            }
            applyKeyLabel(btn, key)
            allKeyButtons.add(btn to key)
            btn.setOnClickListener {
                target?.commitText(key.char.toString())
                onCharInserted?.invoke(key.char)
            }
            row.addView(btn)
        }
        addView(row)
    }

    private fun addControlRow() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutDirection = LAYOUT_DIRECTION_RTL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val backspace = accentButton("⌫", weight = 1.5f) { target?.deleteBackward() }
        val space = accentButton("␣", weight = 4f) { target?.commitText(" ") }
        val enter = accentButton("⏎", weight = 1.5f) { target?.commitNewline() }
        val labelToggle = accentButton(if (showLatinLabels) "AY" else "AZ", weight = 1.5f) {
            setShowLatinLabels(!showLatinLabels)
        }

        // Ordre XML = premier enfant à droite en RTL : on veut, de droite à gauche,
        // [⌫] [bascule étiquette] [espace] [entrée]
        row.addView(backspace)
        row.addView(labelToggle)
        row.addView(space)
        row.addView(enter)

        addView(row)
    }

    private fun accentButton(label: String, weight: Float, onClick: () -> Unit): Button =
        Button(context).apply {
            layoutParams = LayoutParams(0, dp(46), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            background = ContextCompat.getDrawable(context, R.drawable.ak_key_accent_selector)
            setTextColor(ContextCompat.getColor(context, R.color.ayneha_night))
            text = label
            textSize = 16f
            includeFontPadding = false
            minWidth = 0; minHeight = 0
            setPadding(0, 0, 0, 0)
            gravity = Gravity.CENTER
            isAllCaps = false
            stateListAnimator = null
            elevation = 0f
            setOnClickListener { onClick() }
        }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
