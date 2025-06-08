package com.example.librai.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val gender: String = "",
    val profileCompleted: Int = 0
):Parcelable