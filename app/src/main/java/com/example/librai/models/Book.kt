package com.example.librai.models

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val description: String = "",
    val coverUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String? = null,
    val notes: String? = null
)