package com.sparktobloom.receipts.data

data class InStoreItem(
    val storeName: String,
    val itemName: String,
    val units: Int,
    val unitPrice: Float,
    val totalPrice: Float
)
