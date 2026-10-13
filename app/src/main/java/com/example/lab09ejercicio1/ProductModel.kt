package com.example.lab09ejercicio1

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("products")
    val products: List<ProductModel>
)

data class ProductModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val thumbnail: String,
    val brand: String?
)
