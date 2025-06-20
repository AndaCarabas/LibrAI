package com.example.librai.models

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val description: String? = null,
    val coverUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val categories: List<String> = emptyList(),      // e.g. ["Mystery","Classic"]
    val status: String = "To Read"
)