package com.example.simbirsofttest.core.utils

import android.content.Context
import android.util.TypedValue
import android.view.View

context(View)
val Int.dpToPx: Float get() =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        resources.displayMetrics
    )
