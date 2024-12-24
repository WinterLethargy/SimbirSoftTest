package com.example.simbirsofttest.feature.deal.extensions

import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.setEditableBehaviour(editable: Boolean) = apply {
    isCursorVisible = editable
    showSoftInputOnFocus = editable
}