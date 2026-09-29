package com.maigus.ayneha.sample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.maigus.ayneha.sdk.AynehaKeyboard
import com.maigus.ayneha.sdk.AynehaKeyboardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Mode 1 : widget clavier in-app, saisie AYNEHA directe ---
        val editDirect = findViewById<EditText>(R.id.editDirect)
        val aynehaKeyboard = findViewById<AynehaKeyboardView>(R.id.aynehaKeyboard)
        aynehaKeyboard.attachTo(editDirect)

        // --- Mode 2 : translittération automatique latin -> AYNEHA ---
        val editTranslit = findViewById<EditText>(R.id.editTranslit)
        AynehaKeyboard.enableTransliteration(editTranslit)

        // --- Mode 3 : activer le clavier système AYNEHA ---
        findViewById<Button>(R.id.btnEnableSystemKeyboard).setOnClickListener {
            AynehaKeyboard.openSystemKeyboardSettings(this)
        }
    }
}
