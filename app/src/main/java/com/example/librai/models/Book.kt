package com.example.librai.models

data class Book(
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val imageUrl: String = "",
    val summary: String? = null,
    val notes: String? = null
)