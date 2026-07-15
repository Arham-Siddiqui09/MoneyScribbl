package com.moneyscribbl.data

data class UpiAppInfo(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable? = null
)

