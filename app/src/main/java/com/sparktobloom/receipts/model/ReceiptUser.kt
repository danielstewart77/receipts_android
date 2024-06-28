package com.sparktobloom.receipts.model;

object ReceiptUserSingleton {
    var user: ReceiptUser? =null
}

data class ReceiptUser (
    val userName: String,
    val emailAddress: String,
    val accessToken: String,
    val refreshToken: String
)