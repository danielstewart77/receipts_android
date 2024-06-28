package com.sparktobloom.receipts.utils

import com.sparktobloom.receipts.data.InStoreItem
import com.sparktobloom.receipts.data.UniqueEmailValidationResponse
import com.sparktobloom.receipts.data.UserRequestDto
import com.sparktobloom.receipts.data.UserResponseDto
import com.sparktobloom.receipts.data.ValidateEmailBody
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiConsumer {

    @POST("email_unique")
    suspend fun validateUniqueEmail(@Body body: ValidateEmailBody): Response<UniqueEmailValidationResponse>

    @POST("login")
    suspend fun login(@Body userDto: UserRequestDto): Response<UserResponseDto>

    @GET("authenticated")
    suspend fun authenticated(
        @Header("Authorization") token: String,
        @Header("Cookie") cookie: String,
    ): Response<Boolean>

    @POST("uploadreceipt")
    @Multipart
    suspend fun uploadReceipt(
        @Header("Authorization") token: String,
        @Header("Cookie") cookie: String,
        @Part file: MultipartBody.Part
    ): Response<Any>

    @POST("uploadinstore")
    @Multipart
    suspend fun uploadinstore(
        @Header("Authorization") token: String,
        @Header("Cookie") cookie: String,
        @Part file: MultipartBody.Part
    ): Response<InStoreItem>

    @POST("chat")
    suspend fun chat(
        @Header("Authorization") token: String,
        @Header("Cookie") cookie: String,
        @Body message: Map<String, String>
    ): Response<Map<String, String>>
}