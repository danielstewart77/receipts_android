package com.sparktobloom.receipts.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserResponseDto(
    val user: User,
    val accessToken: String,
    val refreshToken: String
) : Parcelable
