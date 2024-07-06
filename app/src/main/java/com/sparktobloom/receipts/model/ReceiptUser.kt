package com.sparktobloom.receipts.model;

import com.sparktobloom.receipts.data.InStoreItem

object ReceiptUserSingleton {
    var user: ReceiptUser? = null
}

object ConfirmationItem {
    var inStore: InStoreItem? = null
    var receipt: String? = null
}

data class ReceiptUser (
    val userName: String,
    val emailAddress: String,
    val accessToken: String,
    val refreshToken: String
)