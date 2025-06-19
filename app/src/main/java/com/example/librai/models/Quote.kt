package com.example.librai.models

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val q: String,
    val a: String
)
