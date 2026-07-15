package com.moneyscribbl.data

const val FALLBACK_FOLDER = "Other"

val defaultFinanceCategories = listOf(
    Folder("Salary", emoji = "💰"),
    Folder("Grocery", emoji = "🛒"),
    Folder("Food", emoji = "🍔"),
    Folder("Transport", emoji = "🚗"),
    Folder("Shopping", emoji = "🛍️"),
    Folder("Bills", emoji = "🧾"),
    Folder("Health", emoji = "🏥"),
    Folder("Entertainment", emoji = "🍿"),
    Folder("Savings", emoji = "🏦"),
    Folder(FALLBACK_FOLDER, emoji = "📁")
)

