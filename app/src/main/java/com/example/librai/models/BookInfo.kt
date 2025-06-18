package com.example.librai.models

import kotlinx.serialization.Serializable

@Serializable
class BookInfo (
    val title: String = "",
    val author: String = "",
    val coverUrl: String = ""
)